package dk.kb.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class YAMLUtils {
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection the yaml
     * @return the yaml as properties
     */
    public static Properties toProperties(YAML collection) {
        Properties properties = new Properties();
        toFlatList(collection, null, false)
                .forEach(tuple -> properties.put(tuple.getKey(), tuple.getValue()));
        return properties;
    }
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection the yaml
     * @param flattenList should lists be flattened like test.arrayofstrings.0=a or kept like test.arrayofstrings=[a, b, c]
     * @return the yaml as properties
     *
     */
    public static Properties toProperties(YAML collection, boolean flattenList) {
        Properties properties = new Properties();
        toFlatList(collection, null, flattenList)
                .forEach(tuple -> properties.put(tuple.getKey(), tuple.getValue()));
        return properties;
    }
    
    @SuppressWarnings("unchecked")
    protected static Stream<Map.Entry<String, Object>> toFlatList(final Map<String, Object> collection,
                                                                  final String keyFix,
                                                                  final boolean flattenList) {
        
        return collection.entrySet().stream().flatMap(
                entry -> {
                    //Prefix the key with the preceding keys
                    String key = keyFix == null ? entry.getKey() : keyFix + "." + entry.getKey();
                    
                    //handle the value
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        //If this is a map, go down, and send the key as the new prefix.
                        return toFlatList((Map<String, Object>) value, key, flattenList);
                    } if (flattenList && value instanceof List) {
                        List<Object> valueList = (List<Object>) value;
                        ArrayList<Map.Entry<String,Object>> result = new ArrayList<>();
                        for (int i = 0; i < valueList.size(); i++) {
                            result.add(new AbstractMap.SimpleEntry<>(key + "." + i, valueList.get(i)));
                        }
                        return result.stream();
                    } else {
                        //Otherwise just return this tuple
                        return Stream.of(new AbstractMap.SimpleEntry<>(key, value));
                    }
                });
    }
}

