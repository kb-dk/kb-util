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

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.*;
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
     * @return the map at the path or null if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public YAML getSubMap(String path) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        if (!(o instanceof Map)) {
            log.trace("Expected a Map for path '{}' but got {}", path, o);
            return null;
        }
        try {
            return new YAML((Map<String, Object>) o);
        } catch (Exception e) {
            log.trace("Expected a Map for path '{}' but got {}", path, o.getClass().getName());
            return null;
        }
    }

    /**
     * Resolves the list at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the list.
     * @return the list at the path or null if it could not be located.
     */
    public <T> List<T> getList(String path) {
        return getList(path, null);
    }

    /**
     * Resolves the list at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the list.
     * @param defaultList if the path cannot be resolved, return this value.
     * @return the list at the path or null if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path, List<T> defaultList) {
        Object o = get(path);
        if (o == null) {
            return defaultList;
        }
        if (!(o instanceof List)) {
            log.trace("Expected a List for path '{}' but got {}", path, o.getClass().getName());
            return null;
        }
        try {
            return (List<T>) o;
        } catch (Exception e) {
            log.trace("Exception casting to typed List", e);
            return null;
        }
    }

    /**
     * Resolves the list of sub YAMLs at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the list.
     * @return the list of sub YAMLs at the path or null if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public List<YAML> getYAMLList(String path) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        if (!(o instanceof List)) {
            log.trace("Expected a List for path '{}' but got {}", path, o.getClass().getName());
            return null;
        }
        List<Map<String, Object>> hmList;
        try {
            hmList = (List<Map<String, Object>>)o;
        } catch (Exception e) {
            log.trace("Exception casting to List<Map<String, Object>>", e);
            return null;
        }
        return hmList.stream().map(YAML::new).collect(Collectors.toList());
    }

    /**
     * Resolves the Short at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Short.
     * @return the Short at the path or null if it could not be located.
     */
    public Short getShort(String path) {
        return getShort(path, null);
    }

    /**
     * Resolves the Short at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Short.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Short at the path or defaultValue if it could not be located.
     */
    public Short getShort(String path, Short defaultValue) {
        Object o = get(path);
        try {
            return o == null ? defaultValue : Short.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Short", o);
            return null;
        }
    }

    /**
     * Resolves the integer at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the integer.
     * @return the integer at the path or null if it could not be located.
     */
    public Integer getInteger(String path) {
        return getInteger(path, null);
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
        Object o = get(path);
        try {
            return o == null ? defaultValue : Integer.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Integer", o);
            return null;
        }
    }

    /**
     * Resolves the Long at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Long.
     * @return the Long at the path or null if it could not be located.
     */
    public Long getLong(String path) {
        return getLong(path, null);
    }

    /**
     * Resolves the Long at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Long.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Long at the path or defaultValue if it could not be located.
     */
    public Long getLong(String path, Long defaultValue) {
        Object o = get(path);
        try {
            return o == null ? defaultValue : Long.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Long", o);
            return null;
        }
    }

    /**
     * Resolves the double at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the double.
     * @return the Double at the path or null if it could not be located.
     */
    public Double getDouble(String path) {
        return getDouble(path, null);
    }

    /**
     * Resolves the double at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the double.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Double at the path or defaultValue if it could not be located.
     */
    public Double getDouble(String path, Double defaultValue) {
        Object o = get(path);
        try {
            return o == null ? defaultValue : Double.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Double", o);
            return null;
        }
    }

    /**
     * Resolves the float at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Float.
     * @return the Float at the path or null if it could not be located.
     */
    public Float getFloat(String path) {
        return getFloat(path, null);
    }

    /**
     * Resolves the float at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the Float.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Float at the path or defaultValue if it could not be located.
     */
    public Float getFloat(String path, Float defaultValue) {
        Object o = get(path);
        try {
            return o == null ? defaultValue : Float.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.trace("Unable to parse '{}' to Float", o);
            return null;
        }
    }

    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the boolean.
     * @return the Boolean at the path or null if it could not be located.
     */
    public Boolean getBoolean(String path) {
        return getBoolean(path, null);
    }

    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the boolean.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Boolean at the path or defaultValue if it could not be located.
     */
    public Boolean getBoolean(String path, Boolean defaultValue) {
        Object o = get(path);
        return o == null ? defaultValue : Boolean.valueOf(o.toString());
    }

    /**
     * Resolves the string at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * Note: Object.toString is used to provide the String value, so this is safe to call for most YAML content.
     * @param path path for the string.
     * @return the String at the path or null if it could not be located.
     */
    public String getString(String path) {
        return getString(path, null);
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
        Object o = get(path);
        return o == null ? defaultValue : o.toString();
    }

    /* **************************** Path-supporting overrides ************************************ */

    /**
     * Used internally by {@link #get} to avoid endless recursion.
     * @param key the key to look up.
     * @return the value for the key or null if the key is not present in the map.
     */
    private Object getSuper(Object key) {
        return super.get(key);
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
        return get(path) != null;
    }

    /**
     * Resolves the Object at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param pathO path for the Object.
     * @return the Object or null if it could not be returned.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object get(Object pathO) {
        String path = pathO.toString();
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        String[] pathElements = path.split("[.]");
        YAML current = this;
        for (int i = 0 ; i < pathElements.length ; i++) {
            Object sub = current.getSuper(pathElements[i]);
            if (sub == null) {
                log.trace("Unable to request sub element '{}' in path '{}'", pathElements[i], path);
                return null;
            }
            if (i == pathElements.length-1) { //If this is the final pathElement, just return it
                return sub;
            } //Otherwise, we require that it is a map so we can continue to query
            if (!(sub instanceof Map)) {
                log.trace("The sub element '{}' in path '{}' was not a Map", pathElements[i], path);
                return null;
            }
            try { //Update current as the sub we have found
                current = new YAML((Map<String, Object>) sub);
            } catch (Exception e) {
                log.trace("Expected a Map<String, Object> for path '{}' but got casting failed", path);
                return null;
            }
        }
        return current;
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
     * Parse the given configStream as YAML.
     * @param yamlStream YAML.
     * @return a YAML based on the given stream.
     */
    public static YAML parse(InputStream yamlStream) {
        Object raw = new Yaml().load(yamlStream);
        if (!(raw instanceof Map)) {
            throw new IllegalArgumentException("The config resource does not evaluate to a valid YAML configuration.");
        }
        YAML rootMap = new YAML((Map<String, Object>) raw);
        log.trace("Parsed YAML config stream");
        return rootMap;
    }

    /**
     * Resolve the given YAML configurations and present a merged YAML from that.
     * Note: This method merges the YAML configs as-is: Any key-collisions are handled implicitly by keeping the latest
     * key-value pair in the stated configurations.
     * @param configNames the names of the configuration files.
     * @return the configurations merged and parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if a configuration could not be fetched.
     */
    public static YAML resolveMultiConfig(String... configNames) throws IOException {
        List<InputStream> configs = null;
        try {
            configs = Arrays.stream(configNames).map(Resolver::resolveStream).collect(Collectors.toList());
            InputStream yamlStream = null;
            for (InputStream config : configs) {
                yamlStream = yamlStream == null ? config : new SequenceInputStream(yamlStream, config);
            }
            log.debug("Fetched merged YAML config '{}'", Arrays.toString(configNames));
            return parse(yamlStream);
        } finally {
            if (configs != null) {
                for (InputStream config: configs) {
                    config.close();
                }
            }
        }
    }

    /**
     * Resolve the given YAML configuration.
     * @param configName the name of the configuration file.
     * @param confRoot the root element in the configuration or null if the full configuration is to be returned.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if the configuration could not be fetched.
     */
    public static YAML resolveConfig(String configName, String confRoot) throws IOException {
        YAML rootMap = resolveMultiConfig(configName);

        if (confRoot == null) {
            return rootMap;
        }

        if (!rootMap.containsKey(confRoot)) {
            throw new IllegalStateException("YAML configuration must contain the '" + confRoot + "' element");
        }
        return rootMap.getSubMap(confRoot);
    }

}
