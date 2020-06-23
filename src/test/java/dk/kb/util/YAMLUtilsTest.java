package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YAMLUtilsTest {
    
    @Test
    void yamlToPropertiesTest() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml");
    
        final Properties x = YAMLUtils.toProperties(yaml);
        assertEquals("Hello World", x.getProperty("test.somestring"));
        assertEquals("{test.somestring=Hello World, test.test.arrayofstrings.2=c, test.someint=87, test.test.arrayofstrings.0=a, test.test.arrayofstrings.1=b, test.nested.sublevel2string=sub1}",
                     x.toString());
    }

    @Test
    public void testNestedMaps() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_maps.yml", "test");

        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("barA1, barA2, barB", flattenedValues);
    }
    
    @Test
    public void testNestedMapsProperites() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_maps.yml", "test");
    
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{listofmaps.0.fooA2=barA2, listofmaps.0.fooA1=barA1, listofmaps.1.fooB=barB}",
                     flattenedValues);
    }
    
    
    @Test
    public void testNestedLists() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_lists.yml", "test");
    
        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("llItemA1, llItemA2, llItemB",
                     flattenedValues);
    }
    
    @Test
    public void testNestedLists2() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_lists.yml", "test");
    
        List<String> flattened = YAMLUtils.flatten(yaml)
                                          .stream()
                                          .map(Object::toString)
                                          .collect(Collectors.toList());
        assertEquals("listoflists.0.0=llItemA1, listoflists.0.1=llItemA2, listoflists.1.0=llItemB",
                     String.join(", ", flattened));
    }
    
    @Test
    public void testNestedListsProperites() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_lists.yml", "test");
        
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{listoflists.0.1=llItemA2, listoflists.1.0=llItemB, listoflists.0.0=llItemA1}",
                     flattenedValues);
    }
    
    
    @Test
    public void testNestedMix() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_mix.yml", "test");
    
        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("barM, 87", flattenedValues);
    }
    
    @Test
    public void testNestedMixProperites() throws IOException {
        YAML yaml = YAML.resolveConfig("nested_mix.yml", "test");
        
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{mixedlist.0.fooM=barM, mixedlist.1=87}",
                     flattenedValues);
    }

}