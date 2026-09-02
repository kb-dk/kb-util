package dk.kb.util.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link DOMSerializer} class
 */
public class DOMSerializerTest {

    private static final String SIMPLE_XML = "<root><child>text</child></root>";

    private static Document parse(String xml) {
        return XMLParser.newInstance().toDOM(xml);
    }

    @Test
    void settersAreFluent() {
        DOMSerializer serializer = DOMSerializer.newInstance();
        assertSame(serializer, serializer.setIndent(true));
        assertSame(serializer, serializer.setXmlDeclaration(true));
        assertSame(serializer, serializer.setStandAlone(true));
    }

    @Test
    void domToStringOmitsXmlDeclarationByDefault() {
        String result = DOMSerializer.newInstance().domToString(parse(SIMPLE_XML));

        assertNotNull(result);
        assertFalse(result.startsWith("<?xml"),
                    "Declaration should be omitted by default: " + result);
        assertTrue(result.contains("<child>text</child>"),
                   "Unexpected output: " + result);
    }

    @Test
    void domToStringIncludesXmlDeclarationWhenRequested() {
        String result = DOMSerializer.newInstance()
                .setXmlDeclaration(true)
                .domToString(parse(SIMPLE_XML));

        assertTrue(result.startsWith("<?xml"),
                   "Declaration should be present: " + result);
    }

    @Test
    void domToStringStandaloneFlagReflectsSetting() {
        String yes = DOMSerializer.newInstance()
                .setXmlDeclaration(true).setStandAlone(true)
                .domToString(parse(SIMPLE_XML));
        assertTrue(yes.contains("standalone=\"yes\""), "Unexpected output: " + yes);

        String no = DOMSerializer.newInstance()
                .setXmlDeclaration(true).setStandAlone(false)
                .domToString(parse(SIMPLE_XML));
        assertTrue(no.contains("standalone=\"no\""), "Unexpected output: " + no);
    }

    @Test
    void domToStringWithoutIndentIsSingleLineAndEscapesNewlines() {
        String xml = "<root>line1\nline2</root>";

        String result = DOMSerializer.newInstance()
                .setIndent(false)
                .domToString(parse(xml));

        assertFalse(result.contains("\n"),
                    "Output should contain no raw newlines: " + result);
        assertTrue(result.contains("line1&#10;line2"),
                   "Newline in text should be escaped as entity: " + result);
    }

    @Test
    void domToStringWithIndentProducesIndentedOutput() {
        String result = DOMSerializer.newInstance()
                .setIndent(true)
                .domToString(parse(SIMPLE_XML));

        assertTrue(result.contains("\n"),
                   "Indented output should contain newlines: " + result);
    }

    @Test
    void roundTripPreservesContent() {
        Document document = parse(SIMPLE_XML);

        String serialized = DOMSerializer.newInstance().domToString(document);
        Document reparsed = XMLParser.newInstance().toDOM(serialized);

        assertTrue(document.isEqualNode(reparsed),
                   "Reparsed document should equal original: " + serialized);
    }
}
