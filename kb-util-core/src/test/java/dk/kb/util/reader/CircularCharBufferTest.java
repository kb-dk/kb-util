package dk.kb.util.reader;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class CircularCharBufferTest {

    @Test
    public void testMax() {
        CircularCharBuffer b = new CircularCharBuffer(2, 2);
        b.put('a');
        b.put('b');
        assertThrows(Exception.class, () -> b.put('c'), "Adding three chars should overflow the buffer");
    }

    @Test
    public void testExtend() {
        CircularCharBuffer b = new CircularCharBuffer(2, 3);
        b.put('a');
        b.put('b');
        b.put('c');
        assertThrows(Exception.class, () -> b.put('d'), "Adding four chars should overflow the buffer");
    }

    @Test
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
        assertThrows(Exception.class, () -> b.put('f'), "Adding another char should overflow the buffer");
    }

    @Test
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

    @Test
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

    @Test
    public void testEmpty() {
        CircularCharBuffer b = new CircularCharBuffer(3, 3);
        assertThrows(NoSuchElementException.class, b::take, "take() on empty buffer should fail");
    }

    @Test
    public void testAsCharSequence() {
        CircularCharBuffer b = new CircularCharBuffer(5, 5);

        b.put("hello");
        testAsCharSequence(b);
    }

    @Test
    public void testShiftedCharSequence() {
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
        // Test the capacity of child seqs are the same as their parent's
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> child.put('q'),
                "Child buffer exceeded parent capacity");
    }

    @Test
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

    @Test
    public void testLength() {
        CircularCharBuffer cb = new CircularCharBuffer(2, 2);
        cb.add("1");
        assertEquals(1, cb.size(),
                     "add(1);");
        cb.add("2");
        assertEquals(2, cb.size(),
                     "add(1); add(2);");
        cb.take();
        assertEquals(1, cb.size(),
                     "add(1); add(2); take();");
        cb.take();
        assertEquals(0, cb.size(),
                     "add(1); add(2); take(); take();");
    }

    @ParameterizedTest
    @MethodSource("testCopyExamples")
    void testCopy(int cbSize, String expected, String input) {
        CircularCharBuffer cb = new CircularCharBuffer(cbSize, cbSize);
        for (char c: input.toCharArray()) {
            if (cb.size() == cbSize) {
                cb.take();
            }
            cb.add(c);
        }

        final char[] OUTPUT = new char[cbSize];
        int retrieved = cb.copy(OUTPUT);

        String o = new String(OUTPUT,0, retrieved);
        assertEquals(expected, o,
                     "Input '" + input + "' with CB-size " + cbSize);
    }

    private static Stream<Arguments> testCopyExamples() {
        return Stream.of(
                // Direct
                Arguments.of(10, "1234567", "1234567"),
                // With wrapping
                Arguments.of(5, "34567", "1234567")
        );
    }

}
