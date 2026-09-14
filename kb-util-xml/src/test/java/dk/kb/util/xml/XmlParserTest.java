package dk.kb.util.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link XmlParser} class
 */
public class XmlParserTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";

    private static final String NAMESPACED_XML =
            "<root xmlns=\"http://example.com/ns\" xmlns:pre=\"http://example.com/pre\">" +
            "<pre:child>text</pre:child>" +
            "</root>";

    private static final String XML_WITH_COMMENT = "<root><!-- a comment --><child/></root>";

    @Test
    void newInstanceIsNamespaceAwareByDefault() {
        assertTrue(XmlParser.newInstance().namespaceAware());
    }

    @Test
    void settersAreFluentAndAffectState() {
        XmlParser parser = XmlParser.newInstance();
        assertSame(parser, parser.setNamespaceAware(false));
        assertFalse(parser.namespaceAware());
        assertSame(parser, parser.setNamespaceAware(true));
        assertTrue(parser.namespaceAware());
        assertSame(parser, parser.setIgnoringComments(true));
        assertSame(parser, parser.setIgnoreWhitespace(true));
        assertSame(parser, parser.setValidateDTD(false));
        assertSame(parser, parser.setSchema(null));
    }

    @Test
    void toDOMFromStringParsesDocument() throws Exception {
        Document document = XmlParser.newInstance().toDOM(SIMPLE_XML);

        assertNotNull(document);
        Element root = document.getDocumentElement();
        assertEquals("root", root.getTagName());
        assertEquals("text", root.getFirstChild().getTextContent());
    }

    @Test
    void toDOMFromInputStreamParsesDocument() throws Exception {
        ByteArrayInputStream stream =
                new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8));

        Document document = XmlParser.newInstance().toDOM(stream);

        assertNotNull(document);
        assertEquals("root", document.getDocumentElement().getTagName());
    }

    @Test
    void toDOMFromInputSourceParsesDocument() throws Exception {
        InputSource inputSource = new InputSource(new StringReader(SIMPLE_XML));

        Document document = XmlParser.newInstance().toDOM(inputSource);

        assertNotNull(document);
        assertEquals("root", document.getDocumentElement().getTagName());
    }

    @Test
    void toDOMResolvesNamespacesWhenAware() throws Exception {
        Document document = XmlParser.newInstance()
                .setNamespaceAware(true)
                .toDOM(NAMESPACED_XML);

        Element root = document.getDocumentElement();
        assertEquals("http://example.com/ns", root.getNamespaceURI());

        Element child = (Element) root.getFirstChild();
        assertEquals("http://example.com/pre", child.getNamespaceURI());
        assertEquals("child", child.getLocalName());
    }

    @Test
    void toDOMKeepsNamespaceDeclarationWhenNotAware() throws Exception {
        Document document = XmlParser.newInstance()
                .setNamespaceAware(false)
                .toDOM(NAMESPACED_XML);

        Element root = document.getDocumentElement();
        assertNull(root.getNamespaceURI());
        assertEquals("root", root.getLocalName() == null ? root.getTagName() : root.getLocalName());
    }

    @Test
    void toDOMKeepsCommentsByDefault() throws Exception {
        Document document = XmlParser.newInstance().toDOM(XML_WITH_COMMENT);

        assertTrue(hasComment(document.getDocumentElement()),
                   "Comment should be present when ignoringComments is false");
    }

    @Test
    void toDOMRemovesCommentsWhenIgnoring() throws Exception {
        Document document = XmlParser.newInstance()
                .setIgnoringComments(true)
                .toDOM(XML_WITH_COMMENT);

        assertFalse(hasComment(document.getDocumentElement()),
                    "Comment should be removed when ignoringComments is true");
    }

    @Test
    void ignoreWhitespaceParsesWithoutError() throws Exception {
        String xml = "<root>\n  <child>text</child>\n</root>";

        Document document = XmlParser.newInstance()
                .setIgnoreWhitespace(true)
                .toDOM(xml);

        assertNotNull(document);
        assertEquals("root", document.getDocumentElement().getTagName());
    }

    @Test
    void toDOMThrowsXMLExceptionOnMalformedXML() {
        XmlParser parser = XmlParser.newInstance();

        XMLException exception =
                assertThrows(XMLException.class, () -> parser.toDOM("<root><unclosed></root>"));
        assertTrue(exception.getMessage().contains("Parse error"),
                   "Unexpected message: " + exception.getMessage());
    }

    @Test
    void toDOMWithSchemaParsesConformantDocument() throws Exception {
        Schema schema = schemaFrom(
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">" +
                "<xs:element name=\"root\">" +
                "<xs:complexType><xs:sequence>" +
                "<xs:element name=\"name\" type=\"xs:string\"/>" +
                "</xs:sequence></xs:complexType>" +
                "</xs:element>" +
                "</xs:schema>");

        XmlParser parser = XmlParser.newInstance().setSchema(schema);

        // A document conforming to the schema should parse fine
        assertNotNull(parser.toDOM("<root><name>x</name></root>"));
    }

    @Test
    void newDocumentReturnsEmptyDocument() {
        Document document = XmlParser.newInstance().newDocument();

        assertNotNull(document);
        assertNull(document.getDocumentElement());
    }

    private static Schema schemaFrom(String xsd) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        return factory.newSchema(new StreamSource(new StringReader(xsd)));
    }

    private static boolean hasComment(Element element) {
        for (org.w3c.dom.Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
                return true;
            }
        }
        return false;
    }
}
