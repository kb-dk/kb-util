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
package dk.kb.util;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testIntArray() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml", "test");
        List<Integer> ints = yaml.getList("arrayofints");
        assertEquals("[1, 2]", ints.toString(),
                     "Arrays of integers should be supported");
    }
    
    @Test
    public void testTypes() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml", "test");
        final double EXPECTED = 87.13;
        double actual = yaml.getDouble("somedouble");

        assertTrue(actual >= EXPECTED*0.99 && actual <= EXPECTED*1.01,
                     "Double should be supported and as expected");
        assertEquals(true, yaml.getBoolean("somebool"),
                     "Boolean should be supported and as expected");
    }
}