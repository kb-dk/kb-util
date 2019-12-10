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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path) {
        Object o = get(path);
        if (o == null) {
            return null;
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
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * @param path path for the boolean.
     * @return the boolean at the path or null if it could not be located.
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
     * @return the boolean at the path or defaultValue if it could not be located.
     */
    public Boolean getBoolean(String path, Boolean defaultValue) {
        Object o = get(path);
        return o == null ? defaultValue : Boolean.valueOf(o.toString());
    }

    /**
     * Resolves the String at the given path in the YAML. Supports {@code .} for path separation,
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
        String[] elements = path.split("[.]");
        YAML current = this;
        for (int i = 0 ; i < elements.length ; i++) {
            Object sub = current.getSuper(elements[i]);
            if (sub == null) {
                log.trace("Unable to request sub element '{}' in path '{}'", elements[i], path);
                return null;
            }
            if (i == elements.length-1) {
                return sub;
            }
            if (!(sub instanceof Map)) {
                log.trace("The sub element '{}' in path '{}' was not a Map", elements[i], path);
                return null;
            }
            try {
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
     * Resolve the given YAML configuration.
     * @param configName the name of the configuration file.
     * @param confRoot the root element in the configuration or null if the full configuration is to be returned.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if the configuration could not be fetched.
     */
    public static YAML resolveConfig(String configName, String confRoot) throws IOException {
        URL configURL = resolveConfigFile(configName);

        Object raw;
        try (InputStream configStream = configURL.openStream()) {
            raw = new Yaml().load(configStream);
            if(!(raw instanceof Map)) {
                throw new IllegalArgumentException("The config resource '" + configURL
                        + "' does not contain a valid DS Cumulus Export configuration.");
            }
        } catch (IOException e) {
            throw new IOException(
                "Exception trying to load the DS Cumulus Export configuration from '" + configURL + "'");
        }

        YAML rootMap = new YAML((Map<String, Object>) raw);
        log.debug("Fetched YAML config '{}'", configName);

        if (confRoot == null) {
            return rootMap;
        }

        if (!rootMap.containsKey(confRoot)) {
            throw new IllegalStateException("YAML configuration must contain the '" + confRoot + "' element");
        }
        return rootMap.getSubMap(confRoot);
    }

    /**
     * Resolve the given resource to an URL.
     * @param resourceName the name of the resource, typically a file name.
     * @return an URL to the resource.
     * @throws FileNotFoundException if the resource could not be located.
     * @throws MalformedURLException if the resource location could not be converted to an URL.
     */
    public static URL resolveConfigFile(String resourceName) throws FileNotFoundException, MalformedURLException {
        // TODO: This should be changed to use JNDI
        log.debug("Looking for '{}' on the classpath", resourceName);
        URL configURL = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (configURL ==  null) {
            log.debug("Looking for '" + resourceName + "' on the user home path");
            Path configPath = Path.of(System.getProperty("user.home"), resourceName);
            if (!configPath.toFile().exists()) {
                String message = "Unable to locate '" + resourceName + "' on the classpath or in user.home";
                log.error(message);
                throw new FileNotFoundException(message);
            }
            configURL = configPath.toUri().toURL();
        }
        log.debug("Resolved '{}' to '{}'", resourceName, configURL);
        return configURL;
    }
}
