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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class StreamsTest {

    private byte[] getByteArray(int length) {
        Random random = new Random();
        byte[] result = new byte[length];
        random.nextBytes(result);
        return result;
    }

    @Test
    public void testPipe() throws Exception {
        int IN_SIZE = 200;
        byte[] inBytes = getByteArray(IN_SIZE);
        ByteArrayInputStream in = new ByteArrayInputStream(inBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream(500);
        Streams.pipe(in, out);
        byte[] outBytes = out.toByteArray();
        assertArrayEquals(inBytes, outBytes);
    }


    @Test
    public void testGetResource() throws Exception {
        String myCode = Streams.getUTF8Resource("textfile.txt");
        assertFalse(myCode.isEmpty(), "Something should be loaded");
    }
}
