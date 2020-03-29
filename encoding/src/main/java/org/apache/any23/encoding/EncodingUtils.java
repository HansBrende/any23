/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.any23.encoding;

import org.apache.tika.detect.TextStatistics;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.utils.CharsetUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.jsoup.select.Selector;
import org.rypt.f8.Utf8Statistics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Hans Brende
 */
class EncodingUtils {

    private static final String TAG_CHARS = "< />";
    private static final byte[] TAG_BYTES = TAG_CHARS.getBytes(UTF_8);

    public static void main(String[] args) throws Exception {
        ArrayList<String> asciiCompatible = new ArrayList<>();
        ArrayList<String> tagCompatible = new ArrayList<>();
        ArrayList<String> charsetsWithMin1byte = new ArrayList<>();
        ArrayList<String> others = new ArrayList<>();

        Arrays.asList(CharsetDetector.getAllDetectableCharsets()).forEach(cs -> {
            Charset c;
            try {
                c = Charset.forName(cs);
            } catch (Exception e) {
                System.out.println(cs + ": UNSUPPORTED");
                return;
            }

            if (true) {
                //CharsetEncoder ce = c.newEncoder();

                try {

                    ArrayList<Integer> badAscii = new ArrayList<>();
                    ArrayList<Integer> goodAscii = new ArrayList<>();

                    for (char i = 0; i < 128; i++) {
                        if (!new String(new char[]{i}).equals(new String(new byte[]{(byte)i}, c))) {
                            badAscii.add((int)i);
                        } else {
                            goodAscii.add((int)i);
                        }
                    }
//                    char[] asciiChars = new char[128];
//                    byte[] asciiBytes = new byte[128];
//                    for (char i = 0; i < 128; i++) {
//                        asciiChars[i] = i;
//                        asciiBytes[i] = (byte)i;
//                    }
//
//                    char[] printableAsciiChars = new char[127 - 32];
//                    for (char i = 0; i < printableAsciiChars.length; i++) {
//                        printableAsciiChars[i]
//                    }
//
//                    String ascii = new String(asciiChars);
                    if (badAscii.size() == 0) {
                        asciiCompatible.add(cs);
                    } else if (TAG_CHARS.equals(new String(TAG_BYTES, c))) {
                        tagCompatible.add(cs + " (bad ascii: " + badAscii + ")");
                    } else if (c.canEncode()) {
                        int minLen = Integer.MAX_VALUE;
                        int maxLen = 0;
                        CharsetEncoder ce = c.newEncoder();
                        for (int i = 0; i <= Character.MAX_CODE_POINT; i++) {
                            if (ce.canEncode(new String(Character.toChars(i)))) {
                                int len = ce.encode(CharBuffer.wrap(Character.toChars(i))).remaining();

                                minLen = Math.min(minLen, len);
                                maxLen = Math.max(maxLen, len);
                            }
                        }
                        if (minLen == 1) {
                            charsetsWithMin1byte.add(cs + " (good ascii: " + goodAscii + ")");
                        } else {
                            others.add(cs);
                        }
                    } else {
                        System.out.println("CANNOT ENCODE AND NOT ASCII OR TAG COMPATIBLE: " + cs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(cs + ": CANNOT ENCODE");
            }

        });

        System.out.println();
        System.out.println("ascii compatible: " + asciiCompatible);
        System.out.println();
        System.out.println("tag compatible: " + tagCompatible);
        System.out.println();
        System.out.println("min 1 byte: " + charsetsWithMin1byte);
        System.out.println();
        System.out.println(others);
    }

    // Prints:
    //ISO-8859-8-I: UNSUPPORTED
    //IBM424_rtl: UNSUPPORTED
    //IBM424_ltr: UNSUPPORTED
    //IBM420_rtl: UNSUPPORTED
    //IBM420_ltr: UNSUPPORTED
    //
    //ascii compatible: [UTF-8, Shift_JIS, GB18030, EUC-JP, EUC-KR, Big5, ISO-8859-1, ISO-8859-2, ISO-8859-5, ISO-8859-6, ISO-8859-7, ISO-8859-8, windows-1251, windows-1256, KOI8-R, ISO-8859-9, IBM866]
    //
    //tag compatible: [ISO-2022-JP (bad ascii: [14, 15, 27]), ISO-2022-CN (bad ascii: [14, 15, 27]), ISO-2022-KR (bad ascii: [14, 15, 27])]
    //
    //min 1 byte: [IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31]), IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31]), IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31]), IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31]), IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31]), IBM500 (good ascii: [0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 24, 25, 28, 29, 30, 31])]
    //
    //[UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE]

    /**
     * Very efficient method to convert an input stream directly to an ISO-8859-1 encoded string
     */
    static String iso_8859_1(InputStream is) throws IOException {
        StringBuilder chars = new StringBuilder(Math.max(is.available(), 8192));
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer)) != -1) {
            for (int i = 0; i < n; i++) {
                chars.append((char)(buffer[i] & 0xFF));
            }
        }
        return chars.toString();
    }


