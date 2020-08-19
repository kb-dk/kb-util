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
package dk.kb.util.yaml;

import dk.kb.util.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.validation.constraints.NotNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.io.SequenceInputStream;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Wrapper for SnakeYAML output for easier access to the YAML elements.
 */
public class YAML extends LinkedHashMap<String, Object> {

    private static final Logger log = LoggerFactory.getLogger(YAML.class);

    private static final long serialVersionUID = -5211961549015821194L;

    
    private static final Pattern ARRAY_ELEMENT = Pattern.compile("^([^\\[]*)\\[([^]]*)]$");
    
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
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List of YAMLs
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public YAML getSubMap(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
        return getSubMap(path, false);
    }
    
    /**
     * Resolves the YAML sub map at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the sub map.
     * @param maintainKeys preserve the path prefix for the keys in the result
     * @return the map at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a subMap
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public YAML getSubMap(String path, boolean maintainKeys)
            throws NotFoundException, InvalidTypeException, NullPointerException {
        Object found = get(path);
        if (found == null) {
            throw new NotFoundException("Path gives a null value", path);
        }
        
        if (!(found instanceof Map)) {
            throw new InvalidTypeException(
                    "Expected a Map for path but got '" + found.getClass().getName() + "'", path);
        }
        Map<String, Object> result;
        try {
            result = (Map<String, Object>) found;
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + found + "' to Map<String, Object>", path,
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
     * Resolves the list at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the list.
     * @param <T> the type of elements in the list
     * @return the list at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> List<T> getList(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
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
     * Resolves the list at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path        path for the list.
     * @param <T> the type of elements in the list
     * @param defaultList if the path cannot be resolved, return this value.
     * @return the list at the path or defaultList if it could not be located.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path, List<T> defaultList) {
        try {
            return getList(path);
        } catch (NotFoundException | InvalidPathException e) {
            return defaultList;
        }
    }
    
    /**
     * Resolves the list of sub YAMLs at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the list.
     * @return the list of sub YAMLs at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value cannot be parsed as a List of YAMLs
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public List<YAML> getYAMLList(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
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
     * Resolves the Short at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the Short.
     * @return the Short at the path or null if it could not be located.
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Short
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    public Short getShort(String path) {
        Object o = get(path);
        try {
            return Short.valueOf(o.toString());
        } catch (NumberFormatException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Short", path, e);
        }
    }
    
    /**
     * Resolves the Short at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the Short.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Short at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    public Short getShort(String path, Short defaultValue) {
        try {
            return getShort(path);
        } catch (NotFoundException | InvalidTypeException e) {
            return defaultValue;
        }
    }
    
    /**
     * Resolves the integer at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the integer.
     * @return the integer at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Integer
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public Integer getInteger(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
        Object o = get(path);
        try {
            return Integer.valueOf(o.toString());
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Integer", path, e);
        }
    }
    
    /**
     * Resolves the Integer at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the integer.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the integer at the path or defaultValue if it could not be located or parsed as an Integer.
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public Integer getInteger(String path, Integer defaultValue) throws NullPointerException {
        Object o;
        try {
            o = get(path);
        } catch (NotFoundException | InvalidTypeException e) {
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
     * Resolves the Long at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the Long.
     * @return the Long at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Long
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    public Long getLong(String path) {
        Object o = get(path);
        try {
            return Long.valueOf(o.toString());
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Long", path, e);
        }
    }
    
    /**
     * Resolves the Long at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the Long.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Long at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    public Long getLong(String path, Long defaultValue) {
        try {
            return getLong(path);
        } catch (NotFoundException | InvalidTypeException e) {
            return defaultValue;
        }
    }
    
    /**
     * Resolves the double at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the double.
     * @return the Double at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Double
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    public Double getDouble(String path) {
        Object o = get(path);
        try {
            return Double.valueOf(o.toString());
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Double", path, e);
        }
    }
    
    /**
     * Resolves the double at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the double.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Double at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    public Double getDouble(String path, Double defaultValue) {
        try {
            return getDouble(path);
        } catch (NotFoundException | InvalidTypeException e) {
            return defaultValue;
        }
    }
    
    /**
     * Resolves the float at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the Float.
     * @return the Float at the path or null if it could not be located.
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the value is not a valid Float
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    public Float getFloat(String path) {
        Object o = get(path);
        try {
            return Float.valueOf(o.toString());
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + o + "' to Float", path, e);
        }
    }
    
    /**
     * Resolves the float at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the Float.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the Float at the path or defaultValue if it could not be located.
     */
    public Float getFloat(String path, Float defaultValue) {
        try {
            return getFloat(path);
        } catch (NotFoundException | InvalidTypeException e) {
            return defaultValue;
        }
    }
    
