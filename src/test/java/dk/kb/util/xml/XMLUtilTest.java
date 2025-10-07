package dk.kb.util.xml;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.util.Map;

import static dk.kb.util.xml.XMLUtil.encode;
import static dk.kb.util.xml.XMLUtil.logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link XMLUtil} class
 */
public class XMLUtilTest {

    private static final Logger log = LoggerFactory.getLogger(XMLUtilTest.class);

    @Test
    void parsesGeneratedStringBack() {
        Document originalDocument = XMLUtil.createDocument();
        Element rootElement = XMLUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest", Map.of("schemaUri", "schemaUriTest"));
        XMLUtil.addElementWithTextContent(rootElement, "myChildTag", "Some text");
        String xml = XMLUtil.getStringFromDocument(originalDocument);
        Document parsedDocument = XMLUtil.parseDocument(xml);

        assertTrue(parsedDocument.isEqualNode(originalDocument),
                () -> "\nExpected:\n" + xml + "\nbut was:\n" + XMLUtil.getStringFromDocument(parsedDocument));
    }

    @Test
    void testRemoveElement() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "<transcodingStatus>pending</transcodingStatus>" +
                        "<transcodingReady>true</transcodingReady>" +
                        "</radiotvTranscodingStatus>";
        String updatedStatus = XMLUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
        assertTrue(areXmlEquivalent(
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "<transcodingReady>true</transcodingReady>" +
                        "</radiotvTranscodingStatus>",
                updatedStatus));
    }

    @Test
    void testRemoveNonExistingElement() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "<transcodingReady>true</transcodingReady>" +
                        "</radiotvTranscodingStatus>";
        String updatedStatus = XMLUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
        assertTrue(areXmlEquivalent(oldStatusMetadata, updatedStatus));
    }

    @Test
    void testCreateDocumentWithRootElementNoExtraAttributes() {
        Document document = XMLUtil.createDocument();
        XMLUtil.addRootElementToDocument(document, "testRootElement", "testxmlns");
        assertTrue(areXmlEquivalent(
                "<testRootElement xmlns=\"testxmlns\"/>", XMLUtil.getStringFromDocument(document)));
    }

    @Test
    void testCreateDocumentWithRootElementWith1ExtraAttribute() {
        Document document = XMLUtil.createDocument();
        Map<String, String> attributeMap = Map.of("schemaUri", "testSchemaUri", "xmlns:xs", "extraNamespace");
        XMLUtil.addRootElementToDocument(
                document, "testRootElement", "testxmlns", attributeMap);
        assertTrue(areXmlEquivalent(
                "<testRootElement xmlns=\"testxmlns\" xmlns:xs=\"extraNamespace\" schemaUri=\"testSchemaUri\"/>",
                XMLUtil.getStringFromDocument(document)));
    }

    @Test
    void testAddElement() {
        Document originalDocument = XMLUtil.createDocument();
        Element rootElement = XMLUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest");
        XMLUtil.addElement(rootElement, "myElementTag");
        String xml = XMLUtil.getStringFromDocument(originalDocument);
        assertTrue(areXmlEquivalent(
                "<myRootTag xmlns=\"xmlnsTest\"><myElementTag/></myRootTag>", xml));
    }

    @Test
    void testAddElementWithTextContent() {
        Document originalDocument = XMLUtil.createDocument();
        Element rootElement = XMLUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest");
        XMLUtil.addElementWithTextContent(rootElement, "myElementTag", "Some text");
        String xml = XMLUtil.getStringFromDocument(originalDocument);
        assertTrue(areXmlEquivalent(
                "<myRootTag xmlns=\"xmlnsTest\"><myElementTag>Some text</myElementTag></myRootTag>", xml));
    }

    public void testEncode() {
        assertEquals("&gt;", encode(">"));
        assertEquals("&lt;", encode("<"));
        assertEquals("&amp;", encode("&"));
        assertEquals("&quot;", encode("\""));
        assertEquals("&apos;", encode("'"));

        assertEquals("&amp;amp;", encode("&amp;"));
        assertEquals("&amp;&amp;", encode("&&"));
        assertEquals("&quot;+", encode("\"+"));
    }

    /**
     * Checks for XML equivalence between two XML objects, ignoring whitespace and element order.
     *
     * @return true if the two documents are semantically equivalent.
     */
    public static boolean areXmlEquivalent(String xml1, String xml2) {
        Diff diffs = DiffBuilder.compare(Input.fromString(xml1))
                .withTest(Input.fromString(xml2))
                .ignoreComments()
                .ignoreElementContentWhitespace()
                .build();
        if (!diffs.hasDifferences()) {
            return true;
        } else {
            for (Object diff : diffs.getDifferences()) {
                logger.debug("Found metadata diff: {}", diff);
            }
            return false;
        }
    }

}