    //get correct variant, or null if charset is incompatible with stats
    static Charset correctVariant(TextStatistics stats, Charset charset) {
        if (charset == null) {
            return null;
        }
        switch (charset.name()) {
            //ISO-8859-1 variants
            case "ISO-8859-1":
                //Take a hint from icu4j's CharsetRecog_8859_1 and Tika's UniversalEncodingListener:
                // return windows-1252 before ISO-8859-1 if:
                // (1) C1 ctrl chars are used (as in icu4j), or
                // (2) '\r' is used (as in Tika)
                if ((stats.count('\r') != 0 || hasC1Control(stats)) && hasNoneOf(stats, windows1252Illegals)) {
                    try {
                        return forName("windows-1252");
                    } catch (Exception e) {
                        //ignore
                    }
                }
                // TODO should we really flip to 15 if declared otherwise?
                // TODO since 15 isn't standard, flip all characters are common to both
                // TODO \r > \n / 2
                // TODO where is ISO-8859-15 in this switch?
                return iso_8859_1_or_15(stats);
            case "windows-1252":
                return hasNoneOf(stats, windows1252Illegals) ? charset : iso_8859_1_or_15(stats);

            //ISO-8859-2 variants
            case "ISO-8859-2":
                //Take a hint from icu4j's CharsetRecog_8859_2 class:
                // return windows-1250 before ISO-8859-2 if has valid C1 chars
                if (hasC1Control(stats) && hasNoneOf(stats, windows1250Illegals)) {
                    try {
                        return forName("windows-1250");
                    } catch (Exception e) {
                        //ignore
                    }
                }
                return charset;
            case "windows-1250":
                return hasNoneOf(stats, windows1250Illegals) ? charset : charset("ISO-8859-2");

            //ISO-8859-7 variants
            case "ISO-8859-7":
                //Take a hint from icu4j's CharsetRecog_8859_7 class:
                // return windows-1253 before ISO-8859-7 if has valid C1 chars
                if (hasC1Control(stats) && hasNoneOf(stats, windows1253Illegals)) {
                    try {
                        return forName("windows-1253");
                    } catch (Exception e) {
                        //ignore
                    }
                }
                return hasNoneOf(stats, iso_8859_7Illegals) ? charset : null;
            case "windows-1253":
                return hasNoneOf(stats, windows1253Illegals) ? charset :
                        hasNoneOf(stats, iso_8859_7Illegals) ? charset("ISO-8859-7") : null;

            //ISO-8859-8 variants
            case "ISO-8859-8":
            case "ISO-8859-8-I":
                //Take a hint from icu4j's CharsetRecog_8859_8 class:
                // return windows-1255 before ISO-8859-8 if has valid C1 chars
                if (hasC1Control(stats) && hasNoneOf(stats, windows1255Illegals)) {
                    try {
                        return forName("windows-1255");
                    } catch (Exception e) {
                        //ignore
                    }
                }
                return hasNoneOf(stats, iso_8859_8Illegals) ? charset : null;
            case "windows-1255":
                return hasNoneOf(stats, windows1255Illegals) ? charset :
                        hasNoneOf(stats, iso_8859_8Illegals) ? charset("ISO-8859-8") : null;

            //ISO-8859-9 variants
            case "ISO-8859-9":
                //Take a hint from icu4j's CharsetRecog_8859_9 class:
                // return windows-1254 before ISO-8859-9 if has valid C1 chars
                if (hasC1Control(stats) && hasNoneOf(stats, windows1254Illegals)) {
                    try {
                        return forName("windows-1254");
                    } catch (Exception e) {
                        //ignore
                    }
                }
                return charset;
            case "windows-1254":
                return hasNoneOf(stats, windows1254Illegals) ? charset : charset("ISO-8859-9");

            //Others: just make sure no illegal characters are present
            case "windows-1251":
                return hasNoneOf(stats, windows1251Illegals) ? charset : null;
            case "ISO-8859-6":
                return hasNoneOf(stats, iso_8859_6Illegals) ? charset : null;
            default:
                return charset;
        }
    }

