package dk.kb.util.webservice.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
@SuppressWarnings("CallToPrintStackTrace")
class ExportWriterFactoryTest {


    /**
     * This unit test demonstrates the use of Mockito to avoid heavy simulation of the webservice environment.
     */
    // The unused suppression is for the ExportWriter where construction should fail
    @SuppressWarnings("unused")
    @Tag("fast")
    @DisplayName("Mockito based test for ExportWriterFactory with jsonl")
    @Test
    void testWrapJSONL() throws IOException {
        // Mockito makes it easy to create mocked versions of classes.
        // Basically it creates an empty shell from the class and lets the test-writer fill in the methods
        // used by the test. The class constructor is bypassed.

        // The servlet response is used for setting the correct MIME type
        HttpServletResponse response = mock(HttpServletResponse.class);

        // We only accept a specific value to be set
        doThrow(new IllegalArgumentException("Only 'application/x-ndjson' is allowed here"))
                .when(response).setContentType(AdditionalMatchers.not(eq("application/x-ndjson")));

        // Methods that are not explicitly mocked returns null
        assertNull(response.getHeader("87"));


        // The HTTP headers specifies allowed MIME types
        HttpHeaders headers = mock(HttpHeaders.class);
        List<MediaType> mimes = Arrays.asList(
                new MediaType("foo", "bar"),
                new MediaType("application", "x-ndjson"));
        when(headers.getAcceptableMediaTypes()).thenReturn(mimes);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ExportWriter writer = ExportWriterFactory.wrap(
                    out, response, headers, "json", null, false, null);
            fail("Accepted json even though the Mockito setup should only accept jsonl");
        } catch (IllegalArgumentException e) {
            // Expected as we asked for json and not jsonl
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
             ExportWriter writer = ExportWriterFactory.wrap(
                     out, response, headers, null, ExportWriterFactory.FORMAT.jsonl, false, null);
            getBooks(2).forEach(writer::write);
            writer.close();
            assertEquals("{\"id\":\"0\",\"title\":\"book #0\"}\n" +
                         "{\"id\":\"1\",\"title\":\"book #1\"}\n",
                         out.toString(StandardCharsets.UTF_8));
        }
    }

    @Tag("fast")
    @Test
    void testWrapCSV() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IllegalArgumentException("Only 'application/x-ndjson' is allowed here"))
                .when(response).setContentType(AdditionalMatchers.not(eq("text/csv")));

        HttpHeaders headers = mock(HttpHeaders.class);
        List<MediaType> mimes = Collections.singletonList(new MediaType("text", "csv"));
        when(headers.getAcceptableMediaTypes()).thenReturn(mimes);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
             ExportWriter writer = ExportWriterFactory.wrap(
                     out, response, headers, "csv", null, false, null);
            getBooks(2).forEach(writer::write);
            writer.close();
            assertEquals("\"id\",\"title\",\"pages\"\n" +
                         "\"0\",\"book #0\",\n" +
                         "\"1\",\"book #1\",\n",
                         out.toString(StandardCharsets.UTF_8));
        }
    }

    @Tag("fast")
    @Test
    void testXML0Format() {
        assertResponse("<books>\n</books>\n", "xml", null, 0);
    }

    @Tag("fast")
    @Test
    void testXML1Format() {
        assertResponse("<books><book id=\"0\">\n" +
                       "  <title>book #0</title>\n" +
                       "</book>\n" +
                       "\n" +
                       "</books>\n",
                       "xml", null, 1);
    }

    @Tag("fast")
    @Test
    void testXML2Format() {
        assertResponse("<books><book id=\"0\">\n" +
                       "  <title>book #0</title>\n" +
                       "</book>\n" +
                       "\n" +
                       "</books>\n",
                       "xml", null, 1);
    }

    @Tag("fast")
    @Test
    void testXML1MIME() {
        assertResponse("<books><book id=\"0\">\n" +
                       "  <title>book #0</title>\n" +
                       "</book>\n" +
                       "\n" +
                       "</books>\n",
                       null, "application/xml", 1);
    }

    @Tag("fast")
    @Test
    void testJSON1MIME() {
        assertResponse("[\n" +
                       "{\"id\":\"0\",\"title\":\"book #0\"}\n" +
                       "]\n",
                       null, "application/json", 1);
    }

    @Tag("fast")
    @Test
    void testJSONL1MIME() {
        assertResponse("{\"id\":\"0\",\"title\":\"book #0\"}\n",
                       null, "application/x-ndjson", 1);
    }

    @Tag("fast")
    @Test
    void testCSV1MIME() {
        assertResponse("\"id\",\"title\",\"pages\"\n" +
                       "\"0\",\"book #0\",\n",
                       null, "text/csv", 1);
    }

    void assertResponse(String expected, String format, String mime, int books) {
        HttpServletResponse response = mock(HttpServletResponse.class);

        HttpHeaders headers = mock(HttpHeaders.class);
        if (mime != null) {
            String[] mimeParts = mime.split("/");
            List<MediaType> mimes = Collections.singletonList(new MediaType(mimeParts[0], mimeParts[1]));
            when(headers.getAcceptableMediaTypes()).thenReturn(mimes);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExportWriter writer = ExportWriterFactory.wrap(
                    out, response, headers, format, ExportWriterFactory.FORMAT.jsonl, false, "books");
            getBooks(books).forEach(writer::write);
            writer.close(); // Muct be called to write closing element
            assertEquals(expected, out.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stream<BookDto> getBooks(int count) {
        return IntStream.range(0, count)
                .boxed()
                .map(id -> new BookDto().id(Integer.toString(id)).title("book #" + id));
    }

}