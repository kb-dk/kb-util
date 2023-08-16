package dk.kb.util.yaml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YAMLUtilsTest {
    
    @Test
    void yamlToPropertiesTest() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml");
    
        final Properties x = YAMLUtils.toProperties(yaml);
        assertEquals("Hello World", x.getProperty("test.somestring"));
        assertEquals("{test.arrayofstrings.2=c, test.arrayofstrings.0=a, test.arrayofstrings.1=b, test.somestring=Hello World, test.somedouble=87.13, test.somebool=true, test.someint=87, test.arrayofints.0=1, test.arrayofints.1=2, test.nested.sublevel2string=sub1}",
                     x.toString());
    }

    @Test
    public void testNestedMaps() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml").getSubMap("test");

        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("barA1, barA2, barB", flattenedValues);
    }
    
    @Test
    public void testNestedMapsProperites() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml").getSubMap("test");
      
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{listofmaps.0.fooA2=barA2, listofmaps.0.fooA1=barA1, listofmaps.1.fooB=barB}",
                     flattenedValues);
    }
    
    
    @Test
    public void testNestedLists() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");
    
        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("llItemA1, llItemA2, llItemB",
                     flattenedValues);
    }
    
    @Test
    public void testNestedLists2() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");
    
        List<String> flattened = YAMLUtils.flatten(yaml)
                                          .stream()
                                          .map(Object::toString)
                                          .collect(Collectors.toList());
        assertEquals("listoflists.0.0=llItemA1, listoflists.0.1=llItemA2, listoflists.1.0=llItemB",
                     String.join(", ", flattened));
    }
    
    @Test
    public void testNestedListsProperites() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml").getSubMap("test");
        
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{listoflists.0.1=llItemA2, listoflists.1.0=llItemB, listoflists.0.0=llItemA1}",
                     flattenedValues);
    }
    
    
    @Test
    public void testNestedMix() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_mix.yml").getSubMap("test");
    
        String flattenedValues = String.join(", ", YAMLUtils.values(yaml));
        assertEquals("barM, 87", flattenedValues);
    }
    
    @Test
    public void testNestedMixProperites() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_mix.yml").getSubMap("test");
        
        String flattenedValues = YAMLUtils.toProperties(yaml).toString();
        assertEquals("{mixedlist.0.fooM=barM, mixedlist.1=87}",
                     flattenedValues);
    }

    @Test
    public void testPathSubstitute() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/path_substitution.yaml");
        yaml.setExtrapolate(true);

        assertEquals("foo", yaml.get(".lower.bar"), "Basic path substitution should work");
        assertEquals("boom", yaml.get(".fallback.bar"), "Path substitution with default value should work");
        assertEquals("foo", yaml.get(".mixing.bar"), "Path substitution with default value should work");
    }

}
