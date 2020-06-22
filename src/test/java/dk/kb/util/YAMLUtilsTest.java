package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YAMLUtilsTest {
    
    @Test
    void toFlatList() throws IOException {
        YAML yaml = YAML.resolveConfig("test.yml");
    
        final Properties x = YAMLUtils.toProperties(yaml);
        assertEquals("Hello World", x.getProperty("test.somestring"));
        assertEquals("{test.arrayofstrings=[a, b, c], test.somestring=Hello World, test.someint=87, test.nested.sublevel2string=sub1}",x.toString());
    }
}