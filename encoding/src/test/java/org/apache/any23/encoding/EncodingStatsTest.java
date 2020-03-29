package org.apache.any23.encoding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.primes.Primes;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.InternalAny23Ext;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EncodingStatsTest {

    private static final String[] markedUpResources = {
            "/application/atom/atom.xml",
            "/application/hcalendar/test1",
            "/application/hcalendar/test2",
            "/application/messy_html/test1.html",
            "/application/rdfa/false.test",
            "/application/rdfa/london-gazette.html",
            "/application/rdfa/mic.xhtml",
            "/application/rdfa/test1.html",
            "/application/rdfxml/error.rdf",
            "/application/rdfxml/foaf",
            "/application/rdfxml/physics.owl",
            "/application/rdfxml/test1",
            "/application/rdfxml/test2",
            "/application/rdfxml/test3",
            "/application/rss1/test1",
            "/application/rss2/index.html",
            "/application/rss2/rss2sample.xml",
            "/application/rss2/test1",
            "/application/trix/test1.trx",
            "/application/xhtml/blank-file-header.xhtml",
            "/application/xhtml/index.html",
            "/application/xhtml/test1",
            "/application/xhtml2/xhtml2.xml",
            "/application/xoxo/test1",
            "/calendar/xml/rfc6321-example1.xml",
            "/calendar/xml/rfc6321-example2.xml",
            "/cli/basic-with-stylesheet.html",
            "/html/mixed/01-xfn-foaf.html",
            "/html/rdfa/0087.xhtml",
            "/html/rdfa/ansa_2010-02-26_12645863.html",
            "/html/rdfa/attribute-already-specified.html",
            "/html/rdfa/base-handling.html",
            "/html/rdfa/base-handling.xhtml",
            "/html/rdfa/basic.html",
            "/html/rdfa/basic-with-errors.html",
            "/html/rdfa/drupal-test-frontpage.html",
            "/html/rdfa/goodrelations-rdfa10.html",
            "/html/rdfa/goodrelations-rdfa11.html",
            "/html/rdfa/incomplete-triples.html",
            "/html/rdfa/invalid-xml-character.html",
            "/html/rdfa/null-datatype-test.html",
            "/html/rdfa/object-resource-test.html",
            "/html/rdfa/opengraph-music-song-object-type.html",
            "/html/rdfa/opengraph-structured-properties.html",
            "/html/rdfa/oreilly-invalid-datatype.html",
            "/html/rdfa/rdfa-11-curies.html",
            "/html/rdfa/rdfa-issue186-1.xhtml",
            "/html/rdfa/rdfa-issue186-2.xhtml",
            "/html/rdfa/rdfa-issue227.html",
            "/html/rdfa/rdfa-issue268-and-317.html",
            "/html/rdfa/rdfa-issue271-and-317.html",
            "/html/rdfa/rdfa-issue273-and-317.html",
            "/html/rdfa/rdfa-issue326-and-267.html",
            "/html/rdfa/rel-href.html",
            "/html/rdfa/rel-rev.html",
            "/html/rdfa/vocab.html",
            "/html/rdfa/xmlliteral-datatype-test.html",
            "/html/BBC_News_Scotland.html",
            "/html/default-language.html",
            "/html/empty-span-broken.html",
            "/html/empty-span-works.html",
            "/html/encoding-test.html",
            "/html/html-body-embedded-jsonld-extractor.html",
            "/html/html-embedded-jsonld-extractor.html",
            "/html/html-embedded-jsonld-extractor-multiple.html",
            "/html/html-head-and-body-embedded-jsonld-extractor.html",
            "/html/html-head-link-extractor.html",
            "/html/html-head-meta-extractor.html",
            "/html/html-head-meta-extractor-with-mozilla-extensions.html",
            "/html/html-jsonld-bad-character.html",
            "/html/html-jsonld-commas.html",
            "/html/html-jsonld-fatal-error.html",
            "/html/html-jsonld-strip-comments.html",
            "/html/html-jsonld-unescaped-characters.html",
            "/html/html-turtle.html",
            "/html/html-without-uf.html",
            "/html/rff-test.html",
            "/microdata/5.2.1-non-normative-example-1.html",
            "/microdata/example2.html",
            "/microdata/5.2.1-non-normative-example-2.html",
            "/microdata/microdata-bad-types.html",
            "/microdata/microdata-bad-properties.html",
            "/microdata/example5.html",
            "/microdata/microdata-basic.html",
            "/microdata/microdata-itemref.html",
            "/microdata/microdata-missing-scheme.html",
            "/microdata/microdata-nested.html",
            "/microdata/microdata-nested-url-resolving.html",
            "/microdata/microdata-richsnippet.html",
            "/microdata/schemaorg-example-1.html",
            "/microdata/schemaorg-example-2.html",
            "/microdata/tel-test.html",
            "/microdata/unused-itemprop.html",
            "/microformats/xfn/encoding-utf-8.html",
            "/microformats/hreview/05-spec.html",
            "/microformats/hcalendar/example5.4.html",
            "/microformats/nested-microformats-a2.html",
            "/microformats/hcalendar/example2.html",
            "/microformats/hreview/03-spec-3.html",
            "/microformats/hcalendar/09-component-vevent-summary-in-img-alt.html",
            "/microformats/hcard/12-img-src-url.html",
            "/microformats/hreview/04-spec-4.html",
            "/microformats/hlisting/multiple-actions.html",
            "/microformats/hcalendar/18-component-vevent-uid.html",
            "/microformats/hlisting/actions-lister-fn-tel.html",
            "/microformats/hcard/16-honorific-additional-multiple.html",
            "/microformats/hlisting/item-fn-photo-href.html",
            "/microformats/hcalendar/13-component-vevent-summary-url-property.html",
            "/microformats/hcard/03-implied-n.html",
            "/microformats/hcard/13-photo-logo.html",
            "/microformats/xfn/with-relative-uri.html",
            "/microformats/hcard/17-email-not-uri.html",
            "/microformats/hlisting/kelkoo-full.html",
            "/microformats/hlisting/price.html",
            "/microformats/xfn/strip-spaces.html",
            "/microformats/hlisting/item-fn-url.html",
            "/microformats/hcalendar/example3.html",
            "/microformats/hcard/38-uid.html",
            "/microformats/hcard/31-include.html",
            "/microformats/hcalendar/03-component-vevent-dtend-date.html",
            "/microformats/microformat-domains.html",
            "/microformats/hresume/ant.html",
            "/microformats/hlisting/empty.html",
            "/microformats/hcard/33-area.html",
            "/microformats/hcard/21-tel.html",
            "/microformats/license/multiple.html",
            "/microformats/hreview/01-spec.html",
            "/microformats/license/empty.html",
            "/microformats/hlisting/actions-lister-url.html",
            "/microformats/hlisting/single-action.html",
            "/microformats/hlisting/actions-lister-email.html",
            "/microformats/hcalendar/02-component-vevent-dtstart-datetime.html",
            "/microformats/hcalendar/08-component-vevent-multiple-classes.html",
            "/microformats/license/apache.html",
            "/microformats/hcalendar/01-component-vevent-dtstart-date.html",
            "/microformats/hcalendar/example5.3.html",
            "/microformats/hcalendar/10-component-vevent-entity.html",
            "/microformats/hcard/05-mailto-1.html",
            "/microformats/hcard/41-ignore-children.html",
            "/microformats/xfn/encoding-iso-8859-1.html",
            "/microformats/hcard/06-mailto-2.html",
            "/microformats/hlisting/description.html",
            "/microformats/hcalendar/16-calendar-force-outlook.html",
            "/microformats/hresume/steveganz.html",
            "/microformats/nested-microformats-managed.html",
            "/microformats/hcard/lastfm-adr-multi-address.html",
            "/microformats/hcard/30-fn-org.html",
            "/microformats/hcard/40-fn-inside-adr.html",
            "/microformats/hcard/02-multiple-class-names-on-vcard.html",
            "/microformats/hcard/25-geo-abbr.html",
            "/microformats/hlisting/multiple-actions-nested.html",
            "/microformats/hcard/14-img-src-data-url.html",
            "/microformats/license/multiple-mixed-case.html",
            "/microformats/xfn/encoding-iso-8859-1.xhtml",
            "/microformats/hcard/04-ignore-unknowns.html",
            "/microformats/hcard/36-categories.html",
            "/microformats/xfn/no-rel.html",
            "/microformats/hcard/07-relative-url.html",
            "/microformats/hcard/26-ancestors.html",
            "/microformats/hcard/23-abbr-title-everything.html",
            "/microformats/hcalendar/example1.html",
            "/microformats/xfn/multiple-friends.html",
            "/microformats/hcard/01-tantek-basic.html",
            "/microformats/hcard/18-object-data-http-uri.html",
            "/microformats/xfn/mixed-case.html",
            "/microformats/hcalendar/19-attachments.html",
            "/microformats/xfn/simple-me.html",
            "/microformats/hcard/27-bday-date.html",
            "/microformats/xfn/simple-friend.html",
            "/microformats/nested-microformats-a1.html",
            "/microformats/hcard/infinite-loop.html",
            "/microformats/hcalendar/15-calendar-xml-lang.html",
            "/microformats/hcard/10-relative-url-xmlbase-2.html",
            "/microformats/hcard/null-pointer.html",
            "/microformats/hreview/02-spec-2.html",
            "/microformats/license/ccBy.html",
            "/microformats/nested-microformats-a3.html",
            "/microformats/hcard/09-relative-url-xmlbase-1.html",
            "/microformats/hcalendar/11-component-vevent-summary-in-subelements.html",
            "/microformats/hcard/29-bday-datetime-timezone.html",
            "/microformats/hlisting/description-complex.html",
            "/microformats/license/multiple-empty-href.html",
            "/microformats/hcalendar/07-component-vevent-description-simple.html",
            "/microformats/hcard/35-include-pattern.html",
            "/microformats/xfn/multiple-rel.html",
            "/microformats/hlisting/actions-lister-tel.html",
            "/microformats/hrecipe/01-spec.html",
            "/microformats/hcard/28-bday-datetime.html",
            "/microformats/species/species-example-2.html",
            "/microformats/hcard/34-notes.html",
            "/microformats/hcard/linkedin-michelemostarda.html",
            "/microformats/xfn/tagsoup.html",
            "/microformats/hcard/37-singleton.html",
            "/microformats/xfn/no-valid-rel.html",
            "/microformats/hcard/20-image-alt.html",
            "/microformats/hlisting/summary-bookmark.html",
            "/microformats/hlisting/full.html",
            "/microformats/hcalendar/example5.html",
            "/microformats/hcalendar/example5.5.html",
            "/microformats/hlisting/actions-lister-email-href.html",
            "/microformats/hcard/19-object-data-data-uri.html",
            "/microformats/hcalendar/04-component-vevent-dtend-datetime.html",
            "/microformats/hlisting/actions-lister.html",
            "/microformats/hcalendar/05-calendar-simple.html",
            "/microformats/hlisting/item-fn.html",
            "/microformats/hlisting/dtlisted-dtexpired.html",
            "/microformats/hcalendar/06-component-vevent-uri-relative.html",
            "/microformats/hlisting/kelkoo.html",
            "/microformats/hcard/32-header.html",
            "/microformats/hlisting/single-action-outside.html",
            "/microformats/hcard/performance.html",
            "/microformats/hlisting/item-fn-url-photo-img.html",
            "/microformats/xfn/me-and-sweetheart.html",
            "/microformats/hcalendar/empty-statcvs.html",
            "/microformats/hcard/08-relative-url-base.html",
            "/microformats/hcalendar/12-component-vevent-summary-url-in-same-class.html",
            "/microformats/xfn/upcase-href.html",
            "/microformats/hlisting/summary.html",
            "/microformats/xfn/encoding-utf-8-after-title.html",
            "/microformats/hcard/22-adr.html",
            "/microformats/hlisting/adr.html",
            "/microformats/xfn/some-links-without-rel.html",
            "/microformats/hcalendar/17-component-vevent-description-value-in-subelements.html",
            "/microformats/hcard/11-multiple-urls.html",
            "/microformats/hlisting/actions-lister-fn.html",
            "/microformats/xfn/encoding-utf-8.xhtml",
            "/microformats/hcard/15-honorific-additional-single.html",
            "/microformats/hcalendar/example5.6.html",
            "/microformats/hlisting/item.html",
            "/microformats2/h-adr/h-adr-test.html",
            "/microformats2/h-card/h-card-test.html",
            "/microformats2/h-entry/h-entry-test.html",
            "/microformats2/h-event/h-event-test.html",
            "/microformats2/h-geo/h-geo-test.html",
            "/microformats2/h-item/h-item-test.html",
            "/microformats2/h-product/h-product-test.html",
            "/microformats2/h-recipe/h-recipe-test.html",
            "/microformats2/h-resume/h-resume-test.html",
            "/org/apache/any23/extractor/openie/example-openie.html",
            "/org/apache/any23/extractor/rdf/embedded_json-ld.html",
            "/org/apache/any23/extractor/xpath/xpathextractor-test.html",
            "/org/apache/any23/validator/invalid-rdfa-about.html",
            "/org/apache/any23/validator/meta-name-misuse.html",
            "/org/apache/any23/validator/microdata-basic.html",
            "/org/apache/any23/validator/missing-og-namespace.html",
            "/org/apache/any23/any23-xml-mimetype.xml",
            "/text/html/test1"
};

    private static final String[] plaintextResources = {
            "/application/nquads/test1.nq",
            "/application/nquads/test2.nq",
            "/application/rdfn3/test1",
            "/application/rdfn3/test2",
            "/application/rdfn3/test3",
            "/application/turtle/geolinkeddata.ttl",
            "/application/turtle/test1",
            "/application/turtle/test2",
            "/application/turtle/test3",
            "/application/wsdl/error.wsdl",
            "/html/rdfa/goodrelations-rdfa10-expected.nq",

            "/calendar/json/rfc7265-example2.json",
            "/calendar/text/rfc5545-example6.ics",
            "/calendar/text/rfc5545-example5.ics",

            "/calendar/text/example2-external-timezone-expected.nquads",
            "/calendar/text/rfc5545-example4.ics",
            "/calendar/json/rfc7265-example2-expected.nquads",
            "/calendar/text/rfc5545-example6-expected.nquads",
            "/calendar/json/rfc7265-example1-expected.nquads",
            "/calendar/text/rfc5545-example2-expected.nquads",
            "/calendar/text/rfc5545-example5-expected.nquads",
            "/calendar/text/example2-bad-timezone-expected.nquads",
            "/calendar/text/rfc5545-example1-expected.nquads",
            "/calendar/text/example2-external-timezone.ics",
            "/calendar/json/rfc7265-example1.json",
            "/calendar/text/example2-bad-timezone.ics",
            "/calendar/text/rfc5545-example3.ics",
            "/calendar/text/rfc5545-example3-expected.nquads",
            "/calendar/text/rfc5545-example1.ics",
            "/calendar/text/rfc5545-example4-expected.nquads",
            "/calendar/text/rfc5545-example2.ics",
            "/calendar/xml/rfc6321-example1-expected.nquads",
            "/calendar/xml/rfc6321-example2-expected.nquads",
            "/cli/rover-test1.nq",
            "/microdata/5.2.1-non-normative-example-1-expected.nquads",
            "/microdata/5.2.1-non-normative-example-2-expected.nquads",
            "/microdata/microdata-bad-properties-expected.nquads",
            "/microdata/microdata-bad-types-expected.nquads",
            "/microdata/microdata-basic-expected.properties",
            "/microdata/microdata-itemref-expected.properties",
            "/microdata/microdata-json-serialization.json",
            "/microdata/microdata-nested-expected.nquads",
            "/microdata/microdata-nested-expected.properties",
            "/microdata/microdata-nested-url-resolving-expected.nquads",
            "/microdata/microdata-richsnippet-expected.nquads",
            "/microdata/schemaorg-example-1-expected.nquads",
            "/microdata/schemaorg-example-2-expected.nquads",
            "/org/apache/any23/extractor/csv/test-comma.csv",
            "/org/apache/any23/extractor/csv/test-missing.csv",
            "/org/apache/any23/extractor/csv/test-semicolon.csv",
            "/org/apache/any23/extractor/csv/test-tab.csv",
            "/org/apache/any23/extractor/csv/test-type.csv",
            "/org/apache/any23/extractor/rdf/place-example.jsonld",
            "/org/apache/any23/extractor/rdf/testMalformedLiteral",
            "/org/apache/any23/extractor/yaml/different-float.yml",
            "/org/apache/any23/extractor/yaml/different-integers.yml",
            "/org/apache/any23/extractor/yaml/multi-test.yml",
            "/org/apache/any23/extractor/yaml/simple-312.yml",
            "/org/apache/any23/extractor/yaml/simple-load.yml",
            "/org/apache/any23/extractor/yaml/simple-load_no_head.yml",
            "/org/apache/any23/extractor/yaml/simple-load_yaml.yaml",
            "/org/apache/any23/extractor/yaml/test-null.yml",
            "/org/apache/any23/extractor/yaml/tree.yml",
            "/org/apache/any23/extractor/yaml/tree2.yml",
            "/rdf/issue415.txt",
            "/rdf/issue415-valid.txt",
            "/rdf/rdf-issue183.ttl",
            "/text/owl-functional/example-functionalsyntax.ofn",
            "/text/owl-manchester/example-manchestersyntax.omn"
    };

    @Test
    public void test() throws Exception {
        System.out.println("MARKUP");
        HashMap<String, EncodingStats.Stats> map = new HashMap<>();
        for (String resource : markedUpResources) {
            //System.out.println("STARTING " + resource);
            byte[] bytes = IOUtils.resourceToByteArray(resource);
            StringBuilder txt = new StringBuilder();
            EncodingStats stats = new EncodingStats(new ByteArrayInputStream(bytes), txt);
            EncodingStats.Stats s = stats.detectEncoding();
            map.put(resource, s);
        }

        System.out.println("max neg: " + map.entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().negPercent)).get());
        System.out.println("min pos: " + map.entrySet().stream().min(Comparator.comparingDouble(e -> e.getValue().posPercent)).get());
        System.out.println("max neg wilson: " + map.entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().negWilson)).get());
        System.out.println("min pos wilson: " + map.entrySet().stream().min(Comparator.comparingDouble(e -> e.getValue().posWilson)).get());


        System.out.println();
        System.out.println("PLAINTEXT");

        map.clear();

        for (String resource : plaintextResources) {
            //System.out.println("STARTING " + resource);
            byte[] bytes = IOUtils.resourceToByteArray(resource);
            StringBuilder txt = new StringBuilder();
            EncodingStats stats = new EncodingStats(new ByteArrayInputStream(bytes), txt);
            EncodingStats.Stats s = stats.detectEncoding();
            map.put(resource, s);
        }

        System.out.println("min neg: " + map.entrySet().stream().min(Comparator.comparingDouble(e -> Double.isNaN(e.getValue().negPercent) ? 1 : e.getValue().negPercent)).get());
        System.out.println("max pos: " + map.entrySet().stream().max(Comparator.comparingDouble(e -> Double.isNaN(e.getValue().posPercent) ? 0 : e.getValue().posPercent)).get());
        System.out.println("min neg wilson: " + map.entrySet().stream().min(Comparator.comparingDouble(e -> e.getValue().negWilson)).get());
        System.out.println("max pos wilson: " + map.entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().posWilson)).get());

    }

    private static String norm(String str) {
        return str.replaceAll("[\\p{IsWhitespace}\\p{IsControl}\u00AD]", "");
    }

    @Test
    public void testText() throws Exception {
        for (String resource : markedUpResources) {
            //System.out.println("STARTING " + resource);

            // weird jsoup issue for document.text(): <h2>PLUS DE 15 ANS : OCÉANE LEBOT</h2> becomes: "PLUS DE 15 ANS : OCÉANE LEBO" in /html/rdfa/invalid-xml-character.html

            byte[] bytes = IOUtils.resourceToByteArray(resource);
            StringBuilder txt = new StringBuilder();
            EncodingStats stats = new EncodingStats(new ByteArrayInputStream(bytes), txt);
            EncodingStats.Stats s = stats.detectEncoding();
            String str = norm(txt.toString());

            Document doc = Parser.htmlParser().parseInput(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.ISO_8859_1)), "");
            doc.getAllElements().forEach(element -> {
                for (TextNode child : element.textNodes())
                    child.text(Parser.htmlParser().parseInput(child.text(), "").text());
            });
            String docStr = norm(doc.text());

            if (!str.equals(docStr)) {
                System.out.println(resource);
                System.out.println(str);
                System.out.println(docStr);
                System.out.println();
                int countPrint = 0;
                for (int i = 0; i < Math.min(str.length(), docStr.length()) && countPrint < 20; i++) {
                    if (str.charAt(i) != docStr.charAt(i)) {
                        //System.out.println((int)str.charAt(i) + " != " + (int)docStr.charAt(i));
                        countPrint++;
                    }
                }
            }
        }
    }

    private static void debugTokens(String resource) throws IOException {
        String content = IOUtils.resourceToString(resource, StandardCharsets.ISO_8859_1);

        ParseErrorList errors = ParseErrorList.tracking(Integer.MAX_VALUE);
        Tokeniser tk = new Tokeniser(new CharacterReader(new StringReader(content)), errors);
        Token t;
        int count = 0;
        while (!(t = tk.read()).isEOF() && count++ < 1000) {
            //System.out.println(t.type + ": " + t);
            if (t.isStartTag()) {
                //System.out.println("TAGNAME: " + t.asStartTag().tagName);
                t.asStartTag().getAttributes().asList().forEach(attr -> {
                    try {
                        //System.out.println("    '" + attr.getKey() + "'='" + attr.getValue() + "'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        System.out.println(errors);

        Element element = Jsoup.parse(content).getAllElements().stream().filter(e -> e.ownText().contains("LEBO")).findFirst().get();

        //System.out.println(element.parent());
        String text = element.parent().parent().parent().text().substring(347, 350);
        char[] chars = text.toCharArray();
        for (char ch : chars) {
            System.out.println((int)ch);
        }
        System.out.println(text);
        System.out.println(norm(text));
        chars = norm(text).toCharArray();
        for (char ch : chars) {
            System.out.println((int)ch);
        }
        System.out.println();
    }

    @Test
    public void debugPage() throws Exception {
        //String resource = "/html/rdfa/rdfa-issue268-and-317.html";
        //String resource = "/microdata/tel-test.html";
        String resource = "/html/rdfa/invalid-xml-character.html";
        debugTokens(resource);
    }

    @Test
    public void debugText() throws Exception {
        System.out.println(norm("PRIX DU PUBLIC MOINS DE 15 ANS : ARTHUR HOURCADE PLUS DE 15 ANS : BÃ\u0089RÃ\u0089NICE DAUTRY PRIX DES AUTEURS MOINS DE 15 ANS : DAVID DE ALMEIDA PLUS DE 15 ANS : FLIP (PHILIPPE MONCAN) PRIX DES COMMERÃ\u0087ANTS MOINS DE 15 ANS :MATTHIEU DOSNE PLUS DE 15 ANS : FABRICE PIERRON PRIX DE LA VILLE MOINS DE 15 ANS : VINCENT SZWANKOWSKI PLUS DE 15 ANS : OCÃ\u0089ANE LEBOT Date: 7 juin 2016/Author: bray2014/CatÃ©gorie: ActualitÃ©s, Culture Post navigation ← Compte rendu du Conseil Municipal du 11 mai 2016 Les gagnants du tirage au sort Festival BD →\n"));
    }

    @Test
    public void testLang() {
        for (Locale locale : Locale.getAvailableLocales()) {
            System.out.println(locale + " " + locale.getDisplayName());
        }
    }

    static class LangStats {
        private final String lang;
        private final double frac;
        private final HashMap<String, Double> charsetFracs = new HashMap<>();
        private final HashMap<String, Double> derivedCharsetFracs = new HashMap<>();
        private final HashMap<String, Double> derivedCharsetFracs2 = new HashMap<>();

        private static final double pre = 1000.0;

        public LangStats(String lang, double frac) {
            this.lang = lang;
            this.frac = frac;
        }

        double getOther() {
            double d = 1.0 - charsetFracs.values().stream().mapToDouble(x -> x).filter(x -> x == x).sum();
            //return Math.max(0, d);
            return d;
        }

        double getDerivedOther() {
            double d = 1.0 - derivedCharsetFracs.values().stream().mapToDouble(x -> x).filter(x -> x == x).sum();
            return Math.max(0, d);
        }

        double getDerived2Other() {
            double d = 1.0 - derivedCharsetFracs2.values().stream().mapToDouble(x -> x).filter(x -> x == x).sum();
            return Math.max(0, d);
        }

        void put(String charset, double frac) {
            if (Double.isNaN(frac) && charsetFracs.containsKey(charset)) {
                return;
            }
            Double old = charsetFracs.put(charset, frac);
            if (old != null && !old.isNaN() && !old.equals(frac)) {
                throw new AssertionError("cannot put both " + old + " and " + frac + " for charset " + charset);
            }
        }
        void putDerived(String charset, double frac) {
            derivedCharsetFracs.put(charset, frac);
        }
        void putDerived2(String charset, double frac) {
            derivedCharsetFracs2.put(charset, frac);
        }


        LangStats fix() {
            LangStats stats = new LangStats(lang, frac);
            stats.charsetFracs.putAll(charsetFracs);
            double sumOfRatios = 0;
            int countRatios = 0;
            for (String charset : derivedCharsetFracs.keySet()) {
                if ("UTF-8".equals(charset)) {
                    continue;
                }
                double existing = charsetFracs.getOrDefault(charset, Double.NaN);
                double derived = derivedCharsetFracs.getOrDefault(charset, Double.NaN);
                double ratio = existing / derived;
                if (Double.isFinite(ratio)) {
                    sumOfRatios += ratio;
                    countRatios++;
                }
            }
            double avgRatio = sumOfRatios / countRatios;
            System.out.println(lang + " avg ratio: " + avgRatio);
            if (Double.isNaN(avgRatio)) {
                avgRatio = 1;
            }

            for (String charset : derivedCharsetFracs.keySet()) {
                double existing = charsetFracs.getOrDefault(charset, Double.NaN);
                if (Double.isNaN(existing)) {
                    double bestGuess = avgRatio * derivedCharsetFracs.getOrDefault(charset, Double.NaN);
                    if (Double.isFinite(bestGuess)) {
                        stats.charsetFracs.put(charset, bestGuess);
                    }
                }
            }
            return stats;
        }



//        public LangStats fixed() {
//            LangStats newStats = new LangStats(lang, frac);
//            newStats.charsetFracs.putAll(charsetFracs);
//            double other = getOther();
//            // derivedCh / derivedTotal
//            // derivedCh / other =
//        }

        @Override
        public String toString() {
            String str = lang + ": " + p(frac) + "\n" + charsetFracs.entrySet().stream().sorted(Comparator.comparingDouble((Map.Entry<String, Double> e) -> Double.isNaN(e.getValue()) ? 0.000001 : e.getValue()).reversed()).map(e -> "    " + e.getKey() + ": " + p(e.getValue())).collect(Collectors.joining("\n"));
            str += "\n    Other: " + p(getOther());
            if (!derivedCharsetFracs.isEmpty()) {
                str += "\n (derived)\n" + derivedCharsetFracs.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed()).map(e -> "    " + e.getKey() + ": " + p(e.getValue())).collect(Collectors.joining("\n"));
                str += "\n    Other: " + p(getDerivedOther());
            }
            if (!derivedCharsetFracs2.isEmpty()) {
                str += "\n (derived 2)\n" + derivedCharsetFracs2.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed()).map(e -> "    " + e.getKey() + ": " + p(e.getValue())).collect(Collectors.joining("\n"));
                str += "\n    Other: " + p(getDerived2Other());
            }
            return str;
        }
    }

    static class Bar {
        public final String href;
        public final String name;
        public final double percent;
        public final double frac;
        public Bar(String name, String href, double percent) {
            this.name = name;
            this.href = href;
            this.percent = percent;
            this.frac = percent / 100;
        }
    }

    private static ArrayList<Bar> parseBars(String url) {
        Elements elements;
        try {
            elements = Jsoup.connect(url).get().select("table.bars tr:has(table.bar)");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ArrayList<Bar> list = new ArrayList<>();
        for (Element e : elements) {
            Elements els = e.select("> th");
            if (els.size() != 1) {
                throw new AssertionError();
            }
            String text = e.select(".bar").text().trim();
            double percent;
            if (text.equals("less than 0.1%")) {
                percent = Double.NaN;
            } else if (text.endsWith("%")) {
                text = text.substring(0, text.length() - 1);
                percent = Double.parseDouble(text);
            } else {
                throw new AssertionError(text);
            }
            Element el = els.get(0);
            if (el.children().size() == 1) {
                el = el.child(0);
            }
            list.add(new Bar(el.text(), el.absUrl("href"), percent));
        }
        return list;

    }

    private static String p(double d) {
        if (Double.isNaN(d)) {
            return "< 0.1%";
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(d * 100) + "%";
    }

    private static String p(Double d) {
        return d == null ? "null" : p(d.doubleValue());
    }

    private static Double mergeFrac(Double old, Double next) {
        // null -> any   =>   any
        // NaN -> NaN    =>   NaN
        // NaN -> a      =>   a or error
        // a   -> NaN    =>   a or error
        if (old == null) {
            return next;
        }
        if (next == null) {
            return old;
        }
        if (old.isNaN()) {
            if (next.isNaN() || next < 0.001) {
                return next;
            } else {
                throw new AssertionError(old + " -> " + next);
            }
        }
        if (next.isNaN()) {
            if (old >= 0.001) {
                throw new AssertionError(old + " -> " + next);
            }
            return old;
        }
        if (!next.equals(old)) {
            throw new AssertionError(old + " -> " + next);
        }
        return next;
    }

    static final class LangEnc implements Comparable<LangEnc> {

        final String lang, enc;
        final Double langFrac, encFrac;

        public LangEnc(String lang, String enc, Double langFrac, Double encFrac) {
            this.lang = lang;
            this.enc = enc;
            this.langFrac = langFrac;
            this.encFrac = encFrac;
        }

        static LangEnc of(String lang, String enc) {
            return new LangEnc(lang, enc, null, null);
        }

        LangEnc enc(Double frac) {
            return new LangEnc(lang, enc, langFrac, mergeFrac(encFrac, frac));
        }

        LangEnc lang(Double frac) {
            return new LangEnc(lang, enc, mergeFrac(langFrac, frac), encFrac);
        }

        LangEnc merge(LangEnc other) {
            if (!equals(other)) {
                throw new AssertionError();
            }
            return lang(other.langFrac).enc(other.encFrac);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LangEnc langEnc = (LangEnc) o;
            return Objects.equals(lang, langEnc.lang) &&
                    Objects.equals(enc, langEnc.enc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lang, enc);
        }

        @Override
        public String toString() {
            return (lang != null ? lang : "*") + "," + (enc != null ? enc : "*") + "=" + p(langFrac) + "," + p(encFrac);
        }

        @Override
        public int compareTo(LangEnc o) {
            int i = (lang == null ? "" : lang).compareTo(o.lang == null ? "" : o.lang);
            if (i != 0) {
                return i;
            }
            double me = encFrac == null ? -2 : encFrac.isNaN() ? -1 : encFrac;
            double that = o.encFrac == null ? -2 : o.encFrac.isNaN() ? -1 : o.encFrac;
            i = Double.compare(that, me);
            if (i != 0) {
                return i;
            }
            me = langFrac == null ? -2 : langFrac.isNaN() ? -1 : langFrac;
            that = o.langFrac == null ? -2 : o.langFrac.isNaN() ? -1 : o.langFrac;
            i = Double.compare(that, me);
            if (i != 0) {
                return i;
            }
            return (enc == null ? "" : enc).compareTo(o.enc == null ? "" : o.enc);
        }
    }

    static List<LangEnc> mergeLangEncs(Collection<LangEnc>... cc) {
        ArrayList<LangEnc> list = new ArrayList<>();
        for (Collection<LangEnc> c : cc) {
            for (LangEnc le : c) {
                int i = list.indexOf(le);
                if (i >= 0) {
                    list.set(i, list.get(i).merge(le));
                } else {
                    list.add(le);
                }
            }
        }
        return list;
    }

    @Test
    public void testAutodetecting() {
        for (Charset ch : Charset.availableCharsets().values()) {
            if (ch.newDecoder().isAutoDetecting()) {
                System.out.println(ch.name() + ": " + ch.isRegistered() + " " + ch.aliases());
            }
        }
    }

    private static boolean isSame(int b, Charset ch1, Charset ch2) {
        return new String(new byte[]{(byte)b}, ch1).equals(new String(new byte[]{(byte)b}, ch2));
    }
    // https://html.spec.whatwg.org/multipage/parsing.html#character-encodings
    // 12.2.3.3 Character encodings
    // User agents must support the encodings defined in Encoding, including, but not limited to,
    // UTF-8, ISO-8859-2, ISO-8859-7, ISO-8859-8, windows-874, windows-1250, windows-1251, windows-1252,
    // windows-1254, windows-1255, windows-1256, windows-1257, windows-1258, gb18030, Big5, ISO-2022-JP,
    // Shift_JIS, EUC-KR, UTF-16BE, UTF-16LE, and x-user-defined. User agents must not support other encodings.

    // Encoding: https://encoding.spec.whatwg.org/
    // as json: https://encoding.spec.whatwg.org/encodings.json
    // https://www.unicode.org/reports/tr22/tr22-8.html#Charset_Alias_Matching  vs. https://encoding.spec.whatwg.org/#concept-encoding-get
    // https://www.iana.org/assignments/character-sets/character-sets.xhtml
    // https://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html
    // https://github.com/servo/libparserutils/blob/master/build/Aliases
    // http://demo.icu-project.org/icu-bin/convexp    (from https://github.com/unicode-org/icu/blob/master/icu4c/source/data/mappings/convrtrs.txt )

    // also: https://github.com/apache/tika/blob/master/tika-parsers/src/main/java/org/apache/tika/parser/html/charsetdetector/CharsetAliases.java

    // TODO: look into Java's x-JISAutoDetect charset
    //  and compare with https://bugzilla.mozilla.org/show_bug.cgi?id=1543077 and https://github.com/hsivonen/shift_or_euc

    // TODO: should we replace ISO-8859-1 with x-user-defined for round-tripping?
    //       also, should we map a 0 to somewhere in the Private Area range to avoid replacement char? see: https://stackoverflow.com/a/7028108/2599133

    // https://medium.com/@wolfgarbe/1000x-faster-spelling-correction-algorithm-2012-8701fcd87a5f
    //

    // https://html.spec.whatwg.org/multipage/parsing.html#determining-the-character-encoding, step 8

    // charset-cleaning strategies:
    // Tika:
    //  -1. "[ \\\"]*([^ >,;\\\"]+).*" group(1)
    //     ie., trim spaces, backslashes, and quotes from beginning of string, then
    //          read up to first space, >, ',', ';', backslash, or quote
    //  0. ISO-8851-1 => ISO-8859-1; windows => windows-1252
    //  1. normalize by lowercasing + aliases defined by standard java Charset
    //  2. reject "none" and "no"
    //  3. .*8859-X  ->   iso-8859-X
    //  4. cp-X      ->   cpX
    //  5. win-?X    ->   windows-X
    //  6. CharsetICU.forNameICU
    //     1. remove ",swaplfnl" from end of name if it exists, store as "options"
    //     2. getICUCanonicalName
    //        1. UConverterAlias.getCanonicalName(enc, "MIME")
    //            - strips all but letters & digits, strips leading zeros not preceded by digit
    //        2. UConverterAlias.getCanonicalName(enc, "IANA")
    //        3. UConverterAlias.getAlias(enc, 0)
    //        4. UConverterAlias.getAlias(enc.substring(2), 0) if enc.startsWith("x-"), else ""
    //     3. getCharset(icuCanonicalName, options)
    //  7. Charset.forName

    // what I should do is say:
    // 1. if no match, find index of last [^-_.:a-zA-Z0-9], make pivot
    // 2. rerun the algorithm on text before pivot, text after pivot
    // 3. return charset if found on only one side, or least specific one if found on both sides and they are in the same family


    // what if used symdelete strategy:
    //   charset-0: ISO88591 -> charset-1: SO88591, IO88591, IS88591, ISO8591, ISO8851
    //   charset-0: ISO885911  -> ......                            ISO88511,  ISO88591 (would be excluded as already exists in charset-0)
    // given: ISO88511
    //  1. EXACT MATCH: check charset table (not found)
    //  2. SUBSTITUTION: check charset-1 table for each possible removal (i.e., find substitutions) (would find ISO8851 -> ISO-8859-1) (FOUND)
    //  3. check

    // also:   silly  ->  spill
    // illy,slly,sily,*sill* -> pill,*sill*,spll,spil
    // so it's not just swap or substitution, it's adding a character in any position and removing in any position!
    // given: name-0
    //  1. NO CHANGE   : check the charset-0 table for name-0
    //  2. SUBSTITUTION OR SWAP: check the charset-1 table for each name-1 (this should absolutely come second priority as it covers 2 different types of errors)
    //  3. MISSING LETTER (?): check the charset-1 table for name-0
    //  3. EXTRA LETTER (?): check the charset-0 table for each name-1 (would come in handy for cases where extra - shouldn't have been inserted)
    //  should we even worry about character additions/removals?

    //                                                     -5       -6
    //                          ISO88591  charset-1 = ..., ISO8891, ISO8851
    //                                                  -5       -6
    // what about errors like:  ISO88951  name-1 = ..., ISO8851, ISO8891   (swap   = find  name-n in charset-(n+1) AND find name-(n+1) in charset-n)
    //                                                  -5       -6
    //                          ISO88511  name-1 =      ISO8811, ISO8851   (subst. = find  name-n in charset-n)
    // swap could also be implemented as
    //   given ABCDE, check charset table for:
    //   BACDE, ACBDE, ABDCE, ABCED

    // how to efficiently check substs? e.g. wimdows-1252   ANSWER: by adding ?indows1252, w?ndows1252, wi?dows1252, etc. to charset table
    // use ':' instead of '-' in table (to make switch less sparse)
    // only need to detect for common charsets... i.e., UTF-8, windows-125X, ISO-8859-X

    // we also need stuff like: windos-1252 -> windows-1252  (check charset - 1 table for name)


    // given: ISO88511, is it a substitution of ISO88591, or a deletion of ISO885911?
    //     should substitutions take priority over deletions?
    // hint:  ISO885911 is used by less than 0.0001% of webpages, ISO88591 is used by 2.8% (28000 times more likely)

    // https://www.iana.org/assignments/ianacharset-mib/ianacharset-mib

    // steps:
    //  1. normalize name
    //  2. find name in charset table
    //  3. find name with any swap in charset table
    //  4. find any subst in subst table

    // https://sci-hub.tw/10.1145/363958.363994
    // wrong letter: 567  = 58.8%
    // missing letter: 153   = 15.9%
    // transposition: 23     = 2.4%
    // extra letter: 99      = 10.3%
    // multiple error: 122   = 12.7%
    // total: 964

    private static boolean isUtf8(int len, byte[] bytes) {
        if (len != 4) {
            return false;
        }
        byte b0 = bytes[0];
        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];
        return ('U' == b0 || 'U' == b1) &&
                ('T' == b1 || 'T' == b0 || 'T' == b2) &&
                ('F' == b2 || 'F' == b1 || 'F' == b3) &&
                ('8' == b3 || '8' == b2);
    }

    enum CharsetGroup {
        latin1,
        latin2,
        EUC_KR,
        turkish,
        greek,
        hebrew,
        thai,
        baltic,
        shift_jis;
    }

    private static boolean isCharsetPrefix(byte b) {
        return b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z' || b >= '1' && b <= '9';
    }

    @Test
    public void testIANA() throws Exception {
        Document doc = Jsoup.connect("https://www.iana.org/assignments/character-sets/character-sets.xml").parser(Parser.xmlParser()).get();
        Elements elements = doc.select("registry#character-sets-1 > record");
        for (Element e : elements) {
            String name = e.selectFirst("name").text().trim();
            String preferredName = Optional.ofNullable(e.selectFirst("preferred_alias")).map(p -> p.text().trim()).orElse(name);
            Set<String> alts = Stream.concat(Stream.of(name), e.select("alias").stream().map(p -> p.text().trim())).collect(Collectors.toSet());
            System.out.println(preferredName + "  " + alts);
        }
    }

    // good function: 7713 * h   mod: 921  (with 2 arrays)
    private static int hashCode(String str, int multiplier1, int multiplier2, int mod) {
        char[] val = str.toCharArray();
        int h = 0;
        int i, len;
        final int arity = 2;
        for (i = 0, len = val.length / arity * arity; i < len; i += arity) {
            h = multiplier1 * h + val[i];
            h = multiplier2 * h + val[i + 1];
        }
        if (i < val.length) {
            h = multiplier1 * h + val[i++];
        }
        assert i == val.length;
        return h % mod;
    }

    private static String keep(String str, int... indexes) {
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
        byte[] newBytes = new byte[indexes.length];
        int pos = 0;
        for (int ind : indexes) {
            ind = ind >= 0 ? ind : (bytes.length + ind);
            if (ind < 0 || ind >= bytes.length) {
                continue;
            }
            newBytes[pos++] = bytes[ind];
        }
        //newBytes[pos++] = (byte)str.length();
        return new String(newBytes, 0, pos, StandardCharsets.ISO_8859_1);
    }

    @Test
    public void testNormalizeCharset() throws Exception {
        String[][] groups = {{
            "utf8", "UTF-8", "u.t.f-0008", "utf8", "x-utf-008"
        }, {
            "utf80", "utf-80"
        }, {
            "ut8", "ut8"
        }, {
            "isoir91", "iso-ir-91"
        }, {
            "isoir9-1", "iso-ir-9-1", "iso-ir-9-01"
        }, {
            "win1252", "win-1252", "win1252", "Windows-1252", "windows1252"
        }, {
            "greek", "IsoLatinGreek", "csIsoLatinGreek"
        }, {
            "l0", "IsoLatin0", "csisoLatin0", "latin0 "
        }, {
            "88591", "ISO-8859-1 ", "csISO88591"
        }, {
            "win31latin2", "csWindows31Latin2"
        }, {
            "88591windows3-0latin1", "ISO-8859-1-Windows-3.0-Latin-1"
        }};

        for (String[] strs : groups) {
            for (int i = 1; i < strs.length; i++) {
                Assert.assertEquals(strs[0], normalizeCharset(strs[i]));
            }
        }

        HashMap<String, String> aliasMapping = new HashMap<>();

        for (Charset ch : Charset.availableCharsets().values()) {
            List<String> keys = Stream.concat(ch.aliases().stream(), Stream.of(ch.name())).map(str -> normalizeCharset(str)).distinct().collect(Collectors.toList());
            //System.out.println(ch.name() + ": " + keys + " <- " + ch.aliases());
            for (String key : keys) {
                String old = aliasMapping.putIfAbsent(key, ch.name());
                if (old != null) {
                    System.out.println("DUPLICATE " + old + ", " + ch.name() + ": " + key);
                }
            }
        }

        int[] primes = new int[1000];

        for (int pos = 0; pos < primes.length; pos++) {
            int i = new Random().nextInt() & Integer.MAX_VALUE;
            primes[pos] = Primes.nextPrime(i);
        }
        Arrays.sort(primes);

        Document doc = Jsoup.connect("https://www.iana.org/assignments/character-sets/character-sets.xml").parser(Parser.xmlParser()).get();
        Elements elements = doc.select("registry#character-sets-1 > record");

        aliasMapping.clear();

        int mime = 0;
        int ianaAliases = 0;
        int mimeAliases = 0;
        for (Element e : elements) {
            String name = e.selectFirst("name").text().trim();
            String preferredAlias = Optional.ofNullable(e.selectFirst("preferred_alias")).map(p -> p.text().trim()).orElse(null);
            String preferredName = preferredAlias == null ? name : preferredAlias;

            List<String> aliases = e.select("alias").stream().map(p -> p.text().trim()).collect(Collectors.toList());
            ianaAliases += aliases.size();
            if (preferredAlias != null) {
                mime++;
                mimeAliases += aliases.size();
            }
            List<String> all = Stream.concat(aliases.stream(), Stream.of(name, preferredName)).collect(Collectors.toList());
            List<String> keys = all.stream().map(str -> normalizeCharset(str)).distinct().collect(Collectors.toList());
            for (String key : keys) {
                String old = aliasMapping.put(key, preferredName);
                if (old != null && !old.equals(preferredName)) {
                    System.out.println("DUPLICATE IANA " + old + ", " + preferredName + ": '" + key + "' " + all);
                }
            }
        }
        System.out.println("Number of IANA charsets: " + elements.size() + "; aliases: " + ianaAliases);
        System.out.println("Number of IANA charsets with MIME: " + mime + "; aliases: " + mimeAliases);
        System.out.println("Number of Java charsets: " + Charset.availableCharsets().size() +
                "; aliases: " + Charset.availableCharsets().values().stream().mapToInt(v -> v.aliases().size()).sum() +
                "; normalized aliases: " + Charset.availableCharsets().values().stream().flatMap(v -> v.aliases().stream().map(a -> normalizeCharset(a))).distinct().count());

        System.out.println();
        for (Charset ch : Charset.availableCharsets().values()) {
            String chNorm = normalizeCharset(ch.name());
            String chIana = aliasMapping.get(chNorm);
            Map<String, String> chAliasesToIana = ch.aliases().stream()
                    .filter(v -> aliasMapping.containsKey(normalizeCharset(v)))
                    .collect(Collectors.toMap(alias -> alias, alias -> aliasMapping.get(normalizeCharset(alias))));

            if (chIana == null && chAliasesToIana.values().stream().distinct().count() == 1) {
                System.out.println("WRONG NAME: " + ch.name() + " -> " + chIana + " : " + chAliasesToIana);
            } else if (chIana == null) {
                //if (!chAliasesToIana.isEmpty()) {
                    System.out.println("MISSING CHARSET: " + ch.name() + " : " + chAliasesToIana);
                //}
            } else if (!chIana.equals(ch.name())) {
                System.out.println("WRONG NAME: " + ch.name() + " -> " + chIana + " : " + ch.aliases().stream().map(alias -> alias + " -> " + aliasMapping.get(normalizeCharset(alias))).collect(Collectors.toList()));
            } else if (chAliasesToIana.values().stream().anyMatch(v -> !chIana.equals(v))) {
                System.out.println("MISMATCHED ALIAS: " + chIana + " : " + chAliasesToIana.entrySet().stream().collect(Collectors.toMap(e -> e.getValue(), e -> Collections.singletonList(e.getKey()), (a, b) -> new ArrayList<String>() {{addAll(a);addAll(b);}})));
            } else if (ch.aliases().stream().anyMatch(alias -> aliasMapping.get(normalizeCharset(alias)) == null)) {
                //System.out.println("Add alias for " + chIana + " : " + ch.aliases().stream().filter(alias -> aliasMapping.get(normalizeCharset(alias)) == null).collect(Collectors.toList()));
            }

            // TODO: just autogenerate a giant switch... that's essentially O(1), zero memory


//            List<String> allAliases = Stream.concat(Stream.of(ch.name()), ch.aliases().stream()).collect(Collectors.toList());
//            for (String alias : allAliases) {
//                String norm = normalizeCharset(alias);
//                String iana = aliasMapping.get(norm);
//                if (!ch.name().equals(iana)) {
//                    System.out.println("Java '" + alias + "' (" + norm + ") is mapped to: " + iana + " when it should be mapped to " + ch.name() + " (which is mapped to " + aliasMapping.get(normalizeCharset(ch.name())) + ")");
//                }
//            }

        }

        //Charset.availableCharsets().values().stream().flatMap(c -> Stream.concat(Stream.of(c.name()), c.aliases().stream()).map(n -> normalizeCharset(n))).distinct().sorted().forEachOrdered(System.out::println);


        //8 (= 2 * 2 * 2)


//        int minMod = Integer.MAX_VALUE;
//        for (int mod = Integer.MAX_VALUE / 2;; mod++) {
//            outer: for (int m1 : primes) {
//                for (int m2 : primes) {
//                    final int[] countDupes = {0};
//                    final int mult1 = m1;
//                    final int mult2 = m2;
//                    final int mod0 = mod;
//                    aliasMapping.entrySet().stream().collect(Collectors.toMap(e0 -> hashCode(e0.getKey(), mult1, mult2, mod0), e0 -> Collections.singleton(e0.getValue()), (v1, v2) -> {
//                        HashSet<String> set = new HashSet<>(v1);
//                        set.addAll(v2);
//                        if (set.size() > 2) {
//                            countDupes[0]++;
//                        }
//                        return set;
//                    }));
//
//                    if (countDupes[0] == 0) {
//                        System.out.println("0 dupes at: " + mult1 + ", " + mult2 + ", mod: " + mod + " (" + countDupes[0] + " dupes)");
//                        mod = mod * 2 / 3;
//                        break outer;
//                    }
//
////                    if (countDupes[0] < minDupes || countDupes[0] == 0) {
////                        System.out.println("better multiplier: " + mult1 + ", " + mult2 + ", mod: " + mod + " (" + countDupes[0] + " dupes)");
////                        minDupes = countDupes[0];
////                    }
//                }
//            }
//        }
    }

    static String normalizeCharset(String charset) {
        byte[] bytes = charset.getBytes(StandardCharsets.ISO_8859_1);
        int newLen = normalizeCharset(bytes, bytes.length);
        return new String(bytes, 0, newLen, StandardCharsets.US_ASCII);
    }

    private static boolean isDigit(byte b) {
        return b <= '9' && b >= '0';
    }

    private static boolean startsWith_window(byte[] b) {
        return b[0] == 'w' && b[1] == 'i' && b[2] == 'n'
                && b[3] == 'd' && b[4] == 'o' && b[5] == 'w';
    }

    private static boolean startsWith_latin(byte[] b) {
        return b[0] == 'l' && b[1] == 'a' && b[2] == 't' && b[3] == 'i' && b[4] == 'n';
    }

    private static boolean startsWith_isolatin(byte[] b) {
        return startsWith_iso(b) && b[3] == 'l' && b[4] == 'a' && b[5] == 't' && b[6] == 'i' && b[7] == 'n';
    }

    private static boolean startsWith_iso(byte[] b) {
        return b[0] == 'i' && b[1] == 's' && b[2] == 'o';
    }

    private static int normalizeCharset(byte[] charset, int len) {
        int newLen = 0;
        boolean dashBeforeDigit = false;
        for (int i = 0; i < len; i++) {
            byte b = charset[i];
            b ^= 0x20;
            if (b >= 'a' && b <= 'z' || (b ^= 0x20) >= 'a' && b <= 'z') {
                if (b == 's' && (newLen == 6 && startsWith_window(charset)
                        || newLen == 1 && charset[0] == 'c')) {
                    newLen >>= 1;
                } else if (newLen == 8 && startsWith_isolatin(charset)) {
                    charset[0] = b;
                    newLen = 1;
                } else {
                    charset[newLen++] = b;
                }
                dashBeforeDigit = false;
            } else if (isDigit(b)) {
                if (newLen == 3 && startsWith_iso(charset)) {
                    charset[0] = b;
                    newLen = 1;
                } else if (newLen == 5 && startsWith_latin(charset)) {
                    charset[1] = b;
                    newLen = 2;
                } else if (newLen == 8 && startsWith_isolatin(charset)) {
                    charset[0] = 'l';
                    charset[1] = b;
                    newLen = 2;
                } else if (newLen > 0 && charset[newLen - 1] == '0' && (newLen == 1 || !isDigit(charset[newLen - 2]))) {
                    charset[newLen - 1] = b;
                } else {
                    if (dashBeforeDigit) {
                        charset[newLen++] = '-';
                    }
                    charset[newLen++] = b;
                }
                dashBeforeDigit = false;
            } else if (b == '-' && newLen == 1 && charset[0] == 'x') {
                newLen = 0;
            } else if (newLen > 0 && isDigit(charset[newLen - 1]) && (newLen == 1 || !isDigit(charset[newLen - 2]))) { // [^0-9] [0-9] [^0-9a-zA-Z]
                dashBeforeDigit = true;
            }
        }
        int last = newLen - 1;
        return last >= 0 && charset[last] == '-' ? last : newLen;
    }

    private enum CommonCharset {
        UTF_8(94.2),
        ISO_8859_1(2.8, CharsetGroup.latin1),
        windows_1251(0.9), // cyrillic
        windows_1252(0.54, CharsetGroup.latin1),
        Shift$JIS(0.33, CharsetGroup.shift_jis),
        GB2312(0.26),
        EUC_KR(0.23, CharsetGroup.EUC_KR),
        EUC_JP(0.11),
        ISO_8859_2(0.11, CharsetGroup.latin2),
        windows_1250(0.1, CharsetGroup.latin2), // note: windows-1250 moves 15 characters into 80-9F range, fills gaps with other symbols
        GBK(0.1),
        Big5(0.074),
        ISO_8859_9(0.063, CharsetGroup.turkish), // turkish
        ISO_8859_15(0.06, CharsetGroup.latin1),
        windows_1256(0.047), // arabic
        windows_1254(0.046, CharsetGroup.turkish), // turkish
        x_windows_874(0.035, CharsetGroup.thai), //thai
        US_ASCII(0.028),
        windows_1255(0.018, CharsetGroup.hebrew), //hebrew; should basically just replace ISO-8859-8 and ISO-8859-8-I
        TIS_620(0.013, CharsetGroup.thai),
        ISO_8859_7(0.012, CharsetGroup.greek),
        windows_1253(0.011, CharsetGroup.greek),
        UTF_16(0.008),
        KOI8_R(0.005),
        // NOTE: KSC5601 is an alias for EUC_KR
        KSC5601(0.0041, CharsetGroup.EUC_KR),
        windows_1257(0.004, CharsetGroup.baltic),
        GB18030(0.0039),
        UTF_7(0.002), // UNSUPPORTED!!!
        windows_31j(0.0013, CharsetGroup.shift_jis),
        ISO_8859_5(0.0008), // cyrillic (not closely related to windows-1251)
        ISO_8859_8(0.00075, CharsetGroup.hebrew),
        ISO_8859_4(0.0007), // latin4 (north european)
        ANSI$X3_dot_110_1983(0.00036), // UNSUPPORTED!!!
        ISO_8859_6(0.00034),
        ISO_8859_13(0.00019, CharsetGroup.baltic),
        ISO_2022_JP(0.00018),
        KOI8_U(0.00018),
        // Just guesses, knowing descending, and absolute min must be 0.00001 (1 in 10 million):
        ISO_8859_16(0.00009), // UNSUPPORTED, because it's a REJECTED STANDARD!
        Big5_HKSCS(0.00008),
        ISO_8859_3(0.00007),
        x_windows_949(0.00006),
        ISO_8859_10(0.00005), // UNSUPPORTED (designed to cover Nordic languages, deemed of more use than ISO-8859-4, shares many common letters with ISO-8859-4)
        windows_1258(0.00004),
        x_iso_8859_11(0.00003, CharsetGroup.thai), // (only difference to TIS-620 is that NBSP is defined here at 0xA0)
        ISO_8859_14(0.00002), // UNSUPPORTED
        IBM850(0.00001);

        int outOfAMillion() {
            return (int)Math.round((percentage / 100) * 1_000_000);
        }

        @Override
        public String toString() {
            return name;
        }

        public final double percentage;
        public final String name;
        public final CharsetGroup group;
        CommonCharset(double percentage) {
            this(percentage, null);
        }
        CommonCharset(double percentage, CharsetGroup group) {
            this.percentage = percentage;
            this.name = name().replace("_dot_", ".").replace("__", " ").replace('_', '-').replace('$', '_');
            find(name);
            this.group = group;
//            try {
//                if (!Charset.isSupported(name)) {
//                    System.out.println("NOT SUPPORTED: " + name);
//                }
//            } catch (Exception e) {
//                System.out.println("NOT SUPPORTED: " + name);
//            }
        }

        private void find(String name) {
            try {
                String representativeName = Charset.forName(name).name();
                if (!name.equals(representativeName)) {
                    System.out.println(name + " SHOULD BE CHANGED TO " + representativeName);
                }
            } catch (Exception e) {
                Set<Integer> chars = name.toLowerCase().codePoints().boxed().collect(Collectors.toSet());
                Charset b = Charset.availableCharsets().values().stream().max(Comparator.comparingLong(c -> {
                            String best = Stream.concat(Stream.of(c.name()), c.aliases().stream()).max(Comparator.comparingLong(testName -> testName.toLowerCase().codePoints().filter(chars::contains).distinct().count())).get();
                            return best.codePoints().filter(chars::contains).count();
                        }
                )).get();
                System.out.println("UNSUPPORTED: " + name + "; closest match: " + b.name() + " " + b.aliases());
            }

        }
    }



    @Test
    public void testCommonCharsets() {
        for (Charset ch : Charset.availableCharsets().values()) {
            System.out.println(ch.name() + " " + ch.aliases());
        }
        for (CommonCharset ch : CommonCharset.values()) {
            System.out.println(ch + ": " + ch.outOfAMillion());
        }
    }

    @Test
    public void testNbsp() {
        String nbsp = "\u00A0";
        byte[] nbspByte = {(byte)0xa0};
        Map<String, List<CommonCharset>> allGroups = Stream.of(CommonCharset.values()).collect(Collectors.groupingBy(ch -> ch.group == null ? ch.name : ch.group.name()));
        Map<String, List<CommonCharset>> whitelist = allGroups.entrySet().stream().filter(e -> e.getValue().stream().mapToDouble(c -> c.percentage).sum() >= 0.01).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        System.out.println("whitelist: ");
        whitelist.entrySet().stream().sorted(Comparator.comparingDouble(e -> e.getValue().stream().mapToDouble(c -> c.percentage).sum())).forEach(e -> System.out.println(e.getKey() + ": " + e.getValue().stream().mapToDouble(c -> c.percentage).sum() + "% " + e.getValue()));
//        ArrayList<Charset> list = new ArrayList<>();
//        for (Charset ch : Charset.availableCharsets().values()) {
//            if (/*isSame(0xA0, ch, StandardCharsets.ISO_8859_1) &&*/ IntStream.range(0, 128).allMatch(i -> isSame(i, ch, StandardCharsets.ISO_8859_1))) {
//                list.add(ch);
//                //System.out.println(ch.name());
//            }
//        }
//        System.out.println(list);
//        System.out.println("total = " + list.size());
//        Map<Charset, List<Integer>> map = list.stream().collect(Collectors.toMap(ch -> ch, ch -> IntStream.range(128, 256).filter(i -> isSame(i, ch, StandardCharsets.ISO_8859_1)).boxed().collect(Collectors.toList())));
//        IntStream.range(128, 256).mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(Integer.toHexString(i), map.entrySet().stream().filter(e -> e.getValue().contains(i)).map(e -> e.getKey()).filter(ch -> !ch.name().startsWith("x-")).collect(Collectors.toList()))).sorted(Comparator.comparingLong(e -> e.getValue().size())).forEachOrdered(e -> System.out.println(e.getKey() + ": " + e.getValue().size() + " " + e.getValue()));
//        for (int i = 128; i < 256; i++) {
//            final int iconst = i;
//            long freq = ;
//            System.out.println(Integer.toHexString(i) + ": " + freq);
//        }

        //a0 (NBSP):  compatible with 21
        //ad (SHY):   compatible with 20 (not with TIS-620, but that is only 0.013% of the web)
        //a7 (section sign) & b0 (degree symbol): compatible with 19   (not with ISO-8859-5 (cyrillic) which is only 0.0008% of web, and TIS-620)
        //b7 (center dot): compatible with 17  (not with ISO-8859-4 (n.european, only 0.0007% of web) & ISO-8859-2 (e.european, 0.1% of web) (but in both of these it is only the inverted circumflex symbol, not likely to stand on its own), ISO-8859-5, TIS-620
        //a4 (currency symbol): compatible with 16 (not with ISO-8859-15 (euro sign) & ISO-8859-7 (greek, euro sign) & windows-1255 (heb, shekel sign), ISO-8859-5, TIS-620

    }

    private static String extractLangFromHref(String href) {
        Matcher m = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([^/]+)/all/all").matcher(href);
        return m.matches() ? m.group(1) : null;
    }

    private static String normalizeLang(String lang) {
        return lang.endsWith("-") ? lang.substring(0, lang.length() - 1) :  lang;
    }

    @Test
    public void gatherStats() throws Exception {
        Map<String, String> allLangs = Jsoup.connect("https://w3techs.com/technologies/overview/content_language/all").get()
                .select("a[href]")
                .stream()
                .map(e -> extractLangFromHref(e.absUrl("href")))
                .filter(Objects::nonNull)
                .collect(Collectors.toConcurrentMap(str -> str, str -> normalizeLang(str)));

        List<LangEnc> langEncBreakdown = allLangs.keySet().parallelStream().flatMap(lang -> {
            return parseBars("https://w3techs.com/technologies/breakdown/cl-" + lang + "/character_encoding").stream()
                    .map(bar -> LangEnc.of(allLangs.get(lang), "overall".equalsIgnoreCase(bar.name) ? null : bar.name).lang(bar.frac));
        }).collect(Collectors.toList());

        List<LangEnc> langEncSegmentation = allLangs.keySet().parallelStream().flatMap(lang -> {
            return parseBars("https://w3techs.com/technologies/segmentation/cl-" + lang + "/character_encoding").stream()
                    .map(bar -> LangEnc.of(allLangs.get(lang), bar.name).enc(bar.frac));
        }).collect(Collectors.toList());

        Map<String, String> allEncs = Jsoup.connect("https://w3techs.com/technologies/overview/character_encoding/all").get()
                .select("a[href]")
                .stream()
                .map(e -> {
                    String href = e.absUrl("href");
                    Matcher m = Pattern.compile("https://w3techs.com/technologies/details/en-([^/]+)/all/all").matcher(href);
                    return m.matches() ? new AbstractMap.SimpleImmutableEntry<>(m.group(1), e.text().trim()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> {
                    if (!v1.equals(v2)) {
                        throw new AssertionError(v1 + "!=" + v2);
                    } else {
                        return v1;
                    }
                }));

        // https://w3techs.com/technologies/details/cl-en-/all/all

        List<LangEnc> encLangBreakdown = allEncs.keySet().parallelStream().flatMap(enc -> {
            return parseBars("https://w3techs.com/technologies/breakdown/en-" + enc + "/content_language").stream()
                    .map(bar -> LangEnc.of("overall".equalsIgnoreCase(bar.name) ? null : normalizeLang(extractLangFromHref(bar.href)), allEncs.get(enc)).enc(bar.frac));
        }).collect(Collectors.toList());

        List<LangEnc> encLangSegmentation = allEncs.keySet().parallelStream().flatMap(enc -> {
            return parseBars("https://w3techs.com/technologies/segmentation/en-" + enc + "/content_language").stream()
                    .map(bar -> LangEnc.of(normalizeLang(extractLangFromHref(bar.href)), allEncs.get(enc)).lang(bar.frac));
        }).collect(Collectors.toList());

        List<LangEnc> merged = mergeLangEncs(langEncBreakdown, langEncSegmentation, encLangBreakdown, encLangSegmentation);

        System.out.println(merged.stream()
                //.filter(e -> e.langFrac != null && !e.langFrac.isNaN() && e.langFrac != 0 && e.encFrac != null && !e.encFrac.isNaN() && e.encFrac != 0)
                .sorted().map(e -> e.toString()).collect(Collectors.joining("\n")));

    }

    @Test
    public void printTLDUsages() throws Exception {
        HashMap<String, Double> charsets = new HashMap<>();
        ArrayList<LangStats> stats = new ArrayList<>();

        // PRIMARY: content language => encoding segmentation
        ArrayList<Bar> langBars = parseBars("https://w3techs.com/technologies/overview/top_level_domain/all");
        for (Bar bar : langBars) {
            Matcher m = Pattern.compile("https://w3techs\\.com/technologies/details/tld-([-a-zA-Z0-9]+)-/all/all").matcher(bar.href);
            if (m.matches()) {
                String lang = m.group(1);
                LangStats ls = new LangStats(lang, bar.frac);
                stats.add(ls);
                ArrayList<Bar> encBars = parseBars("https://w3techs.com/technologies/segmentation/tld-" + lang + "-/character_encoding");
                for (Bar encBar : encBars) {
                    charsets.merge(encBar.name, encBar.frac, Math::max);
                    ls.put(encBar.name, encBar.frac);
                }
            } else {
                System.out.println("NO MATCH FOUND FOR " + bar.href);
            }
            //System.out.println(ls);
        }

//        ArrayList<Bar> encBars = parseBars("https://w3techs.com/technologies/overview/character_encoding/all");
//        for (Bar encBar : encBars) {
//            //System.out.println(encBar.name + ": " + encBar.href + " " + encBar.percent);
//            Matcher m = Pattern.compile("https://w3techs\\.com/technologies/details/([^/]+)/all/all").matcher(encBar.href);
//            m.matches();
//
//            String enc = encBar.name;
//            double encFrac = encBar.frac;
//            String encLangRef = m.group(1);
//
//            ArrayList<Bar> encLangBars = parseBars("https://w3techs.com/technologies/segmentation/" + encLangRef + "/content_language");
//            for (Bar encLang : encLangBars) {
//                Matcher langMatcher = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([a-zA-Z]+)-/all/all").matcher(encLang.href);
//                langMatcher.matches();
//                String lang = langMatcher.group(1);
//                out:
//                {
//                    for (LangStats langStat : stats) {
//                        if (langStat.lang.equals(lang)) {
//                            // % of known lang, enc E sites with at least one lang L page   *    % of known enc sites with at least one enc E page / % of known lang sites with at least one lang L page
//                            // we really need extra multiplier: * % of all sites with known enc / % of all sites with known lang = known enc / known lang
//                            if (!langStat.charsetFracs.containsKey(enc)) {
//                                langStat.putDerived(enc, encLang.frac * encFrac / langStat.frac);
//                            }
//                            break out;
//                        }
//                    }
//                    System.out.println("ignored lang " + lang);
//                }
//            }
            //https://w3techs.com/technologies/details/en-utf8/all/all

            // SECONDARY NUM (should be equal to primary): encoding => language breakdown

//            ArrayList<Bar> encLangBars2 = parseBars("https://w3techs.com/technologies/breakdown/" + encLangRef + "/content_language\n");
//
//            for (Bar encLang : encLangBars2) {
//                Matcher langMatcher = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([a-zA-Z]+)-/all/all").matcher(encLang.href);
//                if (langMatcher.matches()) {
//                    String lang = langMatcher.group(1);
//                    out:
//                    {
//                        for (LangStats langStat : stats) {
//                            if (langStat.lang.equals(lang)) {
//                                langStat.put(enc, encLang.frac);
//                                break out;
//                            }
//                        }
//                        //System.out.println("ignored lang " + lang);
//                    }
//                }
//            }

            //need to get bars for: https://w3techs.com/technologies/segmentation/en-utf8/content_language
        //}

        for (LangStats s : stats) {
            System.out.println(s);
//            ArrayList<String> commonCharsets = new ArrayList<>(s.charsetFracs.keySet());
//            commonCharsets.retainAll(s.derivedCharsetFracs.keySet());
//            for (String ch1 : commonCharsets) {
//                for (String ch2 : commonCharsets) {
//                    double result = (s.charsetFracs.get(ch1) / s.derivedCharsetFracs.get(ch1)) / (s.charsetFracs.get(ch2) / s.derivedCharsetFracs.get(ch2));
//                    if (result > 1.0) {
//                        System.out.println(ch1 + " / " + ch2 + ": " + result);
//                    }
//                }
//            }
        }
    }

    @Test
    public void printUsages() throws Exception {
        HashMap<String, Double> charsets = new HashMap<>();
        ArrayList<LangStats> stats = new ArrayList<>();

        // PRIMARY: content language => encoding segmentation
        ArrayList<Bar> langBars = parseBars("https://w3techs.com/technologies/overview/content_language/all");
        for (Bar bar : langBars) {
            Matcher m = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([a-zA-Z]+)-/all/all").matcher(bar.href);
            m.matches();
            String lang = m.group(1);
            LangStats ls = new LangStats(lang, bar.frac);
            stats.add(ls);
            ArrayList<Bar> encBars = parseBars("https://w3techs.com/technologies/segmentation/cl-" + lang + "-/character_encoding");
            for (Bar encBar : encBars) {
                charsets.merge(encBar.name, encBar.frac, Math::max);
                ls.put(encBar.name, encBar.frac);
            }
            //System.out.println(ls);
        }

        ArrayList<Bar> encBars = parseBars("https://w3techs.com/technologies/overview/character_encoding/all");
        for (Bar encBar : encBars) {
            //System.out.println(encBar.name + ": " + encBar.href + " " + encBar.percent);
            Matcher m = Pattern.compile("https://w3techs\\.com/technologies/details/([^/]+)/all/all").matcher(encBar.href);
            m.matches();

            String enc = encBar.name;
            double encFrac = encBar.frac;
            String encLangRef = m.group(1);

            ArrayList<Bar> encLangBars = parseBars("https://w3techs.com/technologies/segmentation/" + encLangRef + "/content_language");
            for (Bar encLang : encLangBars) {
                Matcher langMatcher = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([a-zA-Z]+)-/all/all").matcher(encLang.href);
                langMatcher.matches();
                String lang = langMatcher.group(1);
                out:
                {
                    for (LangStats langStat : stats) {
                        if (langStat.lang.equals(lang)) {
                            // % of known lang, enc E sites with at least one lang L page   *    % of known enc sites with at least one enc E page / % of known lang sites with at least one lang L page
                            // we really need extra multiplier: * % of all sites with known enc / % of all sites with known lang = known enc / known lang
                            if (!langStat.charsetFracs.containsKey(enc)) {
                                langStat.putDerived(enc, encLang.frac * encFrac / langStat.frac);
                            }
                            break out;
                        }
                    }
                    System.out.println("ignored lang " + lang);
                }
            }
            //https://w3techs.com/technologies/details/en-utf8/all/all

            // SECONDARY NUM (should be equal to primary): encoding => language breakdown

            ArrayList<Bar> encLangBars2 = parseBars("https://w3techs.com/technologies/breakdown/" + encLangRef + "/content_language\n");

            for (Bar encLang : encLangBars2) {
                Matcher langMatcher = Pattern.compile("https://w3techs\\.com/technologies/details/cl-([a-zA-Z]+)-/all/all").matcher(encLang.href);
                if (langMatcher.matches()) {
                    String lang = langMatcher.group(1);
                    out:
                    {
                        for (LangStats langStat : stats) {
                            if (langStat.lang.equals(lang)) {
                                langStat.put(enc, encLang.frac);
                                break out;
                            }
                        }
                        //System.out.println("ignored lang " + lang);
                    }
                }
            }

            //need to get bars for: https://w3techs.com/technologies/segmentation/en-utf8/content_language
        }

        for (LangStats s : stats) {
            System.out.println(s);
//            ArrayList<String> commonCharsets = new ArrayList<>(s.charsetFracs.keySet());
//            commonCharsets.retainAll(s.derivedCharsetFracs.keySet());
//            for (String ch1 : commonCharsets) {
//                for (String ch2 : commonCharsets) {
//                    double result = (s.charsetFracs.get(ch1) / s.derivedCharsetFracs.get(ch1)) / (s.charsetFracs.get(ch2) / s.derivedCharsetFracs.get(ch2));
//                    if (result > 1.0) {
//                        System.out.println(ch1 + " / " + ch2 + ": " + result);
//                    }
//                }
//            }
    }

        // all the data we have on zh & GB18030:
        // *,GB18030=null,< 0.1%
        // *,GB2312=null,0.3%
        // *,GBK=null,0.1%
        // ch,GB2312=null,5.4%
        // en,GB2312=11.1%,< 0.1%
        // en,GB18030=48.9%,< 0.1%
        // en,GBK=8%,< 0.1%
        // es,GB18030=24.1%,null
        // de,GB2312=null,< 0.1%
        // de,GBK=null,< 0.1%
        // fa,GB2312=0.2%,null
        // zh,UTF-8=1.4%,90.4%
        // zh,GB2312=87.8%,5.2%
        // zh,GBK=90%,2.5%
        // vi,GBK=0.5%,null
        // zh,Big5=86.6%,null
        // zh,Big5 HKSCS=80%,null
        // zh,GB18030=19.1%,null
        // zh,*=1.5%,null
        // zh,ISO-8859-1=0.1%,null
        // zh,Windows-1251=0%,null

        // Old data:

        // want: LANGUAGE/SEGMENTATION/ENCODING  ==  ENCODING/BREAKDOWN/LANGUAGE      ==  (lang L & enc E) / (lang L & enc ANY)    =   e(L, E)  =  l(L, E) * (enc E & anylang) / (lang L & anyenc)   =    m(L) * n(E) * l(L, E)

        // have: LANGUAGE/BREAKDOWN/ENCODING     ==  ENCODING/SEGMENTATION/LANGUAGE   ==  (lang L & enc E) / (lang ANY & enc E)    =   l(L, E)

        // e(L, E)  =  l(L, E) * (enc E & anylang) / (lang L & anyenc)
        // e(L, E) / l(L, E) = (enc E & anylang) / (lang L & anyenc)
        // (e(L, E1) / l(L, E1)) / (e(L, E2) / l(L, E2)) = (enc E1 & anylang) / (enc E2 & anylang)

        // let's say:
        //   m(L1) * n(E1) = mn11
        //   m(L1) * n(E2) = mn12
        //   then n(E1) / n(E2) = mn11 / mn12
        //
        //   m(L2) * n(E1) = mn21
        //   then m(L2) * n(E2) should be: mn21

        // so: e(L, E1) / utf8(L) = (m(L) * n(E1) * l(L, E1)) / (m(L) * n(E2) * l(L, E2)) = (n(E1) * l(L, E1)) / (n(E2) * l(L, E2))  =   (n(E1) / n(utf8)) * (l(L, E1) / l(L, utf8))


        // LANG/BREAKDOWN/ENC
        // define (lang ANY & enc E) / ALL =

        // [(lang L & enc E) / (lang ANY & enc E)] * (enc E / enc ANY) / (lang L / lang ANY) * (lang ANY & enc E) / ALL   ==  (lang L & enc E) / ALL *


        // [(lang L & enc E) / (lang ANY & enc E)] * (lang ANY & enc E) / (lang L & enc ANY)  ==  (lang L & enc E) / (lang L & enc ANY)

        // so need to find: (lang ANY & enc E) / (lang L & enc ANY)
        // or:   (lang ANY & enc E) / (lang ANY & enc ANY)      and     (lang L and enc ANY) / (lang ANY & enc ANY)
        //

        // want: (lang L & enc E) / (lang L & enc ANY)

        // we also have: (lang L & enc E) / (lang ANY)
        //             and: (lang L & enc E) / (enc ANY)
        //             and: (lang L) / (lang ANY)
        //             and: (enc E) / (enc ANY)
        // so we can derive:
        //    (lang L & enc E) / (lang L)
        //    (lang L & enc E) / (enc E)
        //    (enc E) / (lang L)
        //    (lang L) / (enc E)

        // (L & E) / (anylang & E) * (E * lang(E)) / (L * enc(L))  ==   (L & E) / (L & anyenc)

        // so we still need:    (anylang & E) / E    = lang(E)    = percent of sites with enc E that have a lang   == constant
        //              and:    (L & anyenc)  / L    = enc(L)     = percent of sites with lang L that have a enc   == constant

        // so: (E * lang(E)) / (L * enc(L))  ==  (L & E) / (L & anyenc) / (L & E / (anylang & E))  ==


        // LANGUAGE > ENCODING
        // Chinese > Encoding Breakdown:
        //    - Chinese = 1.5% of ALL SITES with known lang    i.e., 1.5% = Chinese UTF-8 sites / all sites with known lang    =  (lang Chinese & enc UTF-8) / (lang ANY)
        //    - Chinese = 1.5% of UTF-8 sites with known lang  i.e., 1.5% = Chinese UTF-8 sites / UTF-8 sites with known lang
        //    - Chinese = 0.2% of ISO-8859-1 sites with known lang
        //    - Chinese = 0%   of windows-1251 sites with known lang

        // Chinese > Encoding Segmentation:     PRIMARY INDICATOR
        //    - UTF-8  = 88.1% of Chinese sites with known enc,  i.e.,    88.1% = Chinese UTF-8 sites / Chinese sites with known enc
        //    - GB2312 = 6.6%  of Chinese sites with known enc
        //    - GBK    = 3.3%  of Chinese sites with known enc
        //

        // English > Encoding Breakdown
        //    - English = 54.7% of ALL SITES with known lang
        //    - English = 55.6% of UTF-8 sites with known lang
        //    - English = 46.5% of ISO-8859-1 sites with known lang
        //    - English = 9.1%  of Windows-1251 sites with known lang

        // ENCODING > LANGUAGE

        // UTF-8 > Language Breakdown:     SECONDARY INDICATOR (will be equal to primary indicator)
        //    - UTF-8 = 92.7% of ALL SITES with known enc
        //    - UTF-8 = 97.1% of English sites with known enc
        //    - UTF-8 = 91.6% of German sites with known enc
        //    - UTF-8 = 88.3% of Russian sites with known enc

        // UTF-8 > Language Segmentation:
        //    - English = 54.9% of UTF-8 sites with known lang
        //    - German  = 5.8%  of UTF-8 sites with known lang
        //    - Russian = 5.6%  of UTF-8 sites with known lang

        // (newer data)
        // GB2312 > Language Breakdown     SECONDARY INDICATOR
        //    - GB2312 = 0.3% of ALL SITES with known enc
        //    - GB2312 < 0.1% of English sites with known enc
        //    - GB2312 < 0.1% of Russian sites with known enc
        //    - GB2312 < 0.1% of German sites with known enc

        // GB2312 > Language Segmentation
        //    - Chinese = 87.8% of GB2312 sites with known lang
        //    - English = 11.1% of GB2312 sites with known lang
        //    - Vietnamese = 0.2% of GB2312 sites with known lang

        // GB18030 > Language Segmentation:
        //    - English = 48.6% of GB18030 sites with known lang
        //    - Spanish = 24.3% of GB18030 sites with known lang
        //    - Chinese = 19.3% of GB18030 sites with known lang

        System.out.println(charsets);
    }

    @Test
    public void blah() throws Exception {
        String urlRu = "https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D1%82%D0%B5%D0%BC%D0%B0%D1%82%D0%B8%D0%BA%D0%B0";
        final byte[] content = IOUtils.toByteArray(URI.create(urlRu));
        String contentStr = new String(content, StandardCharsets.UTF_8);
        String charset = "KOI8-R";
        final byte[] textK = contentStr.getBytes(charset);
//        UniversalDetector detector = new UniversalDetector(new CharsetListener() {
//            @Override
//            public void report(String charset) {
//                System.out.println("REPORTING: " + charset);
//            }
//        });
//        detector.handleData(textK, 0, textK.length);
//        System.out.println("DETECTED: " + detector.getDetectedCharset());
//        detector.dataEnd();

//        UniversalDetector detector = new UniversalDetector(new CharsetListener() {
//            @Override
//            public void report(String charset) {
//                System.out.println("REPORTING: " + charset);
//            }
//        });
//        detector.handleData(textK, 0, textK.length);
//        System.out.println("DETECTED: " + detector.getDetectedCharset());
//        detector.dataEnd();
//
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        new InternalAny23Ext() {
//            {
//                super.init(new StringReader(new String(textK, StandardCharsets.ISO_8859_1)));
//                super.extractText();
//            }
//            @Override
//            protected void doctype(DocumentType docType) {
//
//            }
//
//            @Override
//            protected void comment(String data) {
//
//            }
//
//            @Override
//            protected void text(String text) {
//                try {
//                    while (text.endsWith(" ")) {
//                        text = text.substring(0, text.length() - 1);
//                    }
//                    while (text.startsWith(" ")) {
//                        text = text.substring(1);
//                    }
//                    text = " " + text + " ";
//                    byte[] b = text.getBytes(StandardCharsets.ISO_8859_1);
//                    bytes.write(b);
//                    //String s = new String(b, charset).trim();
//                    //if (!s.isEmpty())
//                    //System.out.println(s);
//                } catch (Exception e) {
//                    throw new AssertionError(e);
//                }
//            }
//
//            @Override
//            protected void startTag(String name, Attributes attributes, boolean selfClosing) {
//
//            }
//
//            @Override
//            protected void endTag(String name) {
//
//            }
//
//        };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean lastWasSpace = false;
        for (int i = 0; i < textK.length; i++) {
            if (textK[i] < 0) {
                baos.write(textK, i, 1);
                lastWasSpace = false;
            } else if (!lastWasSpace) {
                baos.write(' ');
                lastWasSpace = true;
            }
        }
        final byte[] text = baos.toByteArray();

        CharsetDetector icu4j = new CharsetDetector(text.length);
        icu4j.setText(text);
        // TODO try again with icu4j with filterWithoutEnglishLetters() found in CharsetProber
        // (this is what juniversalcharset uses for all singlebyte charsets except ISO-8859-1)
        // (only for ISO-8859-1 does juniversalcharset use tag stripping (a.k.a. filterWithEnglishLetters()) (and a very primitive form at that))

        // So we need to modify juniversalcharset to use our tag stripping algorithm in their Latin1 (ISO-8859-1) prober

        // juniversalchardet order of precedence:
        // 1. escape prober:
        //     - HZ-GB-2312, ISO-2022-(CN, JP, KR)
        // 2. multibyte probers: do not use any stripping of content
        //     - UTF8, SJIS, EUCJP, GB18030, EUCKR, Big5, EUCTW
        // 3. singlebyte probers: replace all ascii sequences with single space character
        //     - win1251, koi8r, latin5, macCyrillic, ibm866, ibm855, latin7, win1253, latin5Bulgarian, win1251Bulgarian, Hebrew
        // 4. Latin1Prober: uses tag stripping
        //     - ISO-8859-1

        // NEW STRATEGY:
        // 0. if has BOM, return BOM charset
        // 1. if looks like UTF-8, return UTF-8
        // 2. if all ascii, return UTF-8
        // 3. if has contenttype charset != UTF-8, return contenttype charset
        // 4. if has declared html/xml charset, return declared charset
        // 5. run each charset detector on appropriate input
        // 6. if charset detector's lang matches page lang increase confidence; else if has lang that is different from lang of page, decrease
        // 7. if Latin1Prober does not report error, return ISO-8859-1 or windows-1252


        // TODO compare (universalchardet) Latin1Prober to (icu4j) CharsetRecog_8859_1 after stripping tags.
        //  Latin1Prober looks far more primitive
        //    -Inf indicators: undefined win-1252 chars
        //    -20  indicators: small + upper (if at least 1 is not ascii), latin vowel + latin vowel (i.e. 2 accented vowels in a row)
        //    +0   indicators: latin upper vowel + latin consonant or diphthong
        //    +1   indicators: everything else (including punct, control, small ascii + upper ascii)
        //  CharsetRecog_8859_1: ngrams for da, de, en, es, fr, it, nl, no, pt, sv
        //    conf = total 3-grams found / total 3-gram slots


        // https://w3techs.com/technologies/segmentation/cl-{lang}-/character_encoding

        // lang    UTF-8 ISO88591 win1252  other
        // en      97.8%   1.5%   0.5%     0.2%
        // de      93.0%   5.6%   0.8%     0.6%
        // es      96.1%   3.2%   0.7%     0
        // fr      94.2%   4.7%   0.7%     0.4%
        // ja      88.5%   0.0%
        // pt      93.6%   5.7%
        // fa      99.9%   0.0%
        // it      94.6%   4.2%
        // tr      95.1%   0.1%
        // pl      96.6%   0.0%
        // zh      90.3%   0.2%
        // nl      97.1%   2.2%

        // lang    UTF-8 win1251 KOI8-R   other
        // ru      91.4%   8.5%  0.1%     0.0%

        // lang    UTF-8   sjis  euc-jp   other
        // ja      88.5%   8.0%  3.7%     -0.2% wtf ?!?


        // percent of all websites with a known charset that is win1252: 0.6%   =    | win1252 | / | all |
        // percent of win1252 websites with a known language whose language is en: 58%   =    | EN intersect win1252 | / | win1252 with known lang |
        // what percent of english websites are encoded in win1252?   =    | EN intersect win1252 | / | EN |
        // left to find:   | win1252 with known lang | / | EN |    ~=     | win1252 | / | all| * | all | / | EN |


        // iso-8859-15 & german / iso-8859-15 = 51.8%     https://w3techs.com/technologies/segmentation/en-iso885915/content_language
        // iso-8859-15 / all = 0.1%                       https://w3techs.com/technologies/details/en-iso885915/all/all    or   https://w3techs.com/technologies/overview/character_encoding/all
        // german / all  =  5.2%                          https://w3techs.com/technologies/overview/content_language/all

        // want to know: iso-8859-15 & german / german  =  (iso-8859-15 & german / iso-8859-15) * (iso-8859-15 / all) / (german / all) = 0.518 * 0.001 / 0.052 = 0.01 = 1.0%

        // https://github.com/hsivonen/shift_or_euc

        // https://hg.mozilla.org/mozilla-central/file/tip/intl/chardet/tools

        // https://bugzilla.mozilla.org/show_bug.cgi?id=844115 !!!

        // Actual relative importance of charset detection:
        // 1. UTF-8: 94.1%
        // 2. ISO-8859-1 + Windows-1252 + ISO-8859-15: 2.9% + 0.6% + 0.1% = 3.6%
        // 3. Windows-1251: 0.9% (but 13% of Russian websites)
        // 4.


        for (CharsetMatch match : icu4j.detectAll()) {
            try {
                Charset ch = EncodingUtils.forName(match.getName());
                System.out.println(ch + ": " + match.getConfidence() + " " + match.getLanguage());

                // If we successfully filtered input based on 0x3C and 0x3E, then this must be an ascii-compatible charset
                // See https://issues.apache.org/jira/browse/TIKA-2771
//                if (filterInput && !TAG_CHARS.equals(new String(TAG_BYTES, charset))) {
//                    continue;
//                }

//                charset = EncodingUtils.correctVariant(stats, charset);
//                if (charset != null) {
//                    return charset;
//                }
            } catch (Exception e) {
                //ignore; if this charset isn't supported by this platform, it's probably not correct anyway.
            }
        }


    }
}
