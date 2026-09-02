package dk.kb.util.xml;

import dk.kb.util.string.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static dk.kb.util.xml.DOM.*;
import static org.junit.jupiter.api.Assertions.*;

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
        Assertions.assertNotNull(dom);
    }

    @Test
    public void testSelectInteger() {
        Integer i = selectInteger(dom, "asdfg");
        Assertions.assertEquals(null, i);

        i = selectInteger(dom, "asdfg", 1);
        Assertions.assertEquals(1, i.intValue());

        i = selectInteger(dom, "/body/integer");
        Assertions.assertEquals(27, i.intValue());
    }

    @Test
    public void testSelectDouble() {
        Double d = selectDouble(dom, "asdfg");
        Assertions.assertEquals(null, d);

        d = selectDouble(dom, "asdfg", 1.1);
        Assertions.assertEquals(1.1, d);

        d = selectDouble(dom, "/body/double");
        Assertions.assertEquals(1.1234, d);
    }

    @Test
    public void testSelectBoolean() {
        Boolean b = selectBoolean(dom, "asdfg");
        Assertions.assertEquals(Boolean.FALSE, b);

        b = selectBoolean(dom, "asdfg", false);
        Assertions.assertEquals(Boolean.FALSE, b);

        b = selectBoolean(dom, "asdfg", null);
        Assertions.assertEquals(null, b);

        b = selectBoolean(dom, "/body/boolean");
        Assertions.assertEquals(Boolean.TRUE, b);
    }

    @Test
    public void testSelectString() {
        String s = selectString(dom, "asdfg");
        Assertions.assertEquals("", s);

        s = selectString(dom, "asdfg", "sbutil");
        Assertions.assertEquals("sbutil", s);

        s = selectString(dom, "asdfg", null);
        Assertions.assertEquals(null, s);

        s = selectString(dom, "/body/string");
        Assertions.assertEquals("foobar", s);

        s = selectString(dom, "/body/string", "baz");
        Assertions.assertEquals("foobar", s);
    }

    @Test
    public void testSelectNode() {
        Node n = selectNode(dom, "asdfg");
        Assertions.assertEquals(null, n);

        n = selectNode(dom, "/body");
        Assertions.assertSame(dom.getFirstChild(), n);
    }

    @Test
    public void testSelectNodeList() {
        List<Node> l = selectNodeList(dom, "asdfg");
        Assertions.assertEquals(0, l.size());

        // We use /body/node() because /body/* doesn't select the text nodes
        l = selectNodeList(dom, "/body/node()");
        NodeList expected = dom.getFirstChild().getChildNodes();
        Assertions.assertSame(expected.getLength(), l.size());
        Assertions.assertEquals(10, l.size());
        boolean subExist = false;
        for (int i = 0; i < expected.getLength(); i++) {
            if (expected.item(i).getNodeName().equals("sub")) {
                subExist = true;
                break;
            }
        }
        if (!subExist) {
            Assertions.fail("'sub' isn't found");
        }
    }

    public void threadTest(final boolean blowCache) throws Exception {
        Thread[] threads = new Thread[20];
        final List<Throwable> errors =
                Collections.synchronizedList(new LinkedList<Throwable>());
        final Random random = new Random();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
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
                                Assertions.assertEquals("bleh, no such node", bleh);
                            }
                        }
                    }
                }
            });
            threads[i].setUncaughtExceptionHandler(
                    new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread,
                                                      Throwable throwable) {
                            errors.add(throwable);
                        }
                    });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        if (errors.size() > 0) {
            for (Throwable t : errors) {
         //       t.printStackTrace();
            }
            Assertions.fail("Uncaught exceptions in threads");
        }
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
