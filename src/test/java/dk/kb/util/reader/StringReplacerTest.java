package dk.kb.util.reader;

import dk.kb.util.string.Strings;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ReplaceReader Tester.
 */
@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class StringReplacerTest {

    public void testSimpleReplacement() throws IOException {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        map.put("b", "bar");
        assertEquals("mfoonyfooffool bar", getReplaced(map, "manyafal b"),
                     "Simple replacement should work");
    }

    public void testTrivialReplacement() throws IOException {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        map.put("b", "bar");
        assertEquals("foo", getReplaced(map, "a"),
                     "Trivial replacement should work");
    }

    public static final String JAVASCRIPT =
            "<script language=\"javascript\">function openwidnowb(linkname)"
            + "{window.open (linkname,\"_blank\",\"resizable=yes,location=1"
            + ",status=1,scrollbars=1\");} </script><script language=\"java"
            + "script\">function openwidnowb(linkname){window.open (linknam"
            + "e,\"_blank\",\"resizable=yes,location=1,status=1,scrollbars="
            + "1\");} </script>";

    public void testComplex() throws Exception {
        Map<String, String> rules = new HashMap<String, String>(10);
        rules.put(JAVASCRIPT, "");
        assertEquals("foo", getReplaced(rules, JAVASCRIPT + "foo"),
                     "Complex replacement should work");
    }

    public void testLongTargetOnStream() throws Exception {
        Map<String, String> rules = new HashMap<String, String>(10);
        rules.put(JAVASCRIPT, "");
        Reader replacedReader = new StringReplacer(new StringReader(
                JAVASCRIPT + "foo"), rules);
        StringWriter out = new StringWriter(100);
        int c;
        while ((c = replacedReader.read()) != -1) {
            out.write(c);
        }
        assertEquals("foo", out.toString(),
                     "Target should be removed");

    }

    public void testPriority() throws IOException {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        map.put("aa", "bar");
        assertEquals("barfoo", getReplaced(map, "aaa"),
                     "Priority should work for foo and bar");
    }

    public void testPriority2() throws IOException {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        map.put("aa", "bar");
        map.put("aaa", "zoo");
        assertEquals("zoo", getReplaced(map, "aaa"),
                     "Zoo-priority should work");
    }

    public void testMisc() throws IOException {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        map.put("aa", "bar");
        map.put("aaa", "zoo");
        //noinspection DuplicateStringLiteralInspection
        assertEquals("ffreege", getReplaced(map, "ffreege"),
                     "None-test should work");

        map.put("baa", "zap");
        assertEquals("barzapfoo", getReplaced(map, "aabaaa"),
                     "Mix-test should work");

        assertEquals("", getReplaced(map, ""),
                     "no-input-test should work");

        map.clear();
        //noinspection DuplicateStringLiteralInspection
        assertEquals("klamm", getReplaced(map, "klamm"),
                     "No-rules-test should work");

    }

    public void testIncreasing() throws Exception {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        for (int i = 0; i < 100; i++) {
            StringWriter sw = new StringWriter(i);
            for (int j = 0; j < i; j++) {
                sw.append(Integer.toString(j % 10));
            }
            assertEquals(sw.toString(), getReplaced(map, sw.toString()),
                         "Input of length " + i + " should work");
        }
    }

    public void testBufferSizePlusOne() throws Exception {
        Map<String, String> map = new HashMap<String, String>(10);
        map.put("a", "foo");
        assertEquals("12345678901", getReplaced(map, "12345678901"),
                     "Input of length 11 should work");
    }

    private String getReplaced(Map<String, String> map, String source)
            throws IOException {
        StringReader in = new StringReader(source);
        StringReplacer replacer = new StringReplacer(in, map);
        StringWriter sw = new StringWriter(100);
        int c;

        while ((c = replacer.read()) != -1) {
            sw.append("").append((char) c);
        }
        return sw.toString();
    }

    public void testSetSource() throws Exception {
        StringReplacer rep = new StringReplacer(
                new StringReader("foo"), new HashMap<String, String>());
        assertEquals("foo", Strings.flushLocal(rep));

        rep.setSource(new StringReader("bar"));
        assertEquals("bar", Strings.flushLocal(rep));
    }
}