    /**
     * Resolves the boolean at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the boolean.
     * @return the boolean at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public Boolean getBoolean(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
        Object o = get(path);
        return Boolean.valueOf(o.toString());
    }
    
    /**
     * Resolves the boolean at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the boolean.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the boolean at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public Boolean getBoolean(String path, Boolean defaultValue) throws NullPointerException {
        try {
           return getBoolean(path);
        } catch (NotFoundException | InvalidTypeException e) {
            return defaultValue;
        }
    }
    
    /**
     * Resolves the string at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     * Note: Object.toString is used to provide the String value, so this is safe to call for most YAML content.
     *
     * @param path path for the string.
     * @return the String at the path
     * @throws NotFoundException    if the path could not be found
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public String getString(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
        return get(path).toString();
    }
    
    /**
     * Resolves the string at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path         path for the Object.
     * @param defaultValue if the path cannot be resolved, return this value.
     * @return the String at the path or defaultValue if it could not be located.
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public String getString(String path, String defaultValue) throws NullPointerException {
        try {
            return getString(path);
        } catch (NotFoundException | InvalidTypeException e) {
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
     * Checks if a value is present at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the Object.
     * @return true is an Object exists for the given path.
     * @throws NullPointerException if the path is null
     */
    @Override
    public boolean containsKey(Object path) throws NullPointerException {
        try {
            Object value = get(path);
            return value != null;
        } catch (NotFoundException | InvalidTypeException e) {
            return false;
        }
    }
    
    /**
     * Resolves the Object at the given path in the YAML. Path elements are separated by {@code .} and can be either -
     * YAML-key for direct traversal, e.g. "foo" or "foo.bar" - YAML-key[index] for a specific element in a list, e.g.
     * "foo.[2]" or "foo.[2].bar" - YAML-key.[last] for the last element in a list, e.g. "foo.[last]" or "foo.bar.[last]"
     * Note: Keys in the YAML must not contain dots.
     * <p>
     * Returns this object if the path is empty
     *
     * @param pathO path for the Object.
     * @return the Object. Will never return null, will rather throw exceptions
     * @throws NotFoundException    if the path cannot be found
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     * @throws NullPointerException if the path0 is null
     * @throws InvalidTypeException if the path was invalid (i.e. if treated a value as a map)
     */
    @SuppressWarnings("unchecked")
    @Override
    @NotNull
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
            String fullPathElement = pathElements[i];
            Matcher matcher = ARRAY_ELEMENT.matcher(fullPathElement);
            final String pathKey;
            final String arrayElementIndex;
            if (matcher.matches()) { // foo.bar[2]
                pathKey = matcher.group(1);
                arrayElementIndex = matcher.group(2);
            } else {
                pathKey = fullPathElement;
                arrayElementIndex = null;
            }
            Object sub;
            if (pathKey.isEmpty()) {
                sub = current;
            } else {
                sub = current.getSuper(pathKey);
            }
            if (sub == null) {
                throw new NotFoundException(
                        "Unable to request " + i + "'th sub-element: '" + pathKey + "'", path);
            }
            
