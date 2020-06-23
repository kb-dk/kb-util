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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
     *
     * @param map a map presumable delivered by SnakeYAML.
     */
    public YAML(Map<String, Object> map) {
        this.putAll(map);
    }
    
    /**
     * Resolves the YAML sub map at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the sub map.
     * @return the map at the path
     * @throws NotFoundException if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List of YAMLs
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    public YAML getSubMap(String path) throws NotFoundException {
        return getSubMap(path, false);
    }
    
    /**
     * Resolves the YAML sub map at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path         path for the sub map.
     * @param maintainKeys preserve the path prefix for the keys in the result
     * @return the map at the path
     * @throws NotFoundException if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a subMap
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    public YAML getSubMap(String path, boolean maintainKeys) throws NotFoundException, InvalidTypeException {
        Object found = get(path);
        if (found == null) {
            throw new NotFoundException("Path gives a null value", path);
        }
        
        if (!(found instanceof Map)) {
            throw new InvalidTypeException(
                    "Expected a Map for path but got '" + found.getClass().getName() + "'",path);
        }
        Map<String, Object> result;
        try {
            result = (Map<String, Object>) found;
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + found + "' to Map<String, Object>",path,
                                           e);
        }
        
        if (maintainKeys) {
            result = result.entrySet().stream().collect(Collectors.toMap(
                    entry -> path + "." + entry.getKey(),
                    Map.Entry::getValue
            ));
        }
        
        return new YAML(result);
    }
    
    /**
     * Resolves the list at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the list.
     * @return the list at the path
     * @throws NotFoundException if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path) throws NotFoundException, InvalidTypeException {
        Object found = get(path);
        if (found == null) {
            throw new NotFoundException("Path gives a null value", path);
        }
        
        if (!(found instanceof List)) {
            throw new InvalidTypeException(
                    "Expected a List for path but got '" + found.getClass().getName() + "'", path);
        }
        try {
            return (List<T>) found;
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + found + "' to List<T>", path, e);
        }
    }
    
    /**
     * Resolves the list of sub YAMLs at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the list.
     * @return the list of sub YAMLs at the path
     * @throws NotFoundException if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List of YAMLs
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    public List<YAML> getYAMLList(String path) throws NotFoundException, InvalidTypeException {
        Object found = get(path);
        if (found == null) {
            throw new NotFoundException("Path gives a null value", path);
        }
        
        if (!(found instanceof List)) {
            throw new InvalidTypeException(
                    "Expected a List for path but got '" + found.getClass().getName() + "'", path);
        }
        List<Map<String, Object>> hmList;
        try {
            hmList = (List<Map<String, Object>>) found;
        } catch (ClassCastException e) {
            throw new InvalidTypeException(
                    "Exception casting '" + found + "' to List<Map<String, Object>>", path, e);
        }
        return hmList.stream().map(YAML::new).collect(Collectors.toList());
    }
    
    /**
     * Resolves the integer at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the integer.
     * @return the integer at the path
     * @throws NotFoundException if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Integer
     * @throws NullPointerException if the path is null
     */
    public Integer getInteger(String path) throws NotFoundException {
        Object o = get(path);
        try {
            return Integer.valueOf(o.toString());
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Integer", path,  e);
        }
    }
    
    /**
     * Resolves the Integer at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path         path for the integer.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the integer at the path or defaultValue if it could not be located or parsed as an Integer.
     * @throws NullPointerException if the path is null
     */
    public Integer getInteger(String path, Integer defaultValue) {
        Object o;
        try {
            o = get(path);
        } catch (NotFoundException e) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(o.toString());
        } catch (NumberFormatException e) {
            log.warn("Unable to parse '" + o.toString() + "' as Integer", o);
            return defaultValue;
        }
    }
    
    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the boolean.
     * @return the boolean at the path
     * @throws NotFoundException if the path could not be found
     * @throws NullPointerException if the path is null
     */
    public Boolean getBoolean(String path) throws NotFoundException{
        Object o = get(path);
        return Boolean.valueOf(o.toString());
    }
    
    /**
     * Resolves the boolean at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path         path for the boolean.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the boolean at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    public Boolean getBoolean(String path, Boolean defaultValue) {
        Object o;
        try {
            o = get(path);
        } catch (NotFoundException e) {
            return defaultValue;
        }
        return Boolean.valueOf(o.toString());
        
    }
    
    /**
     * Resolves the String at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * Note: Object.toString is used to provide the String value, so this is safe to call for most YAML content.
     *
     * @param path path for the string.
     * @return the String at the path
     * @throws NotFoundException if the path could not be found
     * @throws NullPointerException if the path is null
     */
    public String getString(String path) throws NotFoundException {
        return get(path).toString();
    }
    
    /**
     * Resolves the string at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path         path for the Object.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the String at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    public String getString(String path, String defaultValue) {
        try {
            return get(path).toString();
        } catch (NotFoundException e) {
            return defaultValue;
        }
    }
    
    /* **************************** Path-supporting overrides ************************************ */
    
    /**
     * Used internally by {@link #get} to avoid endless recursion.
     *
     * @param key the key to look up.
     * @return the value for the key or null if the key is not present in the map.
     */
    private Object getSuper(String key) {
        return super.get(key);
    }
    
    /**
     * Checks if a value is present at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     *
     * @param path path for the Object.
     * @return true is an Object exists for the given path.
     * @throws NullPointerException if the path is null
     */
    @Override
    public boolean containsKey(Object path) {
        try {
            Object value = get(path);
            return value != null;
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    /**
     * Resolves the Object at the given path in the YAML. Supports {@code .} for path separation,
     * Sample path: foo.bar
     * Note: Keys in the YAML must not contain dots.
     * <p>
     * Returns this object if the path is empty
     *
     * @param pathO path for the Object.
     * @return the Object. Will never return null, will rather throw exceptions
     * @throws NotFoundException    if the path cannot be found
     * @throws NullPointerException if the path0 is null
     */
    //@SuppressWarnings("unchecked")
    @Override
    public Object get(Object pathO) throws NotFoundException, InvalidTypeException, NullPointerException {
        if (pathO == null) {
            throw new NullPointerException("Failed to query config for null path");
        }
        String path = pathO.toString().trim();
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        String[] pathElements = path.split(Pattern.quote("."));
        YAML current = this;
        for (int i = 0; i < pathElements.length; i++) {
            Object sub = current.getSuper(pathElements[i]);
            if (sub == null) {
                throw new NotFoundException(
                        "Unable to request " + i + "'th sub-element: '" + pathElements[i] + "'", path);
            }
            if (i == pathElements.length - 1) { //If this is the final pathElement, just return it
                return sub;
            } //Otherwise, we require that it is a map so we can continue to query
            if (!(sub instanceof Map)) {
                throw new InvalidTypeException(
                        "The " + i + "'th sub-element ('" + pathElements[i] + "') was not a Map", path);
            }
            try { //Update current as the sub we have found
                current = new YAML((Map<String, Object>) sub);
            } catch (ClassCastException e) {
                throw new InvalidTypeException(
                        "Expected a Map<String, Object> for path but got ClassCastException", path, e);
            }
        }
        return current;
    }
    
    /* **************************** Fetching YAML ************************************ */
    
    /**
     * Resolve the given YAML configuration.
     *
     * @param configName the name of the configuration file.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException if the configuration could not be fetched.
     */
    public static YAML resolveConfig(String configName) throws IOException {
        return resolveConfig(configName, null);
    }
    
    /**
     * Resolve the given YAML configuration.
     *
     * @param configName the name of the configuration file.
     * @param confRoot   the root element in the configuration or null if the full configuration is to be returned.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException                    if the configuration could not be fetched.
     * @throws java.io.FileNotFoundException  if the config name does not refer to a file
     * @throws java.net.MalformedURLException if the resource location could not be converted to an URL.
     * @throws IllegalArgumentException if the config cannot be parsed as YAML
     */
    public static YAML resolveConfig(String configName, String confRoot)
            throws IOException, FileNotFoundException, MalformedURLException, NotFoundException {
        URL configURL = Resolver.resolveConfigFile(configName);
        
        Map<String, Object> raw;
        try (InputStream configStream = configURL.openStream()) {
            raw = new Yaml().load(configStream);
            if (raw == null) {
                throw new IllegalArgumentException("The config resource '" + configURL
                                                   + "' does not contain a valid YAML configuration.");
            }
        } catch (IOException e) {
            throw new IOException(
                    "Exception trying to load the YAML configuration from '" + configURL + "'", e);
        }
        
        YAML rootMap = new YAML(raw);
        log.debug("Fetched YAML config '{}'", configName);
        
        if (confRoot == null) {
            return rootMap;
        }
        
        if (!rootMap.containsKey(confRoot)) {
            throw new NotFoundException("YAML configuration must contain the '" + confRoot + "' element", confRoot);
        } else {
            return rootMap.getSubMap(confRoot);
        }
    }
    
}
