package dk.kb.util.xml;


import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.matchers.CompareMatcher;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link DOMUtil} class
 */
public class DOMUtilTest {

    @Test
    void parsesGeneratedStringBack() {
        Document originalDocument = DOMUtil.createDocument();

        Element rootElement = DOMUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest", Map.of("schemaUri", "schemaUriTest"));

        DOMUtil.addElement(rootElement, "myChildTag").setTextContent("Some text");

        String xml = DOM.domToString(originalDocument);

        Document parsedDocument = DOM.stringToDOM(xml);
        String parsedDocumentXml = DOM.domToString(parsedDocument);

        assertTrue(parsedDocument.isEqualNode(originalDocument),
                   () -> "\nExpected:\n" + xml + "\nbut was:\n" + parsedDocumentXml);
    }

    @Test
    void testRemoveElement() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<transcodingStatus>pending</transcodingStatus>"
                + "<transcodingReady>true</transcodingReady>"
                + "</radiotvTranscodingStatus>";
        String updatedStatus = DOMUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
        assertThat(updatedStatus,
                   CompareMatcher.isIdenticalTo(
                           "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                           + "<transcodingReady>true</transcodingReady>"
                           + "</radiotvTranscodingStatus>"));
    }

    @Test
    void testRemoveElementNS() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<transcodingStatus>pending</transcodingStatus>"
                + "<transcodingReady>true</transcodingReady>"
                + "</radiotvTranscodingStatus>";
        String updatedStatus = DOMUtil.removeElementFromXml(oldStatusMetadata,
                                                            "transcodingStatus",
                                                            "http://id.kb.dk/schemas/radiotv_access/transcoding_status");

        assertThat(updatedStatus,
                   CompareMatcher.isIdenticalTo(
                           "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                           + "<transcodingReady>true</transcodingReady>"
                           + "</radiotvTranscodingStatus>"));
    }

    @Test
    void testRemoveElementDocument() {
        Element oldStatusMetadata = DOM.stringToDOM(
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<transcodingStatus>pending</transcodingStatus>"
                + "<transcodingReady>true</transcodingReady>"
                + "</radiotvTranscodingStatus>").getDocumentElement();
        String updatedStatus = DOM.domToString(DOMUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus"));
        assertThat(updatedStatus,
                   CompareMatcher.isIdenticalTo(
                           "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                           + "<transcodingReady>true</transcodingReady>"
                           + "</radiotvTranscodingStatus>"));
    }

    @Test
    void testRemoveNonExistingElement() {
        String oldStatusMetadata =
                "<radiotvTranscodingStatus xmlns=\"http://id.kb.dk/schemas/radiotv_access/transcoding_status\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<transcodingReady>true</transcodingReady>"
                + "</radiotvTranscodingStatus>";
        String updatedStatus = DOMUtil.removeElementFromXml(oldStatusMetadata, "transcodingStatus");
        assertThat(updatedStatus,
                   CompareMatcher.isIdenticalTo(oldStatusMetadata));
    }

    @Test
    void testCreateDocumentWithRootElementNoExtraAttributes() {
        Document document = DOMUtil.createDocument();
        DOMUtil.addRootElementToDocument(document, "testRootElement", "testxmlns");
        assertThat(DOM.domToString(document),
                   CompareMatcher.isIdenticalTo("<testRootElement xmlns=\"testxmlns\"/>"));
    }

    @Test
    void testCreateDocumentWithRootElementWith1ExtraAttribute() {
        Document document = DOMUtil.createDocument();
        Map<String, String> attributeMap = Map.of("schemaUri", "testSchemaUri", "xmlns:xs", "extraNamespace");
        DOMUtil.addRootElementToDocument(
                document, "testRootElement", "testxmlns", attributeMap);
        assertThat(DOM.domToString(document),
                   CompareMatcher.isIdenticalTo(
                           "<testRootElement xmlns=\"testxmlns\" xmlns:xs=\"extraNamespace\" schemaUri=\"testSchemaUri\"/>"));
    }

    @Test
    void testAddElement() {
        Document originalDocument = DOMUtil.createDocument();
        Element rootElement = DOMUtil.addRootElementToDocument(
                originalDocument, "myRootTag", "xmlnsTest");
        DOMUtil.addElement(rootElement, "myElementTag");
        String xml = DOM.domToString(originalDocument);
        assertThat(xml, CompareMatcher.isIdenticalTo("<myRootTag xmlns=\"xmlnsTest\"><myElementTag/></myRootTag>"));
    }

    @Test
    void testParseValidXml() {
        String validXml = "<root><child>value</child></root>";
        Document document = DOMUtil.parseDocument(validXml);
        assertNotNull(document, "Document should not be null for valid XML.");
        assertEquals("root", document.getDocumentElement().getNodeName(), "Root element name should be 'root'.");
    }

    @Test
    void testParseEmptyXml() {
        String emptyXml = "";
        assertThrows(XMLException.class, () -> {
            DOMUtil.parseDocument(emptyXml);
        }, "Parsing empty XML should throw XMLException.");
    }

    @Test
    void testParseMalformedXml() {
        String malformedXml = "<root><child></root>"; // Missing closing tag for <child>
        assertThrows(XMLException.class, () -> {
            DOMUtil.parseDocument(malformedXml);
        }, "Parsing malformed XML should throw XMLException.");
    }

    @Test
    void testParseXmlWithNamespaces() {
        String xmlWithNamespace = "<root xmlns=\"http://example.com\"><child>value</child></root>";
        Document document = DOMUtil.parseDocument(xmlWithNamespace);
        assertNotNull(document, "Document should not be null for valid XML with namespaces.");
        assertEquals("root", document.getDocumentElement().getNodeName(), "Root element name should be 'root'.");
        assertEquals("http://example.com", document.getDocumentElement().getNamespaceURI(), "Namespace URI should match.");
    }

    @Test
    void testParseXmlWithInvalidCharacters() {
        String invalidXml = "<root><child>Invalid character: &#x1F;</child></root>"; // Invalid XML character
        assertThrows(XMLException.class, () -> {
            DOMUtil.parseDocument(invalidXml);
        }, "Parsing XML with invalid characters should throw XMLException.");
    }

}
