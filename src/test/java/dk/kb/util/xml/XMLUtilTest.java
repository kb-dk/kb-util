package dk.kb.util.xml;


import org.junit.jupiter.api.Test;

import static dk.kb.util.xml.XMLUtil.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link XMLUtil} class
 */
public class XMLUtilTest {

    @Test
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

    @Test
    public void testAreXmlEquivalent() {
        // Equivalent XMLs
        assertTrue(XMLUtil.areXmlEquivalent("<root><child/></root>", "<root><child /></root>"));
        assertTrue(XMLUtil.areXmlEquivalent("<root>   <child>value</child></root>", "<root><child>value</child></root>"));
        assertTrue(XMLUtil.areXmlEquivalent("<root attr='1'></root>", "<root attr='1'/>"));

        // Non-equivalent XMLs
        assertFalse(XMLUtil.areXmlEquivalent("<root><child>value</child></root>", "<root><child>differentValue</child></root>"));
        assertFalse(XMLUtil.areXmlEquivalent("<root><child attr='1'/></root>", "<root><child attr='2'/></root>"));
        assertFalse(XMLUtil.areXmlEquivalent("<root></root>", "<differentRoot></differentRoot>"));
    }

    @Test
    void compareXml() {
        String metadataXml1 =
                "<myMetadata xmlns=\"http://foobar.com/othernamespace\">" +
                        "<afield>[[FIELD]]</afield>" +
                        "</myMetadata>";
        String metadataXml2 =
                "<myMetadata xmlns=\"http://foobar.com/othernamespace\">" +
                        "<afield>123456</afield>" +
                        "</myMetadata>";
        assertFalse(XMLUtil.areXmlEquivalent(metadataXml1, metadataXml2));
    }

    @Test
    void compareXmlNamespacePrefix() {

        String metadataXml2 =
            "<myMetadata xmlns=\"http://foobar.com/othernamespace\">" +
            "<afield>123456</afield>" +
            "</myMetadata>";
        String metadataXml3 =
            "<ns:myMetadata xmlns:ns=\"http://foobar.com/othernamespace\">" +
            "<ns:afield>123456</ns:afield>\n" +
            "</ns:myMetadata>";
        assertTrue(XMLUtil.areXmlEquivalent(metadataXml2, metadataXml3));
    }

    @Test
    void compareXmlElementOrder() {
        String metadataXml1 =
            "<myMetadata xmlns=\"http://foobar.com/othernamespace\">" +
            "<bfield>123456</bfield>" +
            "<afield>123456</afield>" +
            "</myMetadata>";
        String metadataXml2 =
            "<myMetadata xmlns=\"http://foobar.com/othernamespace\">" +
            "<afield>123456</afield>" +
            "<bfield>123456</bfield>" +
            "</myMetadata>";

        assertTrue(XMLUtil.areXmlEquivalent(metadataXml1, metadataXml2));
    }
}
