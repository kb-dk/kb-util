package dk.kb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YAMLUtils {
    
    /**
     * Converts the YAML object into a properties object
     *
     * @param collection the yaml
     * @return the yaml as properties
     */
    public static Properties toProperties(YAML collection) {
        Map<String, Object> propertiesMap = toFlatList(collection, null, false)
                                                    .collect(Collectors.toMap(
                                                            tuple2 -> tuple2._1,
                                                            tuple2 -> tuple2._2
                                                    ));
        
        Properties properties = new Properties();
        
        properties.putAll(propertiesMap);
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
        Map<String, Object> propertiesMap = toFlatList(collection, null, flattenList)
                                                    .collect(Collectors.toMap(
                                                            tuple2 -> tuple2._1,
                                                            tuple2 -> tuple2._2
                                                    ));
        
        Properties properties = new Properties();
        
        properties.putAll(propertiesMap);
        return properties;
    }
    
    @SuppressWarnings("unchecked")
    protected static Stream<Tuple2<String, Object>> toFlatList(final Map<String, Object> collection,
                                                               final String keyFix,
                                                               final boolean flattenList) {
    
        return collection.entrySet().stream().flatMap(
                entry -> {
                    //Prefix the key with the preceding keys
                    String key = Stream.of(keyFix,entry.getKey())
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.joining("."));
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        //If this is a map, go down, and send the key as the new prefix.
                        return toFlatList((Map<String, Object>) value, key, flattenList);
                    } if (flattenList && value instanceof List) {
                        List valueList = (List) value;
                        ArrayList<Tuple2<String, Object>> result = new ArrayList<>();
                        for (int i = 0; i < valueList.size(); i++) {
                            result.add(new Tuple2<>(key + "." + i, valueList.get(i)));
                        }
                        return result.stream();
                    } else {
                        //Otherwise just return this tuple
                        return Stream.of(new Tuple2<>(key, value));
                    }
                });
    }
    
    
    public static class Tuple2<K,V> {
        
        K _1;
        V _2;
        
        public Tuple2(K _1, V _2) {
            this._1 = _1;
            this._2 = _2;
        }
        
        @Override
        public String toString() {
            return _1 + "=" + _2 + "";
        }
    }
}

