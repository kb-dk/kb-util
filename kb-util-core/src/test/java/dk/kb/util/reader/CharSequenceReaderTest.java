package dk.kb.util.reader;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link CharSequenceReader}
 */
public class CharSequenceReaderTest {

    StringBuilder buf = new StringBuilder();
    CharSequenceReader seq;

    @Test
    public void testReadManySingleChar() throws Exception {
        seq = new CharSequenceReader(buf.append("foobar"));
        assertEquals("foobar", readFullySingleChar(seq));
    }

    @Test
    public void testReadManySmallArray() throws Exception {
        seq = new CharSequenceReader(buf.append("foobar"));
        assertEquals("foobar", readFullySmallArray(seq));
    }

    @Test
    public void testReadManyBigArray() throws Exception {
        seq = new CharSequenceReader(buf.append("foobar"));
        assertEquals("foobar", readFullyBigArray(seq));
    }

    @Test
    public void testReadMoreSingleChar() throws Exception {
        seq = new CharSequenceReader(buf.append("autobiografia"));
        assertEquals("autobiografia", readFullySingleChar(seq));
    }

    @Test
    public void testReadMoreSmallArray() throws Exception {
        seq = new CharSequenceReader(buf.append("autobiografia"));
        assertEquals("autobiografia", readFullySmallArray(seq));
    }

    @Test
    public void testReadMoreBigArray() throws Exception {
        seq = new CharSequenceReader(buf.append("autobiografia"));
        assertEquals("autobiografia", readFullyBigArray(seq));
    }

    @Test
    public void testReadSingleSingleChar() throws Exception {
        seq = new CharSequenceReader(buf.append("f"));
        assertEquals("f", readFullySingleChar(seq));
    }

    @Test
    public void testReadSingleSmallArray() throws Exception {
        seq = new CharSequenceReader(buf.append("f"));
        assertEquals("f", readFullyBigArray(seq));
    }

    @Test
    public void testReadSingleBigArray() throws Exception {
        seq = new CharSequenceReader(buf.append("f"));
        assertEquals("f", readFullyBigArray(seq));
    }

    @Test
    public void testReadEmptySingleChar() throws Exception {
        seq = new CharSequenceReader(buf.append(""));
        assertEquals("", readFullySingleChar(seq));
    }

    @Test
    public void testReadEmptySmallArray() throws Exception {
        seq = new CharSequenceReader(buf.append(""));
        assertEquals("", readFullyBigArray(seq));
    }

    @Test
    public void testReadEmptyBigArray() throws Exception {
        seq = new CharSequenceReader(buf.append(""));
        assertEquals("", readFullyBigArray(seq));
    }

    public static String readFullySmallArray(Reader r) throws IOException {
        int len;
        char[] a = new char[5];
        StringBuilder tmp = new StringBuilder();

        while ((len = r.read(a)) != -1) {
            tmp.append(a, 0, len);
        }

        return tmp.toString();
    }

    public static String readFullySingleChar(Reader r) throws IOException {
        int val;
        StringBuffer tmp = new StringBuffer();

        while ((val = r.read()) != -1) {
            tmp.append((char) val);
        }

        return tmp.toString();
    }

    public static String readFullyBigArray(Reader r) throws IOException {
        int len;
        char[] a = new char[1024];
        StringBuilder tmp = new StringBuilder();

        while ((len = r.read(a)) != -1) {
            tmp.append(a, 0, len);
        }

        return tmp.toString();
    }

}
