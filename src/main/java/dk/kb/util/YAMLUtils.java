package dk.kb.util;

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
        Map<String, Object> propertiesMap = toFlatList(collection, null)
                                                    .collect(Collectors.toMap(
                                                            tuple2 -> tuple2._1,
                                                            tuple2 -> tuple2._2
                                                    ));
        
        Properties properties = new Properties();
        
        properties.putAll(propertiesMap);
        return properties;
    }
    
    protected static Stream<Tuple2<String, Object>> toFlatList(Map<String, Object> collection, String keyFix) {
    
        return collection.entrySet().stream().flatMap(
                entry -> {
                    String key = Stream.of(keyFix,entry.getKey()).filter(Objects::nonNull).collect(Collectors.joining("."));
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        return toFlatList((Map<String, Object>) value, key);
                    } else {
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

