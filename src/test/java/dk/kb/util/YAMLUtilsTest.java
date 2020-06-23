package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YAMLUtilsTest {
    
    @Test
    void toFlatList() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml");
    
        final Properties x = YAMLUtils.toProperties(yaml);
        assertEquals("Hello World", x.getProperty("test.somestring"));
        assertEquals("{test.arrayofstrings=[a, b, c], test.somestring=Hello World, test.someint=87, test.nested.sublevel2string=sub1}",x.toString());
    }

    @Test
    public void testNestedMaps() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_maps.yml", "test");

        String flattenedValues = YAMLUtils.toFlatList(yaml, null, true).
                map(Map.Entry::getValue).map(Object::toString).
                collect(Collectors.joining(", "));
        assertEquals("barA1, barA2, barB", flattenedValues);
    }

    @Test
    public void testNestedLists() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_lists.yml", "test");

        String flattenedValues = YAMLUtils.toFlatList(yaml, null, true).
                map(Map.Entry::getValue).map(Object::toString).
                collect(Collectors.joining(", "));
        assertEquals("llItemA1, llItemA2, llItemB", flattenedValues);
    }

    @Test
    public void testNestedMix() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_mix.yml", "test");

        String flattenedValues = YAMLUtils.toFlatList(yaml, null, true).
                map(Map.Entry::getValue).map(Object::toString).
                collect(Collectors.joining(", "));
        assertEquals("barM, 87", flattenedValues);
    }

}