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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class BaselineReplacerTest {

    @Test
    public void testSimpleReplacement() throws IOException {
        Map<String, String> map = Map.of("a", "foo", "b", "bar");
        assertEquals("mfoonyfooffool bar", getReplacedBaseline(map, "manyafal b"),
                "Simple replacement should work");
    }

    @Test
    public void testTrivialReplacement() throws IOException {
        Map<String, String> map = Map.of("a", "foo");
        assertEquals("foo", getReplacedBaseline(map, "a"),
                     "Trivial replacement should work");
    }

    @Test
    public void testSingleCharReplacement() throws IOException {
        Map<String, String> map = Map.of("a", "b", "b", "c");
        assertEquals("bcde", getReplacedBaseline(map, "abde"),
                     "Single-char replacement should work");
    }

    @Test
    public void testMisc() throws IOException {
        Map<String, String> map = new HashMap<>(Map.of("a", "foo", "aa", "bar", "aaa", "zoo"));
        //noinspection DuplicateStringLiteralInspection
        assertEquals("ffreege", getReplacedBaseline(map, "ffreege"),
                     "None-test should work");

        map.put("baa", "zap");
        assertEquals("barzapfoo", getReplacedBaseline(map, "aabaaa"),
                     "Mix-test should work");

        assertEquals("", getReplacedBaseline(map, ""),
                     "no-input-test should work");

        //noinspection DuplicateStringLiteralInspection
        assertEquals("klamm", getReplacedBaseline(Map.of(), "klamm"),
                     "No-rules-test should work");
    }

    public static String getReplacedBaseline(Map<String, String> rules,
                                             String source) throws IOException {
        StringReader in = new StringReader(source);
        BaselineReplacer replacer = new BaselineReplacer(in, rules);
        StringWriter sw = new StringWriter(100);
        int c;

        while ((c = replacer.read()) != -1) {
            sw.append("").append((char) c);
        }
        return sw.toString();
    }

}