    private static Charset iso_8859_1_or_15(TextStatistics stats) {
        //Take a hint from Tika's UniversalEncodingListener:
        // return ISO-8859-15 before ISO-8859-1 if currency/euro symbol is used
        if (stats.count(0xa4) != 0) {
            try {
                return forName("ISO-8859-15");
            } catch (Exception e) {
                //ignore
            }
        }
        return StandardCharsets.ISO_8859_1;
    }

    private static final int[] windows1252Illegals = {0x81, 0x8D, 0x8F, 0x90, 0x9D};
    private static final int[] windows1250Illegals = {0x81, 0x83, 0x88, 0x90, 0x98};
    private static final int[] iso_8859_7Illegals = {0xAE, 0xD2, 0xFF};
    private static final int[] windows1253Illegals = {0x81, 0x88, 0x8A, 0x8C, 0x8D,
            0x8E, 0x8F, 0x90, 0x98, 0x9A, 0x9C, 0x9D, 0x9E, 0x9F, 0xAA, 0xD2, 0xFF};

    private static final int[] windows1255Illegals = {0x81, 0x8A, 0x8C, 0x8D, 0x8E, 0x8F, 0x90, 0x9A,
            0x9C, 0x9D, 0x9E, 0x9F, 0xCA, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xFB, 0xFC, 0xFF};

    private static final int[] iso_8859_8Illegals = {0xA1, 0xBF, 0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5,
            0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF, 0xD0, 0xD1, 0xD2, 0xD3, 0xD4,
            0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xFB, 0xFC, 0xFF};

    private static final int[] windows1254Illegals = {0x81, 0x8D, 0x8E, 0x8F, 0x90, 0x9D, 0x9E};

    private static final int[] windows1251Illegals = {0x98};

    private static final int[] iso_8859_6Illegals = {0xA1, 0xA2, 0xA3, 0xA5, 0xA6, 0xA7,
            0xA8, 0xA9, 0xAA, 0xAB, 0xAE, 0xAF, 0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6,
            0xB7, 0xB8, 0xB9, 0xBA, 0xBC, 0xBD, 0xBE, 0xC0, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
            0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF};


