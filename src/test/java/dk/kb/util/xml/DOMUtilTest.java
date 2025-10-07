package dk.kb.util.xml;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.TransformerException;
import java.util.Map;

import static dk.kb.util.xml.DOMUtil.logger;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link DOMUtil} class
 */
public class DOMUtilTest {

    private static final Logger log = LoggerFactory.getLogger(DOMUtilTest.class);

    @Test
    void parsesGeneratedStringBack() throws TransformerException {
        Document originalDocument = DOMUtil.createDocument();

        Element rootElement = DOMUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest", Map.of("schemaUri", "schemaUriTest"));

        DOMUtil.addElementWithTextContent(rootElement, "myChildTag", "Some text");

        String xml = DOM.domToString(originalDocument);

        Document parsedDocument = DOM.stringToDOM(xml);
        String parsedDocumentXml = DOM.domToString(parsedDocument);

        assertTrue(parsedDocument.isEqualNode(originalDocument),
                   () -> "\nExpected:\n" + xml + "\nbut was:\n" + parsedDocumentXml);
    }

    @Test
    void testRemoveElement() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "<transcodingStatus>pending</transcodingStatus>" +
                        "<transcodingReady>true</transcodingReady>" +
                        "</radiotvTranscodingStatus>";
        String updatedStatus = DOMUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
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
        String updatedStatus = DOMUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
        assertTrue(areXmlEquivalent(oldStatusMetadata, updatedStatus));
    }

    @Test
    void testCreateDocumentWithRootElementNoExtraAttributes() throws TransformerException {
        Document document = DOMUtil.createDocument();
        DOMUtil.addRootElementToDocument(document, "testRootElement", "testxmlns");
        assertTrue(areXmlEquivalent(
                "<testRootElement xmlns=\"testxmlns\"/>", DOM.domToString(document, false)));
    }

    @Test
    void testCreateDocumentWithRootElementWith1ExtraAttribute() throws TransformerException {
        Document document = DOMUtil.createDocument();
        Map<String, String> attributeMap = Map.of("schemaUri", "testSchemaUri", "xmlns:xs", "extraNamespace");
        DOMUtil.addRootElementToDocument(
                document, "testRootElement", "testxmlns", attributeMap);
        assertTrue(areXmlEquivalent(
                "<testRootElement xmlns=\"testxmlns\" xmlns:xs=\"extraNamespace\" schemaUri=\"testSchemaUri\"/>",
                DOM.domToString(document, false)));
    }

    @Test
    void testAddElement() throws TransformerException {
        Document originalDocument = DOMUtil.createDocument();
        Element rootElement = DOMUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest");
        DOMUtil.addElement(rootElement, "myElementTag");
        String xml = DOM.domToString(originalDocument, false);
        assertTrue(areXmlEquivalent(
                "<myRootTag xmlns=\"xmlnsTest\"><myElementTag/></myRootTag>", xml));
    }

    @Test
    void testAddElementWithTextContent() throws TransformerException {
        Document originalDocument = DOMUtil.createDocument();
        Element rootElement = DOMUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest");
        DOMUtil.addElementWithTextContent(rootElement, "myElementTag", "Some text");
        String xml = DOM.domToString(originalDocument, false);
        assertTrue(areXmlEquivalent(
                "<myRootTag xmlns=\"xmlnsTest\"><myElementTag>Some text</myElementTag></myRootTag>", xml));
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
