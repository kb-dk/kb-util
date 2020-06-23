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
        toFlatList(collection.entrySet().stream(), null)
                .forEach(tuple -> properties.put(tuple.getKey(), tuple.getValue()));
        return properties;
    }
    
    /**
     * Converts the YAML object into a list of entries, i.e. flattens the nested structure.
     *
     * To maintain the uniqueness of the keys, each key is prefixed with the path. This can best be shown with an example
     *
     * This yaml:
     * <pre>
     *     test:
     *         somestring: Hello World
     * </pre>
     * becomes
     * <pre>
     *     test.somestring: Hello World
     * </pre>
     *
     * In case of lists, we use the index. Example:
     *
     * <pre>
     *      arrayofstrings:
     *          - a
     *          - b
     *</pre>
     * becomes
     * <pre>
     *     arrayofstrings.0: a, arrayofstrings.1: b
     * </pre>
     *
     * The entries will be sorted by their key
     *
     * @param collection the yaml
     * @return the yaml as a flat list of entries
     */
    public static List<Entry<String, Object>> flatten(YAML collection) {
        return toFlatList(collection.entrySet().stream(), null)
                       .sorted(Entry.comparingByKey())
                       .collect(Collectors.toList());
    }
    
    /**
     * Converts the YAML object into list of leaf values
     *
     * @param collection the yaml
     * @return the yaml as properties
     */
    public static List<String> values(YAML collection) {
        return toFlatList(collection.entrySet().stream(), null)
                       .sorted(Entry.comparingByKey())
                       .map(entry -> entry.getValue().toString())
                       .collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    protected static Stream<Entry<String, Object>> toFlatList(final Stream<Entry<String, Object>> collection,
                                                              final String keyFix) {
        
        return collection.flatMap(entry -> {
            //Prefix the key with the preceding keys
            String key = keyFix == null ? entry.getKey() : keyFix + "." + entry.getKey();
            
            //handle the value
            Object value = entry.getValue();
            if (value instanceof Map) {
                //If this is a map, handle it recursively, and send the key as the new prefix.
                final Stream<Entry<String, Object>> mapEntries = ((Map<String, Object>) value).entrySet().stream();
                // and send the key as the new prefix
                return toFlatList(mapEntries, key);
            } else if (value instanceof List) {
                //if this is a list, handle it recursively, and use the list index as the key
                List<Object> valueList = (List<Object>) value; //make value a list
    
                //Create a stream of the entries in the list, with the list index as the key
                final Stream<Entry<String, Object>> listEntries = IntStream.range(0, valueList.size())
                                                                           .mapToObj(i -> new AbstractMap.SimpleEntry<>(
                                                                                   ""+i,
                                                                                   valueList.get(i)));
                
                //And as these elements can themselves be lists or maps, run them through toFlatList again
                // this time with
                return toFlatList(listEntries, key);
            } else {
                //Otherwise just return this tuple
                return Stream.of(new AbstractMap.SimpleEntry<>(key, value));
            }
        });
    }
    
}

