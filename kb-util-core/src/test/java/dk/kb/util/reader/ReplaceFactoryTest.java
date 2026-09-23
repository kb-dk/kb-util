/* $Id$
 *
 * The Summa project.
 * Copyright (C) 2005-2008  The State and University Library
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
package dk.kb.util.reader;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ReplaceFactoryTest {

    @Test
    public void testThreeCases() {
        Map<String, String> rules =
                ReplacePerformanceTest.getRangeReplacements(300, 1, 1, 1, 1);
        assertInstanceOf(CharReplacer.class, ReplaceFactory.getReplacer(rules),
                "1=>1 should yield a CharReplacer");
        assertInstanceOf(CharReplacer.class, new ReplaceFactory(rules).getReplacer(),
                "1=>1 should yield a CharReplacer with get");

        rules = ReplacePerformanceTest.getRangeReplacements(300, 1, 1, 0, 1);
        assertInstanceOf(CharArrayReplacer.class, ReplaceFactory.getReplacer(rules),
                "1=>0-1 should yield a CharArrayReplacer");
        assertInstanceOf(CharArrayReplacer.class, new ReplaceFactory(rules).getReplacer(),
                "1=>0-1 should yield a CharArrayReplacer with get");

        rules = ReplacePerformanceTest.getRangeReplacements(300, 1, 1, 0, 5);
        assertInstanceOf(CharArrayReplacer.class, ReplaceFactory.getReplacer(rules),
                "1=>0-5 should yield a CharArrayReplacer");
        assertInstanceOf(CharArrayReplacer.class, new ReplaceFactory(rules).getReplacer(),
                "1=>0-5 should yield a CharArrayReplacer with get");

        rules = ReplacePerformanceTest.getRangeReplacements(300, 1, 1, 1, 5);
        assertInstanceOf(CharArrayReplacer.class, ReplaceFactory.getReplacer(rules),
                "1=>1-5 should yield a CharArrayReplacer");

        rules = ReplacePerformanceTest.getRangeReplacements(300, 1, 5, 0, 5);
        assertInstanceOf(StringReplacer.class, ReplaceFactory.getReplacer(rules),
                "1-5=>1-5 should yield a StringReplacer");
    }

    @Test
    public void testComplexFactory() throws Exception {
        Map<String, String> rules = Map.of(StringReplacerTest.JAVASCRIPT, "");
        ReplaceFactory factory = new ReplaceFactory(rules);
        ReplaceReader replacer = factory.getReplacer();
        String actual =
                replacer.transform(StringReplacerTest.JAVASCRIPT + "foo");
        assertEquals("foo", actual,
                     "Complex factory based replacement should work");
    }

    /* This used to fail due to a missing proper initialization of minBufferSize
       when using a factory together with stream bases replacing.
     */
    @Test
    public void testComplexFactoryStream() throws Exception {
        Map<String, String> rules = Map.of(StringReplacerTest.JAVASCRIPT, "");
        ReplaceFactory factory = new ReplaceFactory(rules);
        StringReader ir =
                new StringReader(StringReplacerTest.JAVASCRIPT + "foo");
        ReplaceReader replacer = factory.getReplacer(ir);
        CircularCharBuffer actual =
                new CircularCharBuffer(10, Integer.MAX_VALUE);
        replacer.read(actual, Integer.MAX_VALUE);
        assertEquals("foo", actual.toString(),
                     "Complex factory & stream based replacement should work");
    }
}
