package dk.kb.util.webservice.stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.CharArrayWriter;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
class JSONStreamWriterTest {

    @Tag("fast")
    @Test
    void testBasicWriterEmptyJSON() {
        assertEquals("""
                     [
                     
                     ]
                     """, getBookJSON(0, JSONStreamWriter.FORMAT.json));
    }

    @Tag("fast")
    @Test
    void testBasicWriterSingleJSON() {
        assertEquals("""
                     [
                     {"id":"0","title":"book #0"}
                     ]
                     """, getBookJSON(1, JSONStreamWriter.FORMAT.json));
    }

    @Tag("fast")
    @Test
    void testBasicWriterMultiJSON() {
        assertEquals("""
                     [
                     {"id":"0","title":"book #0"},
                     {"id":"1","title":"book #1"}
                     ]
                     """, getBookJSON(2, JSONStreamWriter.FORMAT.json));
    }

    @Tag("fast")
    @Test
    void testCustomWriterMulti() {
        assertEquals("""
                     <
                     {"id":"0","title":"b00k #0"}; \
                     {"id":"1","title":"b00k #1"}
                     >
                     """, getCustomBookJSON(2, JSONStreamWriter.FORMAT.json));
    }

    @Tag("fast")
    @Test
    void testBasicWriterSingleJSONL() {
        assertEquals("""
                     {"id":"0","title":"book #0"}
                     """,
                     getBookJSON(1, JSONStreamWriter.FORMAT.jsonl));
    }

    @Tag("fast")
    @Test
    void testBasicWriterMultiJSONL() {
        assertEquals("""
                     {"id":"0","title":"book #0"}
                     {"id":"1","title":"book #1"}
                     """,
                     getBookJSON(2, JSONStreamWriter.FORMAT.jsonl));
    }

    @Tag("fast")
    @Test
    void testBasicWriterLargeJSONL() {
        String large = getBookJSON(200000, JSONStreamWriter.FORMAT.jsonl);
        assertFalse(large.isEmpty());
    }

    private String getBookJSON(int count, JSONStreamWriter.FORMAT format) {
        try (CharArrayWriter stringW = new CharArrayWriter()) {
            try (JSONStreamWriter jsonW = new JSONStreamWriter(stringW, format)) {
                getBooks(count).forEach(jsonW::write);
            }
            return stringW.toString();
        }
    }

    private String getCustomBookJSON(int count, JSONStreamWriter.FORMAT format) {
        try (CharArrayWriter stringW = new CharArrayWriter()) {
            try (JSONStreamWriter jsonW = new JSONStreamWriter(stringW, format)) {
                jsonW.setPreOutput("<\n");
                jsonW.setPostOutput("\n>\n");
                jsonW.setElementDivider("; ");
                jsonW.setAdjustPattern(Pattern.compile("o"));
                jsonW.setAdjustReplacement("0");
                getBooks(count).forEach(jsonW::write);
            }
            return stringW.toString();
        }
    }

    private Stream<BookDto> getBooks(int count) {
        return IntStream.range(0, count)
                        .boxed()
                        .map(id -> new BookDto().id(Integer.toString(id)).title("book #" + id));
    }
}