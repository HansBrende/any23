package org.apache.any23.encoding;

import org.apache.commons.io.IOUtils;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.BooleanAttribute;
import org.jsoup.nodes.DocumentType;
import org.jsoup.parser.InternalAny23Ext;
import org.jsoup.parser.Tag;
import org.w3c.dom.Attr;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

class EncodingStats {

    private final Tokeniser tokeniser;
    private final Appendable text;


    private final EncodingStatsReader reader;

    EncodingStats(InputStream in, Appendable textAccum) {
        reader = new EncodingStatsReader(in);
        tokeniser = new Tokeniser(new CharacterReader(reader), ParseErrorList.noTracking());
        text = textAccum;
    }

    // TODO:
    //  create handlers that all handle chunks of bytes
    //  1. UTF-8 handler: based on utf-8 state machine
    //      only suspicious ascii chars should be the escapes used in ISO-2022 charsets?
    //      how to short circuit positive: (icu4j way): check if num multibyte > num invalid * 10 (count ascii escapes as invalid)
    //      even better: check if num *unique* multibyte > num invalid * 10
    //  2. html handler:
    //      call tokeniser.nextToken() until we get to EOF

    // Supply a FilterInputStream to tokeniser

    private static final List<String> dataTags = Arrays.asList("script", "style");

    private static final int maxTextLen = 65536;

    private static final double z = 1.96; // Corresponds to 95% chance
    private static double wilsonScoreLowerBound(double p, double n) {
        if (n < 1) {
            return 0;
        }
        final double z2_n = z*z/n;
        return ((p + z2_n * 0.5) - z * Math.sqrt((p*(1-p) + z2_n*0.25)/n)) / (1 + z2_n);
    }
    private static int wilsonScoreLowerBoundPercent(int p, double n) {
        return (int)(wilsonScoreLowerBound(p / 100.0, n) * 100.0);
    }

    private static final class AttrHelper extends Attribute {

        private static final Class<? extends Attribute> booleanAttribute = new Attributes().put("test", null).asList().iterator().next().getClass();
        private static final Class<? extends Attribute> regularAttribute = new Attributes().put("test", "").asList().iterator().next().getClass();
        static {
            if (booleanAttribute.isAssignableFrom(regularAttribute)) {
                throw new AssertionError();
            }
        }

        private AttrHelper() {
            super("", null);
        }

//        static boolean hasValue(Attribute attr) {
//            if (!attr.getValue().isEmpty()) {
//                return true;
//            }
//            return attr.isBooleanAttribute();
//            !isBooleanAttribute(attr.getKey())
//        }

        static boolean valueCanBeOmitted(String attributeKey) {
            String key = attributeKey.toLowerCase(Locale.ENGLISH);
            return isBooleanAttribute(key) || isDataAttribute(key);
        }

        static boolean valueWasOmitted(Attribute attr) {
            return booleanAttribute.isInstance(attr);
        }

        static boolean valueLooksValid(Attribute attr) {
            return !valueWasOmitted(attr) || valueCanBeOmitted(attr.getKey());
        }
    }

//    public static class HtmlStats {
//        final int countMarkupIndicators;
//        final ParseErrorList errors = ParseErrorList.tracking(Integer.MAX_VALUE);
//        // TODO test various sized html pages to see what stats for errors vs. markup indicators is.
//    }

    public static boolean valueWasOmitted(Attribute attr) {
        return attr.getValue().isEmpty() && attr instanceof BooleanAttribute;
    }

    private static final Pattern validNamePattern = Pattern.compile("[a-zA-Z_:][-a-zA-Z0-9_:.]*");

    class Stats {
        public final int pos;
        public final int neg;
        public final int total;
        public final double posPercent, negPercent, posWilson, negWilson;
        public Stats(int pos, int neg) {
            this.pos = pos;
            this.neg = neg;
            total = pos + neg;
            posPercent = pos / (double)total;
            negPercent = neg / (double)total;
            posWilson = wilsonScoreLowerBound(posPercent, total);
            negWilson = wilsonScoreLowerBound(negPercent, total);
        }

        public String toString() {
            return "+" + pos + ", -" + neg + "; +" + posPercent + ", -" + negPercent + "; adjusted: +" + posWilson + ", -" + negWilson;
        }
    }

    static Stats detectEncoding(InputStream in, OutputStream out) {
        int[] indicators = new int[2];
        final int pos = 0;
        final int neg = 0;
        new InternalAny23Ext() {
            {
                block: {
                    try {
                        super.init(new EncodingStatsReader(in));
                    } catch (Exception e) {
                        // TODO disable markup stripping
                        break block;
                    }
                    try {
                        extractText();
                    } catch (org.jsoup.UncheckedIOException ignored) {

                    }
                }
                // TODO if we encounter an exception, simply pipe in to out
            }
            @Override
            protected void doctype(DocumentType docType) {
                indicators[pos]++;
            }

            @Override
            protected void comment(String data) {
                // TODO if is xml declaration, get encoding
                indicators[pos]++;
            }

            @Override
            protected void text(String text) {
                // TODO replace replacement characters with null
            }

            @Override
            protected void startTag(String name, Attributes attributes, boolean selfClosing) {
                // TODO if has charset, set html charset
                if (selfClosing) {
                    indicators[pos]++;
                }
                if (Tag.isKnownTag(name)) {
                    indicators[pos]++;
                } else if (!validNamePattern.matcher(name).matches()) {
                    indicators[neg]++;
                }

                for (Attribute attr : attributes.asList()) { // necessary to use asList() to get BooleanAttribute instances where applicable
                    if (!validNamePattern.matcher(attr.getKey()).matches()) {
                        indicators[neg]++;
                    }
                    if (AttrHelper.valueLooksValid(attr)) {
                        indicators[pos]++;
                    } else {
                        indicators[neg]++;
                    }
                }
            }

            @Override
            protected void endTag(String name) {

            }
        };
        return null;
    }