    private static boolean hasNoneOf(TextStatistics stats, int[] illegals) {
        for (int i : illegals) {
            if (stats.count(i) != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasC1Control(TextStatistics ts) {
        for (int i = 0x80; i < 0xA0; i++) {
            if (ts.count(i) != 0) {
                return true;
            }
        }
        return false;
    }

    private static class TextStatisticsOptimizedForUtf8 extends TextStatistics {

        private final Utf8Statistics utf8Stats = new Utf8Statistics();

        @Override
        public void addData(byte[] buffer, int offset, int length) {
            super.addData(buffer, offset, length);
            utf8Stats.write(buffer, offset, length);
        }

        @Override
        public boolean looksLikeUTF8() {
            return utf8Stats.looksLikeUtf8();
        }
    }

    /*
     * Returns a custom implementation of Tika's TextStatistics class for an input stream
     */
    static TextStatistics stats(InputStream stream) throws IOException {
        TextStatisticsOptimizedForUtf8 stats = new TextStatisticsOptimizedForUtf8();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = stream.read(buffer)) != -1) {
            stats.addData(buffer, 0, n);
        }
        return stats;
    }

    static Charset forName(String charset) throws Exception {
        try {
            return CharsetUtils.forName(charset);
        } catch (Exception e) {
            //ICU4j sometimes returns 'ISO-8859-8-I', which is unsupported!
            // Cf. https://en.wikipedia.org/wiki/ISO/IEC_8859-8
            // "Nominally ISO-8859-8 (code page 28598) is for 'visual order',
            // and ISO-8859-8-I (code page 38598) is for logical order.
            // But usually in practice, and required for HTML and XML
            // documents, ISO-8859-8 also stands for logical order text."
            charset = charset.replaceAll("(?i)-I\\b", "");
            try {
                return CharsetUtils.forName(charset);
            } catch (Exception ignored) {
                throw e;
            }
        }
    }

    private static Charset charset(String charset) {
        try {
            return forName(charset);
        } catch (Exception e) {
            return null;
        }
    }

    private static final Evaluator charsetMetas = QueryParser
            .parse("meta[http-equiv=content-type], meta[charset]");

    static Charset htmlCharset(TextStatistics stats, Element root) {
        for (Element meta : Selector.select(charsetMetas, root)) {
            Charset foundCharset = correctVariant(stats, charset(meta.attr("charset")));
            if (foundCharset != null) {
                return foundCharset;
            }
            foundCharset = correctVariant(stats, contentTypeCharset(meta.attr("content")));
            if (foundCharset != null) {
                return foundCharset;
            }
        }
        return null;
    }


    private static final Pattern contentTypeCharsetPattern =
            Pattern.compile("(?i)\\bcharset\\s*=[\\s\"']*([^\\s,;\"']+)");

    static Charset contentTypeCharset(CharSequence contentType) {
        if (contentType == null)
            return null;
        Matcher m = contentTypeCharsetPattern.matcher(contentType);
        if (m.find()) {
            try {
                return forName(m.group(1));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static final Pattern xmlEncoding = Pattern.compile(
            "(?is)\\A\\s*<\\?\\s*xml\\s+[^<>]*encoding\\s*=\\s*(?:['\"]\\s*)?([-_:.a-z0-9]+)");

    static Charset xmlCharset(TextStatistics stats, CharSequence str) {
        Matcher matcher = xmlEncoding.matcher(str);
        if (matcher.find()) {
            return correctVariant(stats, charset(matcher.group(1)));
        } else {
            return null;
        }
    }


    //uncomment this handy function to print out invalid bytes for a charset
//    public static void main(String[] args) throws Exception {
//        String[] cs = {
//                "ISO-8859-15",
//                "windows-1252", "ISO-8859-1",
//                "windows-1250", "ISO-8859-2",
//                "windows-1253", "ISO-8859-7",
//                "windows-1255", "ISO-8859-8",
//                "windows-1254", "ISO-8859-9",
//                "windows-1251",
//                "windows-1256",
//                "ISO-8859-5",
//                "ISO-8859-6"
//        };
//
//        for (String name : cs) {
//            Charset c = EncodingUtils.forName(name);
//            if (c.newEncoder().maxBytesPerChar() > 1) {
//                throw new IllegalArgumentException("this method doesn't support " + c);
//            }
//            String line = java.util.stream.IntStream
//                    .range(0, 256)
//                    .filter(i -> new String(new byte[]{(byte) i}, c).getBytes(c)[0] != (byte)i)
//                    .mapToObj(i -> "0x" + Integer.toHexString(i).toUpperCase())
//                    .collect(java.util.stream.Collectors.joining(", ", "undefined " + name + " = {", "};"));
//
//            System.out.println(line);
//        }
//    }

}
