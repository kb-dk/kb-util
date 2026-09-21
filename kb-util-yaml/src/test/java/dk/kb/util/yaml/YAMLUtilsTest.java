package dk.kb.util.yaml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YAMLUtilsTest {
    
    @Test
    void yamlToPropertiesTest() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml");
        final Properties asProperties = YAMLUtils.toProperties(yaml);

        Map<String, Object> expectedAsMap = Map.ofEntries(
                Map.entry("test.arrayofstrings.2", "c"),
                Map.entry("test.arrayofstrings.0", "a"),
                Map.entry("test.arrayofstrings.1", "b"),
                Map.entry("test.somestring", "Hello World"),
                Map.entry("test.somedouble", 87.13),
                Map.entry("test.somebool", true),
                Map.entry("test.someint", 87),
                Map.entry("test.arrayofints.0", 1),
                Map.entry("test.arrayofints.1", 2),
                Map.entry("test.nested.sublevel2string", "sub1"));

        assertEquals(expectedAsMap, asProperties);
    }

    @Test
    public void testNestedMaps() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml").getSubMap("test");
        assertEquals(List.of("barA1", "barA2", "barB"), YAMLUtils.values(yaml));
    }
    
    @Test
    public void testNestedMapsProperties() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml").getSubMap("test");

        Properties asProperties = YAMLUtils.toProperties(yaml);
        assertEquals(Map.of("listofmaps.0.fooA2", "barA2",
                        "listofmaps.0.fooA1", "barA1",
                        "listofmaps.1.fooB", "barB"),
                asProperties);
    }
    
    
    @Test
    public void testNestedLists() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");

        List<String> values = YAMLUtils.values(yaml);
        assertEquals(List.of("llItemA1", "llItemA2", "llItemB"), values);
    }
    
    @Test
    public void testNestedLists2() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");

        List<Map.Entry<String, Object>> flattened = YAMLUtils.flatten(yaml);
        List<Map.Entry<String, Object>> expected = List.of(
                Map.entry("listoflists.0.0", "llItemA1"),
                Map.entry("listoflists.0.1", "llItemA2"),
                Map.entry("listoflists.1.0", "llItemB"));
        assertEquals(expected, flattened);
    }
    
    @Test
    public void testNestedListsProperties() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");

        Properties asProperties = YAMLUtils.toProperties(yaml);
        assertEquals(Map.of("listoflists.0.1", "llItemA2",
                        "listoflists.1.0", "llItemB",
                        "listoflists.0.0", "llItemA1"),
                asProperties);
    }
    
    
    @Test
    public void testNestedMix() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_mix.yml").getSubMap("test");

        List<String> values = YAMLUtils.values(yaml);
        assertEquals(List.of("barM", "87"), values);
    }
    
    @Test
    public void testNestedMixProperties() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_mix.yml").getSubMap("test");

        Properties asProperties = YAMLUtils.toProperties(yaml);
        assertEquals(Map.of("mixedlist.0.fooM", "barM", "mixedlist.1", 87),
                     asProperties);
    }

}
