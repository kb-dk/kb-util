package dk.kb.util.reader;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class CircularCharBufferTest {
    public void testMax() {
        CircularCharBuffer b = new CircularCharBuffer(2, 2);
        b.put('a');
        b.put('b');
        try {
            b.put('c');
            fail("Adding three chars should overflow the buffer");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testExtend() {
        CircularCharBuffer b = new CircularCharBuffer(2, 3);
        b.put('a');
        b.put('b');
        b.put('c');
        try {
            b.put('d');
            fail("Adding four chars should overflow the buffer");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testWrap() {
        CircularCharBuffer b = new CircularCharBuffer(2, 3);
        b.put('a');
        b.put('b');
        b.put('c');
        assertEquals('a', b.take(),
                     "First take should work");
        b.put('d');
        assertEquals('b', b.take(),
                     "Second take should work");
        b.put('e');
        try {
            b.put('f');
            fail("Adding another char should overflow the buffer");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testAhead() {
        CircularCharBuffer b = new CircularCharBuffer(2, 3);
        b.put('a');
        b.put('b');
        b.put('c');
        assertEquals('b', b.peek(1),
                     "Peek(1) should work");
        b.take();
        b.put('d');
        assertEquals('d', b.peek(2),
                     "Peek(2) should work");
    }

    public void testGetArray() {
        CircularCharBuffer b = new CircularCharBuffer(3, 3);
        b.put("abc");
        b.take();
        b.put('d');
        char[] buf = new char[4];
        assertEquals(3, b.read(buf, 0, 4),
                     "The number of copied chars should match");
        assertEquals("bcd", new String(buf, 0, 3),
                     "The extracted chars should be correct");
    }

    public void testEmpty() {
        CircularCharBuffer b = new CircularCharBuffer(3, 3);
        try {
            b.take();
            fail("take() on empty buffer should fail");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    public void testAsCharSequence() {
        CircularCharBuffer b = new CircularCharBuffer(5, 5);

        b.put("hello");
        testAsCharSequence(b);
    }

    public void testShiftetCharSequence() {
        CircularCharBuffer b = new CircularCharBuffer(5, 5);
        b.put("zhell");
        assertEquals('z', b.take(),
                     "Get should return the first char");
        b.put('o');
        testAsCharSequence(b);

        b.clear();
        b.put("zzh");
        b.take();
        b.take();
        b.put("ello");
        testAsCharSequence(b);
    }

    public void testAsCharSequence(CircularCharBuffer b) {
        assertEquals(5, b.size());
        // To demonstrate correct behaviour
        assertEquals("ello", "hello".subSequence(1, 5).toString());
        assertEquals("ello", b.subSequence(1, 5).toString());
        assertEquals("hello", b.toString());
        assertEquals('h', b.charAt(0));
        assertEquals('e', b.charAt(1));
        assertEquals('l', b.charAt(2));
        assertEquals('l', b.charAt(3));
        assertEquals('o', b.charAt(4));

        CircularCharBuffer child = b.subSequence(0, 5);
        assertEquals("hello", child.toString());
        assertEquals(5, child.size());
        try {
            // Test the capacity of child seqs are the same as their parent's
            child.put('q');
            fail("Child buffer exceeded parent capacity");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void testIndexOf() throws Exception {
        CircularCharBuffer b = new CircularCharBuffer(5, 5);
        b.put("zhell");
        b.take();
        b.put("o");
        assertEquals(1, b.indexOf("ell"),
                     "indexOf ell should be correct");
        assertEquals(4, b.indexOf("o"),
                     "indexOf o should be correct");
        assertEquals(2, b.indexOf("l"),
                     "indexOf l should be correct");
        assertEquals(0, b.indexOf("hello"),
                     "indexOf hello should be correct");
        assertEquals(-1, b.indexOf("fnaf"),
                     "indexOf fnaf should be correct");
        assertEquals(1, b.indexOf("ello"),
                     "indexOf ello should be correct");
        assertEquals(-1, b.indexOf("elloz"),
                     "indexOf elloz should be correct");
        assertEquals(-1, b.indexOf("helloz"),
                     "indexOf helloz should be correct");
    }

    public void testLength() {
        CircularCharBuffer cb = new CircularCharBuffer(2, 2);
        cb.add("1");
        assertEquals(cb.size(), 1,
                     "add(1);");
        cb.add("2");
        assertEquals(cb.size(), 2,
                     "add(1); add(2);");
        cb.take();
        assertEquals(cb.size(), 1,
                     "add(1); add(2); take();");
        cb.take();
        assertEquals(cb.size(), 0,
                     "add(1); add(2); take(); take();");
    }

    public void testCopyDirect() {
         testCopy(10, "1234567", "1234567");
    }

    public void testCopyWrap() {
        testCopy(5, "34567", "1234567");
    }

    private void testCopy(int cbSize, String expected, String input) {
        CircularCharBuffer cb = new CircularCharBuffer(cbSize, cbSize);
        for (char c: input.toCharArray()) {
            if (cb.size() == cbSize) {
                cb.take();
            }
            cb.add(c);
        }

        final char[] OUTPUT = new char[cbSize];
        int retrieved = cb.copy(OUTPUT);

        String o = "";
        for (int i = 0; i < retrieved; i++) {
            o += OUTPUT[i];
        }
        assertEquals(expected, o,
                     "Input '" + input + "' with CB-size " + cbSize);
    }
}
