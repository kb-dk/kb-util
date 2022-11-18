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
package dk.kb.util.xml;

import dk.kb.util.Files;
import dk.kb.util.reader.CircularCharBuffer;
import dk.kb.util.string.Strings;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class NamespaceRemoverTest {

    public void testRemoveNamespaceInformation() throws Exception {
        String[][] TESTS = new String[][]{
                {"<foo>", "<foo>"},
                {"<foo >", "<foo xmlns=\"hello\">"},
                {"<foo >", "<foo xmlns= \"hello\">"},
                {"<foo >", "<foo xmlns:boom=\"hello\">"},
                {"<foo  id=\"bar\">", "<foo xmlns=\"hello\" id=\"bar\">"},
                {"<foo  id=\"bar\">", "<foo xmlns:boom=\"hello\" id=\"bar\">"},
                {"<foo >", "<foo xmlns:boom  = \"hello\">"},
                {"<foo>", "<bar:foo>"},
                {"<foo gnuf=\"test\">", "<foo gnuf=\"test\">"},
                {"<foo gnuf=\"some:colon\">", "<foo gnuf=\"some:colon\">"},
                {"<foo gnuf=\"some:colon\">",
                 "<foo kapow:gnuf=\"some:colon\">"},
                {"</foo>", "</foo>"},
                {"</foo>", "</bar:foo>"},
                {"</foo >", "</bar:foo >"},
                {"</ foo >", "</ bar:foo >"}
        };

        NamespaceRemover remover = new NamespaceRemover(new StringReader(""));
        for (String[] test : TESTS) {
            CircularCharBuffer out = new CircularCharBuffer(100, 100);
            remover.removeNamespace(test[1], out);
            assertEquals(test[0], out.toString(),
                         "The input '" + test[1] + " should process correctly");
        }
    }

    public void testCleanFile() throws Exception {
        Reader in = new InputStreamReader(new FileInputStream(new File(
                XSLTTest.getURL("data/xml/namespace_input.xml").getFile())));
        String expected = Files.loadString(new File(
                XSLTTest.getURL("data/xml/namespace_removed.xml").getFile()));
        Reader sanitized = new NamespaceRemover(in);
        String actual = Strings.flush(sanitized);
        assertEquals(expected, actual,
                     "Namespaces should be removed");
    }

    public void testSpecificProblem() throws Exception {
        Reader in = new InputStreamReader(new FileInputStream(new File(
                XSLTTest.getURL("data/xml/specific_problem.xml").getFile())));
        Reader sanitized = new NamespaceRemover(in);
        String actual = Strings.flush(sanitized);
        System.out.println(actual);
    }

    public void testReplaceReaderMethods() {
        String orig = "foo:bar foo:attr=\"hooray\"";
        NamespaceRemover ns = new NamespaceRemover(null);

        assertEquals("bar attr=\"hooray\"", ns.transform(orig));
    }

    public void testSetSource() {
        String orig1 = "<foo:bar/>";
        String orig2 = "<bazoo:baroo/>";
        NamespaceRemover ns = new NamespaceRemover(new StringReader(orig1));
        assertEquals("<bar/>", Strings.flushLocal(ns));
        assertEquals("<baroo/>", Strings.flushLocal(ns.setSource(new StringReader(orig2))));
    }
}
