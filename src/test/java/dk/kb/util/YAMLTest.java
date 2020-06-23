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

    @Test
    public void testMergeCollision() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("config_pair_part_1.yml", "config_pair_part_2.yml");
        assertEquals("number_2", yaml.getString("collision"),
                     "When merging, the latest definition should win");
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