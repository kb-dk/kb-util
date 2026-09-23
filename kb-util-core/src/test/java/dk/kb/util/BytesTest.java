package dk.kb.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * FIXME: Missing class docs for dk.statsbiblioteket.util.BytesTest
 *
 * @author mke
 * @since Sep 24, 2009
 */
public class BytesTest  {

    @ParameterizedTest
    @MethodSource("md5TestExamples")
    public void testToHexOnMD5Digest(String test, String expected) {
        assertEquals(expected, Bytes.toHex(Checksums.md5(test)));
    }

    public static Stream<Arguments> md5TestExamples() {
        // Contains pairs of inputString followed by the output
        // of the Unix command 'echo -ne "<inputString>" | md5sum'
        return Stream.of(Arguments.of("foo", "acbd18db4cc2f85cedef654fccc4a4d8"),
                Arguments.of("foo\n", "d3b07384d113edec49eaa6238ad5ff00"),
                Arguments.of("foofoo\n", "79c509301a936b89617dab2a632c23ac"));
    }

    @Test
    void includesLeadingZeroes() {
        byte[] input = { 0, 0, 19 };
        assertEquals("000013", Bytes.toHex(input));
    }

    @Test
    void returnsBuf() {
        Appendable buf = new StringBuilder();
        Appendable receivedBuf = Bytes.toHex(buf, new byte[] { 9 });
        assertSame(buf, receivedBuf);
        assertEquals("09", buf.toString());
    }

}
