/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Wrapper for SnakeYAML output for easier access to the YAML elements.
 */
public class YAML extends LinkedHashMap<String, Object> {
    private static final Logger log = LoggerFactory.getLogger(YAML.class);

    public YAML(String resourceName) throws IOException {
        this.putAll(YAML.resolveConfig(resourceName));
    }
    
    
    
    /**
     * Creates a YAML wrapper around the given map.
     * Changes to the map will be reflected in the YAML instance and vice versa.
     * @param map a map presumable delivered by SnakeYAML.
     */
    public YAML(Map<String, Object> map) {
        this.putAll(map);
    }

    /**
     * Resolves the YAML sub map at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the sub map.
     * @return the map at the path or Empty if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public Optional<YAML> getSubMap(String path) {
        return getSubMap(path, false);
    }
    
    /**
     * Resolves the YAML sub map at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the sub map.
     * @param maintainKeys preserve the path prefix for the keys in the result
     * @return the map at the path or Empty if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public Optional<YAML> getSubMap(String path, boolean maintainKeys) {
        Optional<Object> o = get(path);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        Object found = o.get();
        if (!(found instanceof Map)) {
            log.trace("Expected a Map for path '{}' but got {}", path, o);
            return Optional.empty();
        }
        try {
            Map<String,Object> result;
            if (maintainKeys){
                result = ((Map<String, Object>) found).entrySet().stream().collect(Collectors.toMap(
                        entry -> path+"."+entry.getKey(),
                        Map.Entry::getValue
                ));
            } else {
                result = (Map<String, Object>) found;
            }
            return Optional.of(new YAML(result));
        } catch (Exception e) {
            log.trace("Expected a Map for path '{}' but got {}", path, o.getClass().getName());
            return Optional.empty();
        }
    }
    
    /**
     * Resolves the list at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the list.
     * @return the list at the path or Empty if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getList(String path) {
        Optional<Object> o = get(path);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        Object found = o.get();
        if (!(found instanceof List)) {
            log.trace("Expected a List for path '{}' but got {}", path, o.getClass().getName());
            return Optional.empty();
        }
        try {
            return Optional.of((List<T>) found);
        } catch (Exception e) {
            log.trace("Exception casting to typed List", e);
            return Optional.empty();
        }
    }

    /**
     * Resolves the list of sub YAMLs at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the list.
     * @return the list of sub YAMLs at the path or Empty if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public Optional<List<YAML>> getYAMLList(String path) {
        Optional<Object> o = get(path);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        Object found = o.get();
        if (!(found instanceof List)) {
            log.trace("Expected a List for path '{}' but got {}", path, o.getClass().getName());
            return Optional.empty();
        }
        List<Map<String, Object>> hmList;
        try {
            hmList = (List<Map<String, Object>>)found;
        } catch (Exception e) {
            log.trace("Exception casting to List<Map<String, Object>>", e);
            return Optional.empty();
        }
        return Optional.of(hmList.stream().map(YAML::new).collect(Collectors.toList()));
    }

    /**
     * Resolves the integer at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the integer.
     * @return the integer at the path or Empty if it could not be located.
     */
    public Optional<Integer> getInteger(String path) {
        return Optional.ofNullable(getInteger(path, null));
    }

    /**
     * Resolves the Integer at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the integer.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the integer at the path or defaultValue if it could not be located.
     */
    public Integer getInteger(String path, Integer defaultValue) {
        Optional<Object> o = get(path);
        try {
            return o.map(value -> Integer.valueOf(value.toString())).orElse(defaultValue);
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Integer", o);
            return defaultValue;
        }
    }

    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the boolean.
     * @return the boolean at the path or Empty if it could not be located.
     */
    public Optional<Boolean> getBoolean(String path) {
        return Optional.ofNullable(getBoolean(path, null));
    }

    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the boolean.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the boolean at the path or defaultValue if it could not be located.
     */
    public Boolean getBoolean(String path, Boolean defaultValue) {
        Optional<Object> o = get(path);
        return o.map(value -> Boolean.valueOf(value.toString())).orElse(defaultValue);
   }

    /**
     * Resolves the String at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * Note: Object.toString is used to provide the String value, so this is safe to call for most YAML content.
     * @param path path for the string.
     * @return the String at the path or Empty if it could not be located.
     */
    public Optional<String> getString(String path) {
        return Optional.ofNullable(getString(path, null));
    }

    /**
     * Resolves the string at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Object.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the String at the path or defaultValue if it could not be located.
     */
    public String getString(String path, String defaultValue) {
        Optional<Object> o = get(path);
        return o.map(value -> value.toString()).orElse(defaultValue);
    }

    /* **************************** Path-supporting overrides ************************************ */

    /**
     * Used internally by {@link #get} to avoid endless recursion.
     * @param key the key to look up.
     * @return the value for the key or Empty if the key is not present in the map.
     */
    private Optional<Object> getSuper(Object key) {
        return Optional.ofNullable(super.get(key));
    }

    /**
     * Checks if a value is present at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Object.
     * @return true is an Object exists for the given path.
     */
    @Override
    public boolean containsKey(Object path) {
        return get(path).isPresent();
    }

    /**
     * Resolves the Object at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param pathO path for the Object.
     * @return the Object or Empty if it could not be returned.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<Object> get(Object pathO) {
        String path = pathO.toString();
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        String[] pathElements = path.split("[.]");
        YAML current = this;
        for (int i = 0 ; i < pathElements.length ; i++) {
            Optional<Object> sub = current.getSuper(pathElements[i]);
            if (sub.isEmpty()) {
                log.trace("Unable to request sub element '{}' in path '{}'", pathElements[i], path);
                return Optional.empty();
            }
            Object found = sub.get();
            if (i == pathElements.length-1) { //If this is the final pathElement, just return it
                return sub;
            } //Otherwise, we require that it is a map so we can continue to query
            if (!(found instanceof Map)) {
                log.trace("The sub element '{}' in path '{}' was not a Map", pathElements[i], path);
                return Optional.empty();
            }
            try { //Update current as the sub we have found
                current = new YAML((Map<String, Object>) found);
            } catch (Exception e) {
                log.trace("Expected a Map<String, Object> for path '{}' but got casting failed", path);
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    /* **************************** Fetching YAML ************************************ */

    /**
     * Resolve the given YAML configuration.
     * @param configName the name of the configuration file.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if the configuration could not be fetched.
     */
    public static YAML resolveConfig(String configName) throws IOException {
        return resolveConfig(configName, null);
    }

    /**
     * Resolve the given YAML configuration.
     * @param configName the name of the configuration file.
     * @param confRoot the root element in the configuration or null if the full configuration is to be returned.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if the configuration could not be fetched.
     */
    public static YAML resolveConfig(String configName, String confRoot) throws IOException {
        URL configURL = Resolver.resolveConfigFile(configName);

        Object raw;
        try (InputStream configStream = configURL.openStream()) {
            raw = new Yaml().load(configStream);
            if(!(raw instanceof Map)) {
                throw new IllegalArgumentException("The config resource '" + configURL
                        + "' does not contain a valid YAML configuration.");
            }
        } catch (IOException e) {
            throw new IOException(
                "Exception trying to load the YAML configuration from '" + configURL + "'", e);
        }

        YAML rootMap = new YAML((Map<String, Object>) raw);
        log.debug("Fetched YAML config '{}'", configName);

        if (confRoot == null) {
            return rootMap;
        }

        if (!rootMap.containsKey(confRoot)) {
            throw new IllegalStateException("YAML configuration must contain the '" + confRoot + "' element");
        } else {
            return rootMap.getSubMap(confRoot).get();
        }
    }

}
