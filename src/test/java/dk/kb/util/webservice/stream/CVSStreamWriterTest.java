package dk.kb.util.webservice.stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.CharArrayWriter;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class CVSStreamWriterTest {

    @Tag("fast")
    @Test
    void testBasicWriterSingleCSV() {
        assertEquals("\"id\",\"title\",\"pages\"\n" +
                     "\"0\",\"book #0\",100\n",
                     getBookCSV(1));
    }

    @Tag("fast")
    @Test
    void testBasicWriterMultiCSV() {
        assertEquals("\"id\",\"title\",\"pages\"\n" +
                     "\"0\",\"book #0\",100\n" +
                     "\"1\",\"book #1\",101\n",
                     getBookCSV(2));
    }


    private String getBookCSV(int count) {
        try (CharArrayWriter stringW = new CharArrayWriter();
             CSVStreamWriter csvW = new CSVStreamWriter(stringW)) {
            getBooks(count).forEach(csvW::write);
            csvW.close();
            return stringW.toString();
        }
    }

    private Stream<BookDto> getBooks(int count) {
        return IntStream.range(0, count)
                .boxed()
                .map(id -> new BookDto().id(Integer.toString(id)).title("book #" + id).pages(id + 100));
    }

}