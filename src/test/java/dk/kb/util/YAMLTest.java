package dk.kb.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

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
    public void testAlias() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("alias.yml");
        assertEquals("FooServer", yaml.getString("serviceSetup.theServer"),
                     "The alias YAML should have the expected value for alias-using 'theServer'");
    }

    @Test
    public void testMultiConfig() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("config_pair_part_1.yml", "config_pair_part_2.yml");
        assertEquals("bar", yaml.getString("serviceSetup.someString"),
                     "The merged YAML should have the expected value for plain key 'somestring'");
        assertEquals("FooServer", yaml.getString("serviceSetup.theServer"),
                     "The merged YAML should have the expected value for alias-using 'theServer'");
    }
}