/* $Id: StringsTest.java,v 1.4 2007/12/04 13:22:01 mke Exp $
 * $Revision: 1.4 $
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimingTest {

    public void testTrivial() throws InterruptedException {
        Timing timing = new Timing("foo");
        Thread.sleep(50);

        long ms = timing.getMS();
        assertTrue(ms >= 50,
                   "Timing info should be >= sleep time (50ms) but was " + ms);

        long ns = timing.getNS();
        assertTrue(ns >= 50*1000000,
                   "Timing info should be >= sleep time (50*1000000ns) but was " + ns);
    }

    public void testSub() throws InterruptedException {
        Timing timing = new Timing("foo");
        Thread.sleep(50);
        Timing subA = timing.getChild("sub_a", null, "blob");
        assertEquals(30, subA.addMS(30),
                     "Adding 30 ms should return 30 ms");
        assertEquals(40, subA.addMS(10),
                     "Adding 10 ms extra should return 40 ms");
        timing.getChild("sub_b", "#87");
        timing.getChild("sub_c").addNS(3*1000000);
        timing.getChild("sub_d").getChild("sub_d_a");
        timing.getChild("sub_e").getChild("sub_e_a");

        Thread.sleep(10);
        System.out.println("Final output: " + timing);
    }
}
