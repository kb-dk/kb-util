package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Map;
import java.util.stream.Collectors;

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
class YAMLTest {
    @Test
    public void testLoad() throws IOException {
        YAML.resolveConfig("test.yml");
    }

    @Test
    public void testNested() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml");
        assertEquals("Hello World", yaml.getString("test.somestring"),
                     "Nested request for string should be supported");
    }

    @Test
    public void testRoot() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml", "test");
        assertEquals("Hello World", yaml.getString("somestring"),
                     "Direct request for string from resolved root should be supported");
    }
    
    @Test
    public void testArray() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml", "test");
        assertEquals("[a, b, c]", yaml.getList("arrayofstrings").toString(),
                     "Arrays of strings should be supported");
    }

    @Test
    public void testEntrySetLeafs() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml", null);
        String joined = yaml.entrySetLeafs().stream().map(Map.Entry::getKey).collect(Collectors.joining(", "));
        assertEquals("test.somestring, test.someint, " +
                     "test.nested.sublevel2string, " +
                     "test.arrayofstrings.0, test.arrayofstrings.1, test.arrayofstrings.2",
                     joined);
    }
}