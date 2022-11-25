/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.util.xml;

import dk.kb.util.Profiler;
import dk.kb.util.Resolver;
import dk.kb.util.string.Strings;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class XMLStepperTest {
    private static final Logger log = LoggerFactory.getLogger(XMLStepperTest.class);

    private static final String SAMPLE =
            "<foo><bar xmlns=\"http://www.example.com/bar_ns/\">"
            + "<nam:subsub xmlns:nam=\"http://example.com/subsub_ns\">content1<!-- Sub comment --></nam:subsub>"
            + "<!-- Comment --></bar>\n"
            + "<bar><subsub>content2</subsub></bar></foo>";

    private static final String SAMPLE_ATTRIBUTE =
            "<foo><bar xmlns=\"http://www.example.com/bar_ns/\">"
            + "<nam:subsub xmlns:nam=\"http://example.com/subsub_ns\">content1<!-- Sub comment --></nam:subsub>"
            + "<!-- Comment --></bar>\n"
            + "<bar this=\"isit\"><subsub>content2</subsub></bar></foo>";

    private static final String DERIVED_NAMESPACE =
            "<foo xmlns=\"http://www.example.com/foo_ns/\"><bar>simple bar</bar></foo>";

    private static final String OUTER_SNIPPET = "<bar>bar1</bar><bar>bar2</bar>";
    private static final String OUTER_FULL = "<major><foo>" + OUTER_SNIPPET + "</foo>\n<foo>next</foo></major>";

    private XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    {
        xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        // No resolving of external DTDs
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }
    private XMLOutputFactory xmlOutFactory = XMLOutputFactory.newInstance();

    @Test
    public void testGetSubXMLFromPath() throws XMLStreamException {
        final String EXPECTED = "<bar xmlns=\"http://www.example.com/bar_ns/\"><nam:subsub xmlns:nam=\"http://example.com/subsub_ns\">content1<!-- Sub comment --></nam:subsub><!-- Comment --></bar>";
        XMLStreamReader xmlReader = XMLStepper.jumpToNextFakeXPath(SAMPLE, "foo/bar");
        assertNotNull(xmlReader, "Skipping to 'foo/bar' should work");
        String subXML = XMLStepper.getSubXML(xmlReader, true);
        assertEquals(EXPECTED, subXML, "The extracted XML should be as exped");
    }

    @Test
    public void testGetSubXMLFromPathWithAttribute() throws XMLStreamException {
        final String EXPECTED = "<bar this=\"isit\"><subsub>content2</subsub></bar>";
        final String XPATH = "foo/bar[@this='isit']";
        XMLStreamReader xmlReader = XMLStepper.jumpToNextFakeXPath(SAMPLE_ATTRIBUTE, XPATH);
        assertNotNull(xmlReader, "Skipping to '" + XPATH + "' should work");
        String subXML = XMLStepper.getSubXML(xmlReader, true);
        assertEquals(EXPECTED, subXML, "The extracted XML should be as exped");
    }

    @Test
    public void testGetSubXMLFromPathWithAttribute2() throws XMLStreamException {
        final String EXPECTED = "<subsub>content2</subsub>";
        final String XPATH = "foo/bar[@this='isit']/subsub";
        XMLStreamReader xmlReader = XMLStepper.jumpToNextFakeXPath(SAMPLE_ATTRIBUTE, XPATH);
        assertNotNull(xmlReader, "Skipping to '" + XPATH + "' should work");
        String subXML = XMLStepper.getSubXML(xmlReader, true);
        assertEquals(EXPECTED, subXML, "The extracted XML should be as exped");
    }

    @Test
    public void testGetSubXMLLocationIndependent() throws XMLStreamException {
        final String EXPECTED = "<bar this=\"isit\"><subsub>content2</subsub></bar>";
        final String XPATH = "//bar[@this='isit']";
        XMLStreamReader xmlReader = XMLStepper.jumpToNextFakeXPath(SAMPLE_ATTRIBUTE, XPATH);
        assertNotNull(xmlReader, "Skipping to '" + XPATH + "' should work");
        String subXML = XMLStepper.getSubXML(xmlReader, true);
        assertEquals(EXPECTED, subXML, "The extracted XML should be as exped");
    }

    @Test
    public void testGetSubXMLLocationIndependentFail() throws XMLStreamException {
        final String EXPECTED = "<subsub>content2</subsub>";
        final String XPATH = "//bar[@this='isit']/subsub";
        try {
            XMLStepper.jumpToNextFakeXPath(SAMPLE_ATTRIBUTE, XPATH);
            fail("Using path '" + XPATH + "' should fail early as predicates for XPaths starting with '//' are " +
                 "only supported for the last element");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testPartialALTO() throws XMLStreamException, IOException {
        final String FEDORA = Resolver.resolveUTF8String("xml/partial_alto.xml");
        final String XPATH = "digitalObjectBundle/digitalObject/datastream[@ID='ALTO']/datastreamVersion/xmlContent/alto";

        XMLStreamReader xmlALTOReader = XMLStepper.jumpToNextFakeXPath(FEDORA, XPATH);
        assertNotNull(xmlALTOReader, "ALTO block should be findable with path '" + XPATH + "'");
        String alto = XMLStepper.getSubXML(xmlALTOReader, true);
        assertNotNull(alto, "Extracted ALTO should not be null");
        System.out.println(alto);
    }

    @Test
    public void testFakeXPathParse() throws XMLStreamException {
        final String XPATH = "foo/bar[@this='isit']/subsub";
        XMLStepper.FakeXPath xPath = new XMLStepper.FakeXPath(XPATH);
        assertEquals(XPATH, xPath.toString(), "Parsed fakeXPath should match input");
    }


    @Test
    public void testFakeXPath() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/property/@name", "project.name", "project.version.variant"}
        };
        assertXPaths(BIG_XML, tests, 2);
    }

    @Test
    public void testFakeXPathStar() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/property/@name", "project.name"},
                {"/project/*/@name", "project.name"},
                {"/*/property/@name", "project.name"},
                {"/*/*/@name", "project.name"}
        };
        assertXPathShorthand(BIG_XML, tests);
    }

    @Test
    public void testFakeXPathPredicated() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/monkey[@bar='fun']", "ape"},
                {"/project/property[@name='lib.dir']/@value", "${basedir}/lib"},
                {"/project/property[@name]/@value", "sbutil"},
                {"/project/*[@name='config.dir']/@value", "${basedir}/config"},
        };
        assertXPathShorthand(BIG_XML, tests);
    }

    @Test
    public void testFakeXPathAnywhere() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"//bar", "zoo2"},
                {"//bar/text()", "zoo2"},
                {"/bar]", null},
                {"/project/foo/bar", "zoo2", ""},
                {"/project/foo/bar/text()", "zoo2", ""},
        };
        assertXPathShorthand(BIG_XML, tests);
    }

    @Test
    public void testFakeXPathShorthand() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/property/@name", "project.name"},
                {"/project/tstamp/format/@pattern", "MM/dd/yyyy HH:mm"}
        };
        assertXPathShorthand(BIG_XML, tests);
        assertXPathShorthands(BIG_XML, tests);
    }
    private void assertXPathShorthand(String xml, String[][] tests) throws XMLStreamException {
        for (String[] test : tests) {
            String result = XMLStepper.evaluateFakeXPath(xml, test[0]);
            assertEquals(test[1], result,
                         "The single-xpath result for '" + test[0] + " should be as expected");
        }
    }
    private void assertXPathShorthands(String xml, String[][] tests) throws XMLStreamException {
        List<String> xPaths = new ArrayList<String>(tests.length);
        for (String[] test: tests) {
            xPaths.add(test[0]);
        }
        List<String> results = XMLStepper.evaluateFakeXPathsSingleResults(xml, xPaths);

        for (int i = 0; i < tests.length; i++) {
            String[] test = tests[i];
            String result = results.get(i);
            assertEquals(test[1], result,
                         "The multi-xpaths result for '" + test[0] + " should be as expected");
        }
    }

    @Test
    public void testFakeXPathEarlyTermination() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/tstamp/format/@pattern", "MM/dd/yyyy HH:mm"}
        };
        assertXPaths(BIG_XML, tests, 1);
    }

    @Test
    public void testFakeXPathMulti() throws XMLStreamException {
        final String BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
        final String[][] tests = new String[][]{
                {"/project/property/@name", "project.name", "project.version.variant"},
                {"/project/tstamp/format/@pattern", "MM/dd/yyyy HH:mm"},
                {"/project/moo", "zoo1"},
                {"/project/foo/bar", "zoo2", ""},
                // TODO: Make multi-matches work
                //{"/project/foo/bar", "zoo2"}, // Yes, repeat of the above - does not work yet
                //{"/project/foo/bar@moo", "?"},
                //{"/project/foo/bar/@somat", "zoo4"}
                {"/project/foo/baz", "zoo3"},
                {"//monkey", "ape", "gorilla"}
        };
        assertXPaths(BIG_XML, tests, 2);
    }

    private void assertXPaths(String xml, String[][] tests, int maxMatchesPerXP) throws XMLStreamException {
        List<String> xPaths = new ArrayList<String>(tests.length);
        for (String[] test: tests) {
            xPaths.add(test[0]);
        }
        List<List<String>> results = XMLStepper.evaluateFakeXPaths(xml, xPaths, maxMatchesPerXP);

        for (int i = 0; i < tests.length; i++) {
            String[] test = tests[i];
            List<String> result = results.get(i);
            assertEquals(test.length - 1, result.size(),
                         "The number of matches for " + test[0] + " should be as expected");
            for (int j = 0 ; j < test.length-1 ; j++) {
                assertEquals(test[j+1], result.get(j),
                             "Result #" + j + " for " + test[0] + " should match");
            }
        }
    }

    @Test
    public void testSpaceRemoval() throws IOException, XMLStreamException {
        String INPUT = Strings.flush(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "replacement_input.xml"));
        String EXPECTED = Strings.flush(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "replacement_expected.xml"));
        String actual = XMLStepper.replaceElementText(INPUT, new XMLStepper.ContentReplaceCallback() {
            @Override
            protected String replace(List<String> tags, String current, String originalText) {
                return originalText.replace(" ", "");
            }

            @Override
            protected boolean match(XMLStreamReader xml, List<String> tags, String current) {
                return "bar".equals(current) && "a".equals(XMLStepper.getAttribute(xml, "name", "")) ||
                        "baz".equals(current);
            }
        });
        assertEquals(EXPECTED, actual,
                     "The text content replaced XML should be as expected");
    }

    @Test
    public void testSpaceRemovalStreaming() throws IOException, XMLStreamException {
        InputStream INPUT = Thread.currentThread().getContextClassLoader().getResourceAsStream("replacement_input.xml");
        String EXPECTED = Strings.flush(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "replacement_expected.xml"));
        InputStream actualS = XMLStepper.streamingReplaceElementText(INPUT, new XMLStepper.ContentReplaceCallback() {
            @Override
            protected String replace(List<String> tags, String current, String originalText) {
                return originalText.replace(" ", "");
            }

            @Override
            protected boolean match(XMLStreamReader xml, List<String> tags, String current) {
                return "bar".equals(current) && "a".equals(XMLStepper.getAttribute(xml, "name", "")) ||
                        "baz".equals(current);
            }
        });

        String actual = Strings.flush(actualS);
        // TODO: Figure out why the streaming version keeps the header, while the direct one doesn't
        actual = actual.replace("<?xml version='1.0' encoding='UTF-8'?>", "").trim();
        assertEquals(EXPECTED, actual,
                     "The text content replaced XML should be as expected");
    }

    @Test
    public void testIsWellformed() {
        final String[] FINE = new String[]{
                "<foo xmlns=\"http://www.example.com/foo_ns/\"><bar>simple bar</bar></foo>",
                "<foo/>"
        };
        final String[] FAULTY = new String[]{
                "<foo xmlns=\"http://www.example.com/foo_ns/\"><bar>simple bar<bar></bar></foo>",
                "<foo xmlns=\"http://www.example.com/foo_ns/\"><bar>simple bar</bar>",
                "<foo xmlns=\"http://www.example.com/foo_ns/\"><bar>simple bar</bar></doo>",
                "<foo xmlns=\"http://www.example.com/foo_ns/><bar>simple bar</bar></foo>",
        };
        for (String fine: FINE) {
            assertTrue(XMLStepper.isWellformed(fine),
                       "The XML should be well-formed: " + fine);
        }
        for (String faulty: FAULTY) {
            assertFalse(XMLStepper.isWellformed(faulty),
                        "The XML should not be well-formed: " + faulty);
        }
    }

    @Test
    public void testMultipleInner() throws XMLStreamException {
        final String XML = "<foo><bar><zoo>z1</zoo><zoo>z2</zoo></bar></foo>";
        for (final Boolean step: new boolean[]{Boolean.FALSE, Boolean.TRUE}) {
            XMLStreamReader xml = xmlFactory.createXMLStreamReader(new StringReader(XML));
            XMLStepper.findTagStart(xml, "zoo");
            final AtomicInteger zooCount = new AtomicInteger(0);
            XMLStepper.iterateTags(xml, new XMLStepper.Callback() {
                @Override
                public boolean elementStart(XMLStreamReader xml, List<String> tags, String current)
                        throws XMLStreamException {
                    if ("zoo".equals(current)) {
                        zooCount.incrementAndGet();
                    }
                    if (step) {
                        xml.next();
                        return true;
                    }
                    return false;
                }
            });
            assertEquals(2, zooCount.get(),
                         "After iteration with step==" + step
                         + ", the stepper should have encountered 'zoo' the right number of times");
            assertEquals("bar", xml.getLocalName(),
                         "After iteration with step==" + step +
                         ", the reader should be positioned at the correct end tag");
        }


    }

    @Test
    public void testLenient() throws XMLStreamException {
        final String[][] TESTS = new String[][]{
                new String[]{"<foo><bar><zoo>Hello</zoo></bar></foo>", "zoo", "bar"},
                new String[]{"<foo><bar><zoo>Hello</zoo></bar></foo>", "zoo", "foo"},
                new String[]{"<foo><bar>Hello</bar></foo>", "bar", "foo"}
        };
        for (String[] test: TESTS) {
            try {
                XMLStreamReader xml = xmlFactory.createXMLStreamReader(new StringReader(test[0]));
                lenientHelper(xml, false, test[1], test[2]);
                fail("Stepping past the current element with lenient==false should raise an exception for " + test[0]);
            } catch (IllegalStateException e) {
                // Expected
            }
            XMLStreamReader xml = xmlFactory.createXMLStreamReader(new StringReader(test[0]));
            lenientHelper(xml, true, test[1], test[2]);
        }
    }
    private void lenientHelper(XMLStreamReader xml, boolean lenient, final String startTag, final String skipToEndTag)
            throws XMLStreamException {
        XMLStepper.iterateTags(xml, lenient, new XMLStepper.Callback() {
            @Override
            public boolean elementStart(XMLStreamReader xml, List<String> tags, String current)
                    throws XMLStreamException {
                if (startTag.equals(current)) {
                    XMLStepper.findTagEnd(xml, skipToEndTag);
                    return true;
                }
                return false;
            }
        });
    }

    private final static String LIMIT_BARS =
            "<foo><bar zoo=\"true\"></bar><bar zoo=\"true\"></bar><bar zoo=\"false\"></bar><baz></baz></foo>";

    @Test
    public void testLimitXMLSimple() throws XMLStreamException {
        assertLimit(LIMIT_BARS, "<foo><baz /></foo>", false, true, false,
                    "/foo/bar", 0);
        assertLimit(LIMIT_BARS, "<foo><bar zoo=\"true\" /><baz /></foo>", false, true, false,
                    "/foo/bar", 1);
        assertLimit(LIMIT_BARS, "<foo><bar zoo=\"true\" /><bar zoo=\"true\" /><baz /></foo>", false, true, false,
                    "/foo/bar", 2);
    }

    @Test
    public void testLimitPositiveList() throws XMLStreamException {
        assertLimit(LIMIT_BARS, "<foo><bar zoo=\"true\" /></foo>", false, true, true,
                    "/foo$", -1, "/foo/bar", 1);
        assertLimit(LIMIT_BARS, "<foo><baz /></foo>", false, true, true,
                    "/foo$", -1, "/foo/baz", 1);
    }

    @Test
    public void testLimitXMLAttribute() throws XMLStreamException {
        assertLimit(LIMIT_BARS, "<foo><bar zoo=\"false\" /><baz /></foo>", false, false, false,
                    "/foo/bar#zoo=true", 0);
        assertLimit(LIMIT_BARS, "<foo><bar zoo=\"true\" /><bar zoo=\"false\" /><baz /></foo>", false, false, false,
                    "/foo/bar#zoo=true", 1);
    }

    @Test
    public void testLimitXMLAttributeNamespace() throws XMLStreamException {
        final String NS =
                "<n:foo xmlns:n=\"sjfk\" xmlns=\"myDefault\"><bar n:zoo=\"true\"></bar><bar zoo=\"true\"></bar>"
                + "<bar zoo=\"false\"></bar><baz></baz></n:foo>";
        assertLimit(NS, "<n:foo xmlns:n=\"sjfk\" xmlns=\"myDefault\"><bar zoo=\"false\"/><baz/></n:foo>",
                    false, false, false, "/foo/bar#zoo=true", 0);
    }

    @Test
    public void testLimitCountPatterns() throws XMLStreamException {
        assertLimit(LIMIT_BARS,
                    "<foo><bar zoo=\"true\"/><bar zoo=\"true\"/><baz/></foo>", true, true, false,
                    ".*", 2);
    }

    // Limits on specific field with specific tag
    @Test
    public void testLimitPerformance() throws IOException, XMLStreamException {
        final String SAMPLE = getSample(9423);
        final int RUNS = 10;
        final Map<Pattern, Integer> limits = new HashMap<Pattern, Integer>();
        limits.put(Pattern.compile("/record/datafield#tag=Z30"), 10);

        Profiler profiler = new Profiler(RUNS);

        String reduced = "";
        for (int run = 0 ; run < RUNS ; run++) {
            reduced = XMLStepper.limitXML(SAMPLE, limits, false, false, false);
            profiler.beat();
        }
        log.info(String.format(Locale.ROOT, 
                "Reduced %d blocks @ %dKB to %dKB at %.1f reductions/sec",
                RUNS, SAMPLE.length() / 1024, reduced.length() / 1024, profiler.getBps(false)));
        assertTrue(reduced.contains("<datafield tag=\"LOC\""),
                   "The reduced XML should contain datafields after the skipped ones");
    }

    @Test
    public void testLimitException() throws XMLStreamException {
        Map<Pattern, Integer> lims = new HashMap<Pattern, Integer>();
        lims.put(Pattern.compile("/foo/bar"), 1);
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader("<foo><bar s=\"t\" /><<</foo>"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter out = xmlOutFactory.createXMLStreamWriter(os);
        try {
            XMLStepper.limitXML(in, out, lims, true, true, false);
            fail("An XMLStreamException was expected here due to invalid input XML");
        } catch (XMLStreamException e) {
            // Intended
        }
    }

    // Limits in all datafields, counting on unique datafield#tag=value
    @Test
    public void testLimitPerformanceCountPatterns() throws IOException, XMLStreamException {
        final String SAMPLE = getSample(9423);
        final int RUNS = 10;
        final Map<Pattern, Integer> limits = new HashMap<Pattern, Integer>();
        limits.put(Pattern.compile("/record/datafield#tag=.*"), 10);

        Profiler profiler = new Profiler(RUNS);

        XMLStepper.Limiter limiter = XMLStepper.createLimiter(limits, true, false, false);

        String reduced = "";
        for (int run = 0 ; run < RUNS ; run++) {
            reduced = limiter.limit(SAMPLE);
            profiler.beat();
        }
        log.info(String.format(Locale.ROOT, 
                "Reduced %d blocks @ %dKB to %dKB at %.1f reductions/sec",
                RUNS, SAMPLE.length() / 1024, reduced.length() / 1024, profiler.getBps(false)));
        assertTrue(reduced.contains("<datafield tag=\"LOC\""),
                   "The reduced XML should contain datafields after the skipped ones");
    }

    private String getSample(int repeats) {
        StringBuilder sb = new StringBuilder(10*1024*1024);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<record xmlns=\"http://www.loc.gov/MARC21/slim\" " +
                  "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                  "  xmlns:null=\"http://www.loc.gov/MARC21/slim\" " +
                  "  schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">\n" +
                  "  <leader>00000nap  1233400   6543</leader>\n" +
                  "  <datafield tag=\"004\" ind1=\"0\" ind2=\"0\">\n" +
                  "    <subfield code=\"r\">n</subfield>\n" +
                  "    <subfield code=\"a\">e</subfield>\n" +
                  "  </datafield>\n" +
                  "  <datafield tag=\"001\" ind1=\" \" ind2=\" \">\n" +
                  "    <subfield code=\"a\">4106186</subfield>\n" +
                  "    <subfield code=\"f\">a</subfield>\n" +
                  "  </datafield>\n");
        for (int i = 0 ; i < repeats ; i++) {
            sb.append("<datafield tag=\"Z30\" ind1=\"-\" ind2=\"2\">\n" +
                      "    <subfield code=\"l\">SOL02</subfield>\n" +
                      "    <subfield code=\"8\">20100327</subfield>\n" +
                      "    <subfield code=\"m\">ISSUE</subfield>\n" +
                      "    <subfield code=\"1\">UASB</subfield>\n" +
                      "    <subfield code=\"2\">UASBH</subfield>\n" +
                      "    <subfield code=\"3\">Bom</subfield>\n" +
                      "    <subfield code=\"5\">").append("12345-67").append(i).append(
                    "</subfield>\n" +
                    "    <subfield code=\"a\">2010</subfield>\n" +
                    "    <subfield code=\"b\">1</subfield>\n" +
                    "    <subfield code=\"c\">3456</subfield>\n" +
                    "    <subfield code=\"f\">67</subfield>\n" +
                    "    <subfield code=\"h\">2010 1  6543</subfield>\n" +
                    "    <subfield code=\"i\">20100821</subfield>\n" +
                    "    <subfield code=\"j\">20101025</subfield>\n" +
                    "    <subfield code=\"k\">20100910</subfield>\n" +
                    "  </datafield>\n");
        }
        sb.append(
                "  <datafield tag=\"STS\" ind1=\" \" ind2=\" \">\n" +
                "    <subfield code=\"a\">67</subfield>\n" +
                "  </datafield>\n" +
                "  <datafield tag=\"SBL\" ind1=\" \" ind2=\" \">\n" +
                "    <subfield code=\"a\">FOOB</subfield>\n" +
                "  </datafield>\n" +
                "  <datafield tag=\"LOC\" ind1=\" \" ind2=\" \">\n" +
                "    <subfield code=\"b\">FOOB</subfield>\n" +
                "    <subfield code=\"c\">AUGHH</subfield>\n" +
                "    <subfield code=\"h\">MPG</subfield>\n" +
                "    <subfield code=\"o\">ISSUE</subfield>\n" +
                "  </datafield>\n" +
                "  <datafield tag=\"STS\" ind1=\" \" ind2=\" \">\n" +
                "    <subfield code=\"a\">67</subfield>\n" +
                "  </datafield>\n" +
                "</record>");
        return sb.toString();
    }

    private void assertLimit(String input, String expected, boolean countPatterns, boolean onlyElementMatch,
                             boolean discardNonMatched, Object... limits) throws XMLStreamException {
        if (!isCollapsing) {
            expected = expected.replaceAll("<([^> ]+)([^>]*) />", "<$1$2></$1>");
        }
        expected = expected.replaceAll(" />", "/>"); // <foo bar="zoo" /> -> <foo bar="zoo"/>
        Map<Pattern, Integer> lims = new HashMap<>();
        for (int i = 0 ; i < limits.length ; i+=2) {
            lims.put(Pattern.compile((String) limits[i]), (Integer) limits[i + 1]);
        }
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(input));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter out = xmlOutFactory.createXMLStreamWriter(os);
        XMLStepper.limitXML(in, out, lims, countPatterns, onlyElementMatch, discardNonMatched);
        assertEquals(expected, os.toString(StandardCharsets.UTF_8),
                     "The input should be reduced properly for limits " + Strings.join(limits));
        assertLimitConvenience(input, expected, countPatterns, onlyElementMatch, discardNonMatched, limits);
        assertLimitPersistent(input, expected, countPatterns, onlyElementMatch, discardNonMatched, limits);
    }

    private void assertLimitConvenience(String input, String expected, boolean countPatterns, boolean onlyElementMatch,
                                        boolean discardNonMatched, Object... limits) throws XMLStreamException {
        if (!isCollapsing) {
            expected = expected.replaceAll("<([^> ]+)([^>]*) />", "<$1$2></$1>");
        }
        Map<Pattern, Integer> lims = new HashMap<Pattern, Integer>();
        for (int i = 0 ; i < limits.length ; i+=2) {
            lims.put(Pattern.compile((String) limits[i]), (Integer) limits[i + 1]);
        }

        String os = XMLStepper.limitXML(input, lims, countPatterns, onlyElementMatch, discardNonMatched);
        assertEquals(expected, os,
                     "The input should be convenience reduced properly for limits " + Strings.join(limits));
    }

    private void assertLimitPersistent(String input, String expected, boolean countPatterns, boolean onlyElementMatch,
                                       boolean discardNonMatched, Object... limits) throws XMLStreamException {
        if (!isCollapsing) {
            expected = expected.replaceAll("<([^> ]+)([^>]*) />", "<$1$2></$1>");
        }
        Map<Pattern, Integer> lims = new HashMap<Pattern, Integer>();
        for (int i = 0 ; i < limits.length ; i+=2) {
            lims.put(Pattern.compile((String) limits[i]), (Integer) limits[i + 1]);
        }

        XMLStepper.Limiter limiter = XMLStepper.createLimiter(lims, countPatterns, onlyElementMatch, discardNonMatched);
        String os = limiter.limit(input);
        assertEquals(expected, os,
                     "The input should be convenience reduced properly for limits " + Strings.join(limits));
    }

    // Sanity check for traversal of sub
    @Test
    public void testIterateTags() throws Exception {
        XMLStreamReader xml = xmlFactory.createXMLStreamReader(new StringReader(SAMPLE));
        assertTrue(XMLStepper.findTagStart(xml, "bar"),
                   "The first 'bar' should be findable");
        xml.next();

        final AtomicInteger count = new AtomicInteger(0);
        XMLStepper.iterateTags(xml, new XMLStepper.Callback() {
            @Override
            public boolean elementStart(
                    XMLStreamReader xml, List<String> tags, String current) throws XMLStreamException {
                count.incrementAndGet();
                return false;
            }
        });
        assertEquals(1, count.get(),
                     "Only a single content should be visited");
        assertTrue(XMLStepper.findTagStart(xml, "bar"),
                   "The second 'bar' should be findable");
    }

    private final boolean isCollapsing = writerIsCollapsing();
    @SuppressWarnings("CallToPrintStackTrace")
    private synchronized boolean writerIsCollapsing() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            XMLStreamWriter out = xmlOutFactory.createXMLStreamWriter(os);
            out.writeStartElement("foo");
            out.writeEndElement();
            out.flush();
            String xml = os.toString(StandardCharsets.UTF_8);
            return "<foo />".equals(xml) || "<foo/>".equals(xml);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Unable to determine if XMLStreamWriter collapses empty elements", e);
        }
    }

    @Test
    public void testPipePositionOnIgnoredFail() throws XMLStreamException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter out = xmlOutFactory.createXMLStreamWriter(os);
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(SAMPLE));
        assertTrue(XMLStepper.findTagStart(in, "bar"),
                   "The first 'bar' should be findable");
        XMLStepper.pipeXML(in, out, false); // until first </bar>
        assertEquals(XMLStreamConstants.CHARACTERS, in.getEventType(),
                     "The reader should be positioned at a character tag (newline) but was positioned at "
                                          + XMLUtil.eventID2String(in.getEventType()));
    }

    @Test
    public void testPipe() throws XMLStreamException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter out = xmlOutFactory.createXMLStreamWriter(os);
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(SAMPLE));
        XMLStepper.pipeXML(in, out, false);
        assertEquals(SAMPLE, os.toString(StandardCharsets.UTF_8),
                     "Piped stream should match input stream");
    }

    @Test
    public void testGetSubXML_DoubleContent() throws XMLStreamException {
        final String XML = "<field><content foo=\"bar\"/><content foo=\"zoo\"/></field>";
        final String EXPECTED = "<content foo=\"bar\"/><content foo=\"zoo\"/>";

        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(XML));
        in.next();
        String piped = XMLStepper.getSubXML(in, false, true);
        assertEquals(EXPECTED, piped,
                     "The output should contain the inner XML");
    }

    @Test
    public void testGetSubXML_NoOuter() throws XMLStreamException {
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(OUTER_FULL));
        assertTrue(XMLStepper.findTagStart(in, "foo"),
                   "The first 'foo' should be findable");
        String piped = XMLStepper.getSubXML(in, false, true);
        assertEquals(OUTER_SNIPPET, piped,
                     "The output should contain the inner XML");
    }

    @Test
    public void testGetSubXML_Outer() throws XMLStreamException {
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(OUTER_FULL));
        assertTrue(XMLStepper.findTagStart(in, "foo"),
                   "The first 'foo' should be findable");
        String piped = XMLStepper.getSubXML(in, false, false);
        assertEquals("<foo><bar>bar1</bar><bar>bar2</bar></foo>", piped,
                     "The output should contain the inner XML");
    }

    @Test
    public void testPipeComments() throws XMLStreamException {
        final String EXPECTED =
                "<bar xmlns=\"http://www.example.com/bar_ns/\">"
                + "<nam:subsub xmlns:nam=\"http://example.com/subsub_ns\">content1<!-- Sub comment --></nam:subsub>"
                + "<!-- Comment --></bar>";
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(SAMPLE));
        assertTrue(XMLStepper.findTagStart(in, "bar"),
                   "The first 'bar' should be findable");
        assertPipe(EXPECTED, in);
    }

    @Test
    public void testExtract() throws XMLStreamException {
        final String EXPECTED =
                "<bar xmlns=\"http://www.example.com/bar_ns/\">"
                + "<nam:subsub xmlns:nam=\"http://example.com/subsub_ns\">content1<!-- Sub comment --></nam:subsub>"
                + "<!-- Comment --></bar>";
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(SAMPLE));
        assertTrue(XMLStepper.findTagStart(in, "bar"),
                   "The first 'bar' should be findable");
        assertEquals(EXPECTED, XMLStepper.getSubXML(in, true));
    }

    // Currently there is no namespace repair functionality
    public void disabletestPipeNamespace() throws XMLStreamException {
        final String EXPECTED = "<bar xmlns=\"http://www.example.com/foo_ns/\">simple bar</bar>";
        XMLStreamReader in = xmlFactory.createXMLStreamReader(new StringReader(DERIVED_NAMESPACE));
        assertTrue(XMLStepper.findTagStart(in, "bar"),
                   "The first 'bar' should be findable");
        assertPipe(EXPECTED, in);
    }

    private void assertPipe(String expected, XMLStreamReader xml) throws XMLStreamException {
        String result = XMLStepper.getSubXML(xml, false);
        log.info("Sub-XML: " + result);
        assertEquals(expected, result,
                     "The piper should reproduce the desired sub section of the XML");
    }
}
