package dk.kb.util.xml;


import static dk.kb.util.xml.XMLUtil.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link XMLUtil} class
 */
public class XMLUtilTest {

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

}
