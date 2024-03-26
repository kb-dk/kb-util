/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.util.string;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CallbackReplacerTest {

    @Test
    void basicReplace() {
         CallbackReplacer allAs = new CallbackReplacer(
                 "a+", s -> "A");
         assertEquals("AnAnAs",
                      allAs.apply("anaaanas"));
    }

    @Test
    void basicReplacePattern() {
         CallbackReplacer allAs = new CallbackReplacer(
                 Pattern.compile("a+"), s -> "A");
         assertEquals("AnAnAs",
                      allAs.apply("anaaanas"));
    }

    @Test
    void callbackLogic() {
         CallbackReplacer halver = new CallbackReplacer(
                 "[0-9]+", s -> Integer.toString((Integer.parseInt(s)/2)));
         assertEquals("Here are 2 apples and 1024 oranges",
                      halver.apply("Here are 4 apples and 2048 oranges"));
    }

    @Test
    void captureGroup() {
         CallbackReplacer halver = new CallbackReplacer(
                 Pattern.compile("[a-z]=([0-9]+)"), s -> Integer.toString((Integer.parseInt(s)/2)));
         assertEquals("Person 1 says a=5 and person 2 says a=10",
                      halver.apply("Person 1 says a=10 and person 2 says a=20"));
    }

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    @Test
    void nonCaptureGroup() {
         CallbackReplacer halver = new CallbackReplacer(
                 Pattern.compile("(?:[a-z])=([0-9]+)"), s -> Integer.toString((Integer.parseInt(s)/2)));
         assertEquals("Person 1 says a=5 and person 2 says a=10",
                      halver.apply("Person 1 says a=10 and person 2 says a=20"));
    }

    @Test
    void captureGroupNoMatch() {
         CallbackReplacer halver = new CallbackReplacer(
                 Pattern.compile("[a-z]=([0-9]+)"), s -> Integer.toString((Integer.parseInt(s)/2)));
         assertEquals("Person 1 says a=A1 and person 2 says a=A2",
                      halver.apply("Person 1 says a=A1 and person 2 says a=A2"));
    }

    @Test
    void captureGroupFail() {
         try {
             new CallbackReplacer(
                     Pattern.compile("[a-z]=([0-9]+)(a-z)"), s -> Integer.toString((Integer.parseInt(s)/2)));
             throw new IllegalStateException("More than 1 capturing group is not expected to be supported");
         } catch (Exception e) {
             // Expected
         }
    }

    @Test
    void testGroupMatchFullReplace() {
        CallbackReplacer subMatcher = new CallbackReplacer(
                Pattern.compile("\\$\\{config:([^}]+)}"),
                s -> "foo",
                true);
        assertEquals("url: \"foo\"",
                subMatcher.apply("url: \"${config:some.path}\""));
    }

    @Test
    void testGroupMatchGroupReplace() {
        CallbackReplacer subMatcher = new CallbackReplacer(
                Pattern.compile("\\$\\{config:([^}]+)}"),
                s -> "foo",
                false);
        assertEquals("url: \"${config:foo}\"",
                subMatcher.apply("url: \"${config:some.path}\""));
    }

    @Test
    void streamingOutput() throws IOException {
        StringWriter out = new StringWriter();
        CallbackReplacer halver = new CallbackReplacer(
                "[0-9]+", s -> Integer.toString((Integer.parseInt(s)/2)));
        halver.apply("foo=12", out);
        String result = out.toString();
        assertEquals("foo=6", result);
    }
}