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
}
