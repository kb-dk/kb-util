package dk.kb.util;

import java.io.IOException;
import java.util.HexFormat;

/**
 * Utillity class for working with bytes, bytearrays, and strings.
 */
public class Bytes {
    /**
     * The hexadecimal characters in numeric order from {@code 0-f}
     */
    public static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * The number of bits in a nibble (used for shifting).
     */
    private static final byte BITS_IN_NIBBLE = 4;
    /**
     * A bitmask for a nibble (used for "and'ing" out the bits.
     */
    private static final byte BITMASK_FOR_NIBBLE = 0x0f;

    /**
     * Utility class, don't initialise.
     */
    private Bytes() {
    }

    /**
     * Converts a byte array to a hex-string.
     *
     * @param ba the bytearray to be converted
     * @return ba the byte array to convert to a hex-string
     *
     * @deprecated Use {@code Hexformat.of().toHexDigits(ba)}.
     */
    @Deprecated
    public static String toHex(final byte[] ba) {
        return HexFormat.of().formatHex(ba);
    }

    /**
     * Converts a byte array to a hex-string and write the result to an
     * {@code Appendable} (such as a {@code StringBuilder}).
     *
     * @param buf the appendable to write to.
     * @param ba  the byte array to convert to a hex-string.
     * @return always returns {@code buf}.
     *
     * @deprecated Use {@code HexFormat.of().formatHex(buf, ba)}.
     */
    public static Appendable toHex(Appendable buf, byte[] ba) {
        return HexFormat.of().formatHex(buf, ba);
    }
}
