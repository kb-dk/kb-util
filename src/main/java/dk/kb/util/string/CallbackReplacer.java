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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sub-strings matching a given regular expression are delivered to a callback that returns the replacement string.
 */
// TODO: Make this truly streaming using a buffered reader that maps to CharSequence
public class CallbackReplacer implements Function<String, String> {
    private final Pattern pattern;
    private final Function<String, String> callback;

    /**
     * @param pattern the pattern to look for.
     *                If the pattern contains no groups, the full match is passed to the callback.
     *                f it contains a single capturing group, the content of that group is passed to the callback.
     *                More than 1 group is not supported.
     * @param callback optionally adjusts the part matching the pattern. Returning null is the same as the empty String.
     */
    public CallbackReplacer(String pattern, Function<String, String> callback) {
        this(Pattern.compile(pattern), callback);
    }

    /**
     * @param pattern the pattern to look for.
     *                If the pattern contains no groups, the full match is passed to the callback.
     *                f it contains a single capturing group, the content of that group is passed to the callback.
     *                More than 1 group is not supported.
     * @param callback optionally adjusts the part matching the pattern. Returning null is the same as the empty String.
     */
    public CallbackReplacer(Pattern pattern, Function<String, String> callback) {
        this.pattern = pattern;
        this.callback = callback;
        final int groups = pattern.matcher("").groupCount();
        if (groups > 1) {
            throw new UnsupportedOperationException(String.format(
                    Locale.ROOT,  "The Pattern '%s' has %d capturing groups, while only 0 or 1 group is supported",
                    pattern.pattern(), groups));
        }
    }

    /**
     * Replaces {@link #pattern} matches in s with the result of calling {@link #callback} with the match.
     * @param s an input string for replacements.
     * @return thre result of replacing all matches in s.
     */
    @Override
    public String apply(String s) {
        Writer out = new StringWriter();
        try {
            apply(s, out);
        } catch (Exception e) {
            throw new RuntimeException("Exception during replacement", e);
        }
        return out.toString();
    }

    /**
     * Replaces {@link #pattern} matches in s with the result of calling {@link #callback} with the match.
     * @param in an input string for replacements.
     * @param out the result is written here. {@link Writer#flush()} and {@link Writer#flush()} are not called.
     */
    public void apply(String in, Writer out) throws IOException {
        Matcher matcher = pattern.matcher(in);
        int begin = 0;
        while (matcher.find()) {
            out.write(in.substring(begin, matcher.start())); // Copy up to the beginning of the match

            String replacement;
            // If there is a group in the Pattern, use the content of that one, else use the full match
            if (matcher.groupCount() == 0) { // No group
                replacement = callback.apply(matcher.group());
                out.write(replacement == null ? "" : replacement);
            } else if (matcher.groupCount() == 1) {
                replacement = callback.apply(matcher.group(1));
                out.write(in.substring(matcher.start(), matcher.start(1)));
                out.write(replacement == null ? "" : replacement);
                out.write(in.substring(matcher.end(1), matcher.end()));
            } else {
                throw new IllegalStateException(
                        "More that 1 capturing group is not supported. Pattern: '" + pattern.pattern() + "'");
            }
            begin = matcher.end();
        }
        out.write(in.substring(begin)); // Copy from eht end of last match to the end of the input
    }

    @Override
    public String toString() {
        return "CallbackReplacer(pattern='" + pattern.pattern() + "')";
    }
}
