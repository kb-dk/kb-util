/* $Id: StreamsTest.java,v 1.3 2007/12/04 13:22:01 mke Exp $
 * $Revision: 1.3 $
 * $Date: 2007/12/04 13:22:01 $
 * $Author: mke $
 *
 * The SB Util Library.
 * Copyright (C) 2005-2007  The State and University Library of Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class StreamsTest {

    private byte[] getByteArray(int length) {
        Random random = new Random();
        byte[] result = new byte[length];
        random.nextBytes(result);
        return result;
    }

    @Test
    public void testPipe() throws Exception {
        int INSIZE = 200;
        byte[] inbytes = getByteArray(INSIZE);
        ByteArrayInputStream in = new ByteArrayInputStream(inbytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream(500);
        Streams.pipe(in, out);
        byte[] outbytes = out.toByteArray();
        assertEquals(inbytes.length, outbytes.length,
                     "Input and output streams should be the same size");
        for (int i = 0; i < INSIZE; i++) {
            assertEquals(inbytes[i], outbytes[i],
                         "The content at position " + i + " should be equal. " +
                         "In was " + inbytes[i] + " out was " + outbytes[i]);
        }
    }


    @Test
    public void testGetResource() throws Exception {
        String myCode = Streams.getUTF8Resource("dots.yml");
        assertTrue(myCode.length() > 0, "Something should be loaded");
    }
}
