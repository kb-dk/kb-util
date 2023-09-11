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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
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

    @Test
    public void testStats() {
        Timing parent = new Timing("parent").setShowStats(Timing.MS_STATS_SIMPLE);
        parent.measure(() -> {
            if (System.currentTimeMillis() == 0) {
                throw new RuntimeException("Impossible time");
            }
        });
        Timing child = parent.getChild("child").measure(() -> {
            if (System.currentTimeMillis() == 0) {
                throw new RuntimeException("Impossible time");
            }
        });

        {
            assertFalse(parent.toString().contains("util"),
                    "Simple stats for parent should not contain utilization");
        }
        {
            parent.setShowStats(Timing.MS_STATS);
            assertTrue(parent.toString().contains("util"),
                    "Full stats should contain utilization after shange to showStats");
        }
        {
            assertFalse(child.toString().contains("util"),
                    "Simple stats for child should not contain utilization, even when parent showStat has changed");
        }
        Function<Integer, Integer> myFunction = num -> num+1;
        Function<Integer, Integer> measuredF = num -> parent.measure(() -> myFunction.apply(num));

        Stream.of(1, 2, 3).map(measuredF).collect(Collectors.toList());
    }

    @Test
    public void testMeasureRunnable() {
        AtomicLong receiver = new AtomicLong();
        new Timing("parent").measure(() -> receiver.set(87L));
        assertEquals(87L, receiver.get());
    }

    @Test
    public void testMeasureSupplier() {
        assertEquals(87L, new Timing("parent").measure(() -> 87L));
    }

    @Test
    public void testWrapFunction() {
        Timing myTimer = new Timing("timer");

        Function<Integer, Integer> incrementer = num -> num+1;
        Function<Integer, Integer> wrappedIncrementer = myTimer.wrap(incrementer);
        int sum = Stream.of(1, 2, 3).map(wrappedIncrementer).mapToInt(Integer::intValue).sum();
        assertEquals(9, sum, "Sum over incremented should match");
        assertEquals(3, myTimer.getUpdates(), "Invocation count should match");
        assertTrue(myTimer.getNS() > 0, "Invocation time should not be 0 ns");
    }

    @Test
    public void testPredicate() {
        Timing myTimer = new Timing("timer");

        Predicate<Integer> isEven = num -> (num & 1) == 0;
        Predicate<Integer> wrappedIsEven = myTimer.wrap(isEven);
        int sum = Stream.of(1, 2, 3).filter(wrappedIsEven).mapToInt(Integer::intValue).sum();
        assertEquals(2, sum, "Sum over isEven should match");
        assertEquals(3, myTimer.getUpdates(), "Invocation count should match");
        assertTrue(myTimer.getNS() > 0, "Invocation time should not be 0 ns");
    }

    @Test
    public void testWrapConsumer() {
        Timing myTimer = new Timing("timer");
        List<Integer> myNumbers = new ArrayList<>();
        Consumer<Integer> myConsumer = myNumbers::add;
        Consumer<Integer> wrappedConsumer = myTimer.wrap(myConsumer);
        Stream.of(1, 2, 3).forEach(wrappedConsumer);
        assertEquals(3, myNumbers.size(), "Collected count shoudl match");
        assertEquals(3, myTimer.getUpdates(), "Invocation count should match");
        assertTrue(myTimer.getNS() > 0, "Invocation time should not be 0 ns");
    }
}
