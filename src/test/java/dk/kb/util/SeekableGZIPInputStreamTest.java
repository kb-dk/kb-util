package dk.kb.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

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
class SeekableGZIPInputStreamTest {

    @Test
    void testBasicGZIP() throws IOException {
        InputStream gis = new GZIPInputStream(Resolver.resolveURL("multi.gz").openStream());
        assertEquals("Foo", IOUtils.toString(gis, StandardCharsets.UTF_8),
                "Reading until EOF should return only the first gzip block");
    }

    @Test
    void testBasicRead() throws IOException {
        try (SeekableGZIPInputStream szip = new SeekableGZIPInputStream(
                Resolver.getPathFromClasspath("multi.gz").toFile())) {
            assertEquals("Foo", read(szip, 3), "First block should be directly readable");
        }
    }

    @Test
    void testMultiRead() throws IOException {
        try (SeekableGZIPInputStream szip = new SeekableGZIPInputStream(
                Resolver.getPathFromClasspath("multi.gz").toFile())) {
            assertEquals("Foo", read(szip, 3), "First block should be directly readable");
            szip.seek(24);
            assertEquals("Hello World!", read(szip, 12), "Second block should be readable after seek");
            szip.seek(57);
            assertEquals("bar", read(szip, 3), "Third block should be readable after seek");
        }
    }

    @Test
    void testFullFirstRead() throws IOException {
        try (SeekableGZIPInputStream szip = new SeekableGZIPInputStream(
                Resolver.getPathFromClasspath("multi.gz").toFile())) {
            assertEquals("Foo", IOUtils.toString(szip, StandardCharsets.UTF_8),
                    "Reading until EOF should return current gzip block");
        }
    }

    @Test
    void testFullSecondRead() throws IOException {
        try (SeekableGZIPInputStream szip = new SeekableGZIPInputStream(
                Resolver.getPathFromClasspath("multi.gz").toFile())) {
            szip.seek(24);
            assertEquals("Hello World!", IOUtils.toString(szip, StandardCharsets.UTF_8),
                    "Reading until EOF should return current gzip block");
        }
    }

    /**
     * Read {@code length} bytes from the current position in {@code szip}.
     * @param stream the stream to read from.
     * @param length the number of bytes to read.
     * @return the read bytes parsed as UTF-8.
     */
    private String read(InputStream stream, int length) throws IOException {
        byte[] buffer = new byte[length];
        assertEquals(length, stream.read(buffer), "The expecte number of bytes should be read");
        return new String(buffer, StandardCharsets.UTF_8);
    }
}