    Stats detectEncoding() throws IOException {
        Token t;
        int countPositiveIndicators = 0;
        int countNegativeIndicators = 0;
        String currentDataTagName = null;
        long totalChars = 0;
        try {
            while ((t = tokeniser.read()) != null && !t.isEOF()) {
                switch (t.type) {
                    case Doctype:
                        t.asDoctype();
                        countPositiveIndicators++;
                        break;
                    case Comment:
                        t.asComment();
                        countPositiveIndicators++;
                        break;
                    case StartTag:
                        Token.StartTag startTag = t.asStartTag();
                        String tagName = startTag.normalName();
                        if (startTag.isSelfClosing()) {
                            countPositiveIndicators++; // '/>' is a positive indicator
                        }
                        if (Tag.isKnownTag(tagName)) {
                            countPositiveIndicators++;
                        } else if (!validNamePattern.matcher(tagName).matches()) {
                            countNegativeIndicators++;
                        }
                        for (Attribute attr : startTag.getAttributes().asList()) { // necessary to use asList() to get BooleanAttribute instances where applicable
                            if (!validNamePattern.matcher(attr.getKey()).matches()) {
                                countNegativeIndicators++;
                            }
                            if (AttrHelper.valueLooksValid(attr)) {
                                countPositiveIndicators++;
                            } else {
                                countNegativeIndicators++;
                            }
                        }

                        totalChars += tagName.length() + startTag.getAttributes().asList().stream().mapToInt(attr -> attr.getKey().length() + attr.getValue().length()).sum();

                        if (currentDataTagName == null && !startTag.isSelfClosing()) {
                            if ("script".equals(tagName)) {
                                // otherwise will create new element for <n.length ... in e.g., /html/rdfa/rdfa-issue268-and-317.html
                                tokeniser.transition(TokeniserState.ScriptData);
                                currentDataTagName = tagName;
                            } else if ("style".equals(tagName)) {
                                tokeniser.transition(TokeniserState.Rawtext);
                                currentDataTagName = tagName;
                            }

                            // TODO HtmlTreeBuilder also transitions "plaintext" to TokeniserState.PLAINTEXT, "textarea" and "title" to TokeniserState.Rcdata,
                            //   and "noframes", "style", "xmp", "iframe", and "noembed" to TokeniserState.Rawtext
                            //   do we need to follow suit?
                        } else {
                            currentDataTagName = null;
                        }
                        break;
                    case EndTag:
                        countPositiveIndicators++; // '</' is a positive indicator
                        tagName = t.asEndTag().normalName();
                        if (Tag.isKnownTag(tagName)) {
                            countPositiveIndicators++;
                        }
                        totalChars += t.asEndTag().normalName().length();
                        if (tagName.equals(currentDataTagName)) {
                            currentDataTagName = null;
                        }
                        break;
                    case Character:
                        String data = t.asCharacter().getData();
                        totalChars += data.length();
                        if (currentDataTagName == null) {
                            CharBuffer.wrap(t.asCharacter().getData());
                            //text.write(t.asCharacter().getData().getBytes(StandardCharsets.ISO_8859_1));
                            String d = data.trim();
                            if (!d.isEmpty()) {
                                text.append(d + "\n");
                            }
                        }
                        break;
                    default:
                        throw new AssertionError("unrecognized jsoup token type: " + t.type);
                }


                // DOES THIS *** DEFINITELY NOT *** LOOK LIKE MARKUP???
                double totalIndicators = countPositiveIndicators + countNegativeIndicators;
                double percentageOfIndicatorsError = countNegativeIndicators / totalIndicators;
                double adjustedPercentageNegative = wilsonScoreLowerBound(percentageOfIndicatorsError, totalIndicators);



                // WHY SHOULD WE DISCARD MARKUP STRIPPING?
                //   - If we detect that markup stripping would strip out good data (i.e. we're misinterpreting '<' and '>'; they're part of the human readable text)
                //   - i.e. if tag is unknown and attributes' values are empty (i.e., no equals signs) and unknown

//                if (adjustedPercentageNegative > 0.25) { // TODO test various thresholds
//
//                }
//
//
////                // ARE WE IN MARKUP???
////                if (countMarkupIndicators / 2 > errors.size()) {
////
////                }
//
//                if (reader.lastChunk.size() > maxTextLen) {
//                    // check if it looks like we're actually in html markup, if so:
//                    // 1. process text using juniversalcharset
//
//                    //1. process text chunk using icu4j detectors that rely on ngrams
//                    //2. clear text
//
//                    //if not:
////                            reader.setLangDetectionEnabled(true);
////
////                            reader.skip(Long.MAX_VALUE);
//                    break;
//                }
            }
        } catch (CharsetResult e) {
            //return e.name;
        } catch (org.jsoup.UncheckedIOException e) {
            IOException underlying = e.ioException();
            if (underlying != null) {
                throw underlying;
            } else {
                //reader.setLangDetectionEnabled(true);
                try {
                    reader.skip(Long.MAX_VALUE);
                } catch (CharsetResult e2) {
                    //return e2.name;
                }
            }
        }

        //System.out.println("count positive: " + countPositiveIndicators + "; count negative: " + countNegativeIndicators);
        int totalIndicators = countPositiveIndicators + countNegativeIndicators;
        double p = countPositiveIndicators / (double)totalIndicators;
        //System.out.println("positive: " + p + " wilson lowerbound: " + wilsonScoreLowerBound(p, totalIndicators));

        return new Stats(countPositiveIndicators, countNegativeIndicators);
    }
}