            if (arrayElementIndex != null) { // foo.bar.[2]
                final Collection<Object> subCollection;
                if (sub instanceof List) {
                    subCollection = (List<Object>) sub;
                } else if (sub instanceof Map) {
                    subCollection = ((Map<String,Object>) sub).values();
                } else {
                    throw new InvalidTypeException(String.format(
                            Locale.ENGLISH, "Key %s requested but the element %s was of type %s instead of Collection",
                            fullPathElement, pathKey, sub.getClass().getSimpleName()), path);
        
                }
    
                int index = "last".equals(arrayElementIndex) ?
                                    subCollection.size() - 1 :
                                    Integer.parseInt(arrayElementIndex);
                if (index >= subCollection.size()) {
                    throw new IndexOutOfBoundsException(String.format(
                            Locale.ENGLISH, "The index %d is >= collection size %d for path element %s in path %s",
                            index, subCollection.size(), fullPathElement, path));
                }
                sub = subCollection.stream().skip(index).findFirst().orElseThrow(
                        () -> new RuntimeException("This should not happen..."));
            }
            
            if (i == pathElements.length - 1) { //If this is the final pathElement, just return it
                return sub;
            } //Otherwise, we require that it is a map so we can continue to query
            
            //If sub is a list, make it a map with the indexes as keys
            if (sub instanceof List) {
                List<Object> list = (List<Object>) sub;
                LinkedHashMap<String, Object> map = new LinkedHashMap<>(list.size());
                for (int j = 0; j < list.size(); j++) {
                    map.put(j + "", list.get(j));
                }
                sub = map;
            }
            if (!(sub instanceof Map)) {
                throw new InvalidTypeException(
                        "The " + i + "'th sub-element ('" + pathKey + "') was not a Map", path);
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
     * @throws IOException                    if the configuration could not be fetched.
     * @throws java.io.FileNotFoundException  if the config name does not refer to a file
     * @throws java.net.MalformedURLException if the resource location could not be converted to an URL.
     * @throws InvalidPathException           if the configName is not valid as a path
     * @throws NullPointerException           if the configName is null
     * @throws IllegalArgumentException       if the config cannot be parsed as YAML
     */
    @NotNull
    public static YAML resolveConfig(String configName) throws IOException,
                                                                       FileNotFoundException,
                                                                       MalformedURLException,
                                                                       NullPointerException,
                                                                       InvalidPathException {
        return resolveConfig(configName, null);
    }
    
    /**
     * Parse the given configStream as YAML.
     *
     * @param yamlStream YAML.
     * @return a YAML based on the given stream.
     */
    public static YAML parse(InputStream yamlStream) {
        Object raw = new Yaml().load(yamlStream);
        if (raw instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String,Object> map = (Map<String,Object>) raw;
            //Get a classcast exception here, and not someplace later https://stackoverflow.com/a/509288
            for (String s : map.keySet());
            for (Object o : map.values());
            
            YAML rootMap = new YAML(map);
            log.trace("Parsed YAML config stream");
            return rootMap;
        } else {
            throw new IllegalArgumentException("The config resource does not evaluate to a valid YAML configuration.");
        }
        
    }
    
    /**
     * Resolve the given YAML configurations and present a merged YAML from that.
     * Note: This method merges the YAML configs as-is: Any key-collisions are handled implicitly by keeping the latest
     * key-value pair in the stated configurations.
     *
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
                for (InputStream config : configs) {
                    config.close();
                }
            }
        }
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
     * @throws InvalidPathException           if the configName is not valid as a path
     * @throws NullPointerException           if the configName is null
     * @throws IllegalArgumentException       if the config cannot be parsed as YAML
     * @throws NotFoundException              if the confRoot is not null and is not found in the YAML document
     * @throws InvalidTypeException           if the confRoot was not null and is invalid (i.e. if treated a value as a map)
     */
    public static YAML resolveConfig(String configName, String confRoot) throws IOException {
        YAML rootMap = resolveMultiConfig(configName);
        
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
