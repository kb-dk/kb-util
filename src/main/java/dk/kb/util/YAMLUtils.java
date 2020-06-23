package dk.kb.util;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        toFlatList(collection.entrySet().stream(), null, true)
                .forEach(tuple -> properties.put(tuple.getKey(), tuple.getValue()));
        return properties;
    }
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection  the yaml
     * @param flattenList should lists be flattened like test.arrayofstrings.0=a or kept like test.arrayofstrings=[a, b, c]
     * @return the yaml as properties
     */
    public static Properties toProperties(YAML collection, boolean flattenList) {
        Properties properties = new Properties();
        toFlatList(collection.entrySet().stream(), null, flattenList)
                .forEach(tuple -> properties.put(tuple.getKey(), tuple.getValue()));
        return properties;
    }
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection the yaml
     * @return the yaml as properties
     */
    public static List<Entry<String, Object>> flatten(YAML collection) {
        return toFlatList(collection.entrySet().stream(), null, true).collect(Collectors.toList());
    }
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection the yaml
     * @return the yaml as properties
     */
    public static List<String> values(YAML collection) {
        return toFlatList(collection.entrySet().stream(), null, true).map(entry -> entry.getValue().toString())
                                                                     .collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    protected static Stream<Entry<String, Object>> toFlatList(final Stream<Entry<String, Object>> collection,
                                                              final String keyFix,
                                                              final boolean flattenList) {
        
        return collection
                       .flatMap(entry -> {
                           //Prefix the key with the preceding keys
                           String key = keyFix == null ? entry.getKey() : keyFix + "." + entry.getKey();
                    
                           //handle the value
                           Object value = entry.getValue();
                           if (value instanceof Map) {
                               //If this is a map, go down, and send the key as the new prefix.
                               return toFlatList(((Map<String, Object>) value).entrySet().stream(), key, flattenList);
                           } else if (flattenList && value instanceof List) {
                               List<Object> valueList = (List<Object>) value;
                        
                               final Stream<Entry<String, Object>> uStream =
                                       IntStream.range(0, valueList.size())
                                                .mapToObj(i -> new AbstractMap.SimpleEntry<>(
                                                        key + "." + i,
                                                        valueList.get(i)));
                               return toFlatList(uStream, keyFix, flattenList);
                           } else {
                               //Otherwise just return this tuple
                               return Stream.of(new AbstractMap.SimpleEntry<>(key, value));
                           }
                       })
                       .sorted(Entry.comparingByKey());
    }
    
}

