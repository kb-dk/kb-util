package dk.kb.util.xml;

import dk.kb.util.string.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static dk.kb.util.xml.DOM.XML_HEADER;
import static dk.kb.util.xml.DOM.clearXPathCache;
import static dk.kb.util.xml.DOM.selectBoolean;
import static dk.kb.util.xml.DOM.selectDouble;
import static dk.kb.util.xml.DOM.selectInteger;
import static dk.kb.util.xml.DOM.selectNode;
import static dk.kb.util.xml.DOM.selectNodeList;
import static dk.kb.util.xml.DOM.selectString;
import static dk.kb.util.xml.DOM.stringToDOM;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test cases for the {@code DOM.select*} methods
 */
public class DOMSelectTest {

    static final String SIMPLE_XML =
        XML_HEADER +
            "<body version=\"1.0\" xmlns=\"http://statsbiblioteket.dk/2010/Body\">" +
            "  <double>1.1234</double>" +
            "  <sub>" +
            "    <inner>is</inner>" +
            "  </sub>" +
            "  <boolean>true</boolean>" +
            "  <string>foobar</string>" +
            "  <integer>27</integer>" +
            "</body>";
    static final String BIG_XML;

    static {
        BIG_XML = Strings.flushLocal(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/big.xml"));
    }

    Document dom;

    @BeforeEach
    public void setUp() {
        clearXPathCache();
        dom = stringToDOM(SIMPLE_XML);
        assertNotNull(dom);
    }

    @Test
    public void testSelectInteger() {
        Integer i = selectInteger(dom, "asdfg");
        assertNull(i);

        i = selectInteger(dom, "asdfg", 1);
        assertEquals(1, i.intValue());

        i = selectInteger(dom, "/body/integer");
        assertEquals(27, i.intValue());
    }

    @Test
    public void testSelectDouble() {
        Double d = selectDouble(dom, "asdfg");
        assertNull(d);

        d = selectDouble(dom, "asdfg", 1.1);
        assertEquals(1.1, d);

        d = selectDouble(dom, "/body/double");
        assertEquals(1.1234, d);
    }

    @Test
    public void testSelectBoolean() {
        Boolean b = selectBoolean(dom, "asdfg");
        assertEquals(Boolean.FALSE, b);

        b = selectBoolean(dom, "asdfg", false);
        assertEquals(Boolean.FALSE, b);

        b = selectBoolean(dom, "asdfg", null);
        assertNull(b);

        b = selectBoolean(dom, "/body/boolean");
        assertEquals(Boolean.TRUE, b);
    }

    @Test
    public void testSelectString() {
        String s = selectString(dom, "asdfg");
        assertEquals("", s);

        s = selectString(dom, "asdfg", "sbutil");
        assertEquals("sbutil", s);

        s = selectString(dom, "asdfg", null);
        assertNull(s);

        s = selectString(dom, "/body/string");
        assertEquals("foobar", s);

        s = selectString(dom, "/body/string", "baz");
        assertEquals("foobar", s);
    }

    @Test
    public void testSelectNode() {
        Node n = selectNode(dom, "asdfg");
        assertNull(n);

        n = selectNode(dom, "/body");
        assertSame(dom.getFirstChild(), n);
    }

    @Test
    public void testSelectNodeList() {
        List<Node> l = selectNodeList(dom, "asdfg");
        assertTrue(l.isEmpty());

        // We use /body/node() because /body/* doesn't select the text nodes
        l = selectNodeList(dom, "/body/node()");
        NodeList expected = dom.getFirstChild().getChildNodes();
        assertEquals(expected.getLength(), l.size());
        assertEquals(10, l.size());
        boolean subExist = IntStream.range(0, expected.getLength())
                .anyMatch(i -> expected.item(i).getNodeName().equals("sub"));
        assertTrue(subExist, "‘sub’ isn’t found");
    }

    public void threadTest(final boolean blowCache) throws Exception {
        Thread[] threads = new Thread[20];
        final List<Throwable> errors =
                Collections.synchronizedList(new LinkedList<>());
        final Random random = new Random();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    testSelectBoolean();
                    testSelectDouble();
                    testSelectInteger();
                    testSelectNode();
                    testSelectNodeList();
                    testSelectString();

                    if (blowCache) {
                        for (int k = 0; k < 50; k++) {
                            String bleh = selectString(
                                    dom, "/body/a" + random.nextInt(),
                                    "bleh, no such node");
                            assertEquals("bleh, no such node", bleh);
                        }
                    }
                }
            });
            threads[i].setUncaughtExceptionHandler(
                    (thread, throwable) -> errors.add(throwable));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        assertTrue(errors.isEmpty(), "Uncaught exceptions in threads: " + errors);
    }

    // TODO not stable, reason should be found.
    @Test
    public void testThreadsBlowCache() throws Exception {
        threadTest(true);
    }

    @Test
    public void testThreadsWithCache() throws Exception {
        threadTest(false);
    }
}
