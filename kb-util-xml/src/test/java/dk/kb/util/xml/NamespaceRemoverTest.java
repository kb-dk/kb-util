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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class NamespaceRemoverTest {

    @ParameterizedTest
    @MethodSource("removeNamespaceTestExamples")
    public void testRemoveNamespaceInformation(String expected, String tag) throws Exception {
        try (NamespaceRemover remover = new NamespaceRemover(new StringReader(""))){
            CircularCharBuffer out = new CircularCharBuffer(100, 100);
            remover.removeNamespace(tag, out);
            assertEquals(expected, out.toString(),
                    "The input '" + tag + "' should process correctly");
        }
    }

    public static Stream<Arguments> removeNamespaceTestExamples() {
        return  Stream.of(
                Arguments.of("<foo>", "<foo>"),
                Arguments.of("<foo >", "<foo xmlns=\"hello\">"),
                Arguments.of("<foo >", "<foo xmlns= \"hello\">"),
                Arguments.of("<foo >", "<foo xmlns:boom=\"hello\">"),
                Arguments.of("<foo  id=\"bar\">", "<foo xmlns=\"hello\" id=\"bar\">"),
                Arguments.of("<foo  id=\"bar\">", "<foo xmlns:boom=\"hello\" id=\"bar\">"),
                Arguments.of("<foo >", "<foo xmlns:boom  = \"hello\">"),
                Arguments.of("<foo>", "<bar:foo>"),
                Arguments.of("<foo gnuf=\"test\">", "<foo gnuf=\"test\">"),
                Arguments.of("<foo gnuf=\"some:colon\">", "<foo gnuf=\"some:colon\">"),
                Arguments.of("<foo gnuf=\"some:colon\">", "<foo kapow:gnuf=\"some:colon\">"),
                Arguments.of("</foo>", "</foo>"),
                Arguments.of("</foo>", "</bar:foo>"),
                Arguments.of("</foo >", "</bar:foo >"),
                Arguments.of("</ foo >", "</ bar:foo >"));
    }

    @Test
    public void testCleanFile() throws Exception {
        Reader in = new InputStreamReader(new FileInputStream(
                XSLTTest.getURL("data/xml/namespace_input.xml").getFile()), StandardCharsets.UTF_8);
        String expected = Files.loadString(new File(
                XSLTTest.getURL("data/xml/namespace_removed.xml").getFile()));
        try (Reader sanitized = new NamespaceRemover(in)) {
            String actual = Strings.flush(sanitized);
            assertEquals(expected, actual, "Namespaces should be removed");
        }
    }

    @Test
    public void testSpecificProblem() throws Exception {
        Reader in = new InputStreamReader(new FileInputStream(
                XSLTTest.getURL("data/xml/specific_problem.xml").getFile()), StandardCharsets.UTF_8);
        Reader sanitized = new NamespaceRemover(in);
        String actual = Strings.flush(sanitized);
        System.out.println(actual);
    }

    @Test
    public void testReplaceReaderMethods() throws IOException {
        String orig = "foo:bar foo:attr=\"hooray\"";
        try (NamespaceRemover ns = new NamespaceRemover(null)) {
            assertEquals("bar attr=\"hooray\"", ns.transform(orig));
        }
    }

    @Test
    public void testSetSource() throws IOException {
        String orig1 = "<foo:bar/>";
        String orig2 = "<bazoo:baroo/>";
        try (NamespaceRemover ns = new NamespaceRemover(new StringReader(orig1))) {
            assertEquals("<bar/>", Strings.flushLocal(ns));
            assertEquals("<baroo/>", Strings.flushLocal(ns.setSource(new StringReader(orig2))));
        }
    }
}
