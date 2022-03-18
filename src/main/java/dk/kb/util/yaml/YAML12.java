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
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Wrapper for SnakeYAML output for easier access to the YAML elements.
 * <p>
 * Primary use case is for configuration files, using either of
 * <ul>
 *     <li>{@link #resolveLayeredConfigs(String...)} which treats multiple files as separate YAMLs, where the content
 *     of the YAMLs is merged and atomic values are overwritten with the latest file</li>
 *     <li>{@link #resolveMultiConfig(String...)} which treats multiple files as a single YAML allowing for
 *     cross referencing inside of the YAML parts</li>
 * </ul>
 * <p>
 * For standard use, the {@link #resolveLayeredConfigs(String...)} is recommended as it makes it simple to layer the
 * configurations:
 * <ol>
 *     <li>myapp_behaviour.yaml (base behaviour settings, goes into the main repo)</li>
 *     <li>myapp_environment.yaml (servers, usernames, passwords..., is controlled by Operations)</li>
 *     <li>myapp_local_overrides.yaml (local overrides, used for developing and testing)</li>
 * </ol>
 */
public class YAML12 extends LinkedHashMap<String, Object> {
    
    private static final Logger log = LoggerFactory.getLogger(YAML12.class);
    
    private static final long serialVersionUID = -5211961549015821194L;
    
    
    private static final Pattern ARRAY_ELEMENT = Pattern.compile("^([^\\[]*)\\[([^]]*)]$");
    
    private boolean extrapolateSystemProperties = false;
    
    /**
     * Creates an empty YAML.
     */
    public YAML12() {
        super();
    }
    
    /**
     * Resolves one or more resources using globbing and returns YAML based on the concatenated resources.
     * <p>
     * Note: This method merges the YAML configs as-is: Any key-collisions are handled implicitly by keeping the latest
     * key-value pair in the stated configurations. Sub-entries are not merged on key collisions.
     * Use {@link #resolveLayeredConfigs} for treating YAML files as overlays when loading.
     *
     * @param resourceName glob for YAML files.
     * @throws IOException if the files could not be loaded or parsed.
     * @see #resolveLayeredConfigs(String...)
     * @see #resolveLayeredConfigs(MERGE_ACTION, MERGE_ACTION, String...)
     */
    public YAML12(String resourceName) throws IOException {
        this.putAll(YAML12.resolveMultiConfig(resourceName));
    }
    
    /**
     * Creates a YAML wrapper around the given map.
     * Changes to the map will be reflected in the YAML instance and vice versa.
     *
     * @param map a map presumable delivered by SnakeYAML.
     */
    public YAML12(Map<String, Object> map) {
        this.putAll(map);
    }
    
    /**
     * Creates a YAML wrapper around the given map.
     * Changes to the map will be reflected in the YAML instance and vice versa.
     *
     * @param map                         a map presumable delivered by SnakeYAML.
     * @param extrapolateSystemProperties should system properties be extrapolated in values
     */
    public YAML12(Map<String, Object> map, boolean extrapolateSystemProperties) {
        this.extrapolateSystemProperties = extrapolateSystemProperties;
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
    public YAML12 getSubMap(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
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
    public YAML12 getSubMap(String path, boolean maintainKeys)
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
        
        return new YAML12(result, extrapolateSystemProperties);
    }
    
    /**
     * Resolves the list at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path path for the list.
     * @param <T>  the type of elements in the list
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
            return (List<T>) extrapolate(found);
        } catch (ClassCastException e) {
            throw new InvalidTypeException("Exception casting '" + found + "' to List<T>", path, e);
        }
    }
    
    /**
     * Resolves the list at the given path in the YAML. See {@link #get(Object)} for path syntax.
     * Sample path: foo.bar
     *
     * @param path        path for the list.
     * @param <T>         the type of elements in the list
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
    public List<YAML12> getYAMLList(String path) throws NotFoundException, InvalidTypeException, NullPointerException {
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
        return hmList.stream().map(map -> new YAML12(map, extrapolateSystemProperties)).collect(Collectors.toList());
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
            return Short.valueOf(toString(o));
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
            return Integer.valueOf(toString(o));
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
            return Long.valueOf(toString(o));
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
            return Double.valueOf(toString(o));
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
            return Float.valueOf(toString(o));
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
        return Boolean.valueOf(toString(o));
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
        return toString(get(path)).stripTrailing();
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
     * "foo.[2]" or "foo.[2].bar" - YAML-key.[last] for the last element in a list, e.g. "foo.[last]" or
     * "foo.bar.[last]"
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
        YAML12 current = this;
        for (int i = 0; i < pathElements.length; i++) {
            String fullPathElement = pathElements[i];
            Matcher matcher = ARRAY_ELEMENT.matcher(fullPathElement);
            final String pathKey;
            final String arrayElementIndex;
            if (matcher.matches()) { // foo.bar[2]
                pathKey           = matcher.group(1);
                arrayElementIndex = matcher.group(2);
            } else {
                pathKey           = fullPathElement;
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
                    subCollection = ((Map<String, Object>) sub).values();
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
                current = new YAML12((Map<String, Object>) sub, extrapolateSystemProperties);
            } catch (ClassCastException e) {
                throw new InvalidTypeException(
                        "Expected a Map<String, Object> for path but got ClassCastException", path, e);
            }
        }
        return current;
    }
    
    private <T> T extrapolate(T sub) {
        if (sub == null) {
            return null;
        }
        if (extrapolateSystemProperties()) {
            if (sub instanceof List<?>) {
                List<?> objects = (List<?>) sub;
                return (T) objects.stream().map(o -> extrapolate(o)).collect(Collectors.toList());
            }
            if (sub instanceof Map) {
                Map<String,?> map = (Map<String,?>) sub;
                return (T) map.entrySet()
                              .stream()
                              .map((Map.Entry<String,?> entry) -> Map.entry(entry.getKey(), extrapolate(entry.getValue())))
                              .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            }
            return (T) toString(sub);
        }
        return sub;
    }
    
    /* **************************** Fetching YAML ************************************ */
    
    /**
     * Resolve the given YAML configuration.
     * <p>
     * Note: The resolver supports globbing so {@code /home/someone/myapp-conf/*.yaml} expands to all YAML-files
     * in the {@code myapp} folder. When globbing is used, the matching files are iterated in alphanumerical order
     * so that subsequent YAML definitions overwrites previous ones. See also {@link #resolveMultiConfig(String...)}.
     *
     * @param configName the name, path or glob for the configuration file.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException                    if the configuration could not be fetched.
     * @throws FileNotFoundException  if the config name does not refer to a file
     * @throws MalformedURLException if the resource location could not be converted to an URL.
     * @throws InvalidPathException           if the configName is not valid as a path
     * @throws NullPointerException           if the configName is null
     * @throws IllegalArgumentException       if the config cannot be parsed as YAML
     * @deprecated use {@link #resolveLayeredConfigs(String...)} or {@link #resolveMultiConfig(String...)} instead.
     */
    @NotNull
    @Deprecated
    public static YAML12 resolveConfig(String configName) throws
                                                        IOException,
                                                        FileNotFoundException,
                                                        MalformedURLException,
                                                        NullPointerException,
                                                        InvalidPathException {
        return resolveConfig(configName, null);
    }
    
    /**
     * Resolve the given YAML configuration.
     * <p>
     * Note: The resolver supports globbing so {@code /home/someone/myapp-conf/*.yaml} expands to all YAML-files
     * in the {@code myapp-conf} folder. When globbing is used, the matching files are iterated in alphanumerical order
     * so that subsequent YAML definitions overwrites previous ones. See also {@link #resolveMultiConfig(String...)}.
     *
     * @param configName the path, name or glob of the configuration file.
     * @param confRoot   the root element in the configuration or null if the full configuration is to be returned.
     * @return the configuration parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException                    if the configuration could not be fetched.
     * @throws FileNotFoundException  if the config name does not refer to a file
     * @throws MalformedURLException if the resource location could not be converted to an URL.
     * @throws InvalidPathException           if the configName is not valid as a path
     * @throws NullPointerException           if the configName is null
     * @throws IllegalArgumentException       if the config cannot be parsed as YAML
     * @throws NotFoundException              if the confRoot is not null and is not found in the YAML document
     * @throws InvalidTypeException           if the confRoot was not null and is invalid (i.e. if treated a value as a
     *                                        map)
     * @deprecated use {@link #resolveLayeredConfigs(String...)} or {@link #resolveMultiConfig(String...)} instead.
     */
    @Deprecated
    public static YAML12 resolveConfig(String configName, String confRoot) throws IOException {
        YAML12 rootMap = resolveMultiConfig(configName);
        
        if (confRoot == null) {
            return rootMap;
        }
        
        if (!rootMap.containsKey(confRoot)) {
            throw new NotFoundException("YAML configuration must contain the '" + confRoot + "' element", confRoot);
        } else {
            return rootMap.getSubMap(confRoot);
        }
    }
    
    /**
     * Parse the given configStream as a single YAML.
     * <p>
     * Note: This method merges the YAML config as-is: Any key-collisions are handled implicitly by keeping the latest
     * key-value pair. Sub-entries are not merged on key collisions, meaning that key-collisions at the root level
     * replaces the full tree under the key. References are supported with this method.
     *
     * @param yamlStream YAML.
     * @return a YAML based on the given stream.
     */
    public static YAML12 parse(InputStream yamlStream) {
        Object raw = new Load(LoadSettings.builder()
                                          .setParseComments(true)
                                          .setAllowDuplicateKeys(true)
                                          .build()).loadFromInputStream(yamlStream);
        
        if (raw instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) raw;
            //Get a classcast exception here, and not someplace later https://stackoverflow.com/a/509288
            for (String s : map.keySet())
                ;
            for (Object o : map.values())
                ;
            
            YAML12 rootMap = new YAML12(map, false);
            log.trace("Parsed YAML config stream");
            return rootMap;
        } else {
            throw new IllegalArgumentException(
                    "The config resource does not evaluate to a valid YAML configuration.");
        }
        
    }
    
    /**
     * Parse the given Paths as a single YAML, effectively concatenating all paths.
     * It is possible to cross-reference between the individual paths.
     * Duplicate keys across files are handled by last-wins, with no merging of sub-entries.
     * <p>
     * Use {@link #resolveLayeredConfigs} for treating multiple YAML files as overlays.
     *
     * @param yamlPaths paths to YAML Files.
     * @return a YAML based on the given paths.
     * @throws IOException           if a configuration could not be fetched.
     * @throws FileNotFoundException if a yamlFile could not be located.
     * @throws AccessDeniedException if a resource at a yamlPath could not be read.
     */
    public static YAML12 parse(Path... yamlPaths) throws IOException {
        return parse(Arrays.stream(yamlPaths).map(Path::toFile).toArray(File[]::new));
    }
    
    /**
     * Parse the given Files as a single YAML, effectively concatenating all files.
     * It is possible to use cross references between the individual files.
     * Duplicate keys across files are handled by last-wins, with no merging of sub-entries.
     * <p>
     * Use {@link #resolveLayeredConfigs} for treating multiple YAML files as overlays.
     *
     * @param yamlFiles path to YAML Files.
     * @return a YAML based on the given stream.
     * @throws IOException           if a configuration could not be fetched.
     * @throws FileNotFoundException if a yamlFile could not be located.
     * @throws AccessDeniedException if a resource at a yamlFile could not be read.
     */
    public static YAML12 parse(File... yamlFiles) throws IOException {
        // Check is files can be read
        for (File yamlFile : yamlFiles) {
            if (!yamlFile.exists()) {
                throw new FileNotFoundException("The file '" + yamlFile + "' could not be found");
            }
            if (!yamlFile.canRead()) {
                throw new AccessDeniedException("The file '" + yamlFile + "' could not be read (check permissions)");
            }
        }
        
        List<InputStream> configs = null;
        try {
            // Convert to InputStreams
            configs = Arrays.stream(yamlFiles)
                            .map(YAML12::openStream)
                            .collect(Collectors.toList());
            
            // Concatenate all InputStreams
            InputStream yamlStream = null;
            for (InputStream config : configs) {
                yamlStream = yamlStream == null ? config : new SequenceInputStream(yamlStream, config);
            }
            
            // Perform a single parse of the content
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
     * Resolve the given YAML configurations and present a merged YAML from that.
     * <p>
     * Note: This method merges the YAML configs as-is: Any key-collisions are handled implicitly by keeping the latest
     * key-value pair in the stated configurations. Sub-entries are not merged on key collisions.
     * This means that references across configResources is possible.
     * Use {@link #resolveLayeredConfigs} for treating multiple YAML files as overlays.
     * <p>
     * Note 2: The resolver supports globbing so {@code /home/someone/myapp-conf/*.yaml} expands to all YAML-files
     * in the {@code myapp} folder. When globbing is used, the matching files in each glob are parsed in alphanumerical
     * order for that glob. The overall order is the given array of configResources
     *
     * @param configResources the names, paths or globs of the configuration files.
     * @return the configurations merged and parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException           if a configuration could not be fetched.
     * @throws FileNotFoundException if none of the given configResources could be resolved.
     * @throws AccessDeniedException if any of the resolved config files could not be read.
     * @see #resolveLayeredConfigs for alternative.
     */
    public static YAML12 resolveMultiConfig(String... configResources) throws IOException {
        Path[] configPaths = Arrays.stream(configResources)
                                   .map(Resolver::resolveGlob).flatMap(Collection::stream)
                                   .toArray(Path[]::new);
        if (configPaths.length == 0) {
            throw new FileNotFoundException("No paths resolved from " + Arrays.toString(configResources));
        }
        return parse(configPaths);
    }
    
    /**
     * Resolve the given YAML configurations, merging key-value pairs from subsequent configs into the first one.
     * This is typically used to support easy overwriting of specific parts of a major configuration file.
     * This is shorthand for {@code resolveLayeredConfig(MERGE_ACTION.union, MERGE_ACTION.kee_extra, configResources}:
     * The values for duplicate keys in YAMLs are merged, lists and atomic values are overwritten with the values
     * from extra.
     * <p>
     * Note: As opposed to {@link #resolveMultiConfig(String...)} this approach does not allow for references
     * across configResources.
     * <p>
     * Note 2: The resolver supports globbing so {@code /home/someone/myapp-conf/*.yaml} expands to all YAML-files
     * in the {@code myapp} folder. When globbing is used, the matching files in each glob are parsed in alphanumerical
     * order for that glob. The overall order is the given array of configResources
     *
     * @param configResources the names, paths or globs of the configuration files.
     * @return the configurations merged and parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException           if a configuration could not be fetched.
     * @throws FileNotFoundException if none of the given configResources could be resolved.
     * @throws AccessDeniedException if any of the resolved config files could not be read.
     * @see #resolveMultiConfig for alternative.
     */
    public static YAML12 resolveLayeredConfigs(String... configResources) throws IOException {
        return resolveLayeredConfigs(MERGE_ACTION.union, MERGE_ACTION.keep_extra, configResources);
    }
    
    /**
     * Resolve the given YAML configurations, merging key-value pairs from subsequent configs into the first one.
     * This is typically used to support easy overwriting of specific parts of a major configuration file.
     * <p>
     * Note: As opposed to {@link #resolveMultiConfig(String...)} this approach does not allow for references
     * across configResources.
     * <p>
     * Note 2: The resolver supports globbing so {@code /home/someone/myapp-conf/*.yaml} expands to all YAML-files
     * in the {@code myapp} folder. When globbing is used, the matching files in each glob are parsed in alphanumerical
     * order for that glob. The overall order is the given array of configResources
     *
     * @param configResources the names, paths or globs of the configuration files.
     * @param defaultMA       the general action to take when a key collision is encountered. Also used for maps
     *                        (YAMLs).
     *                        Typically this will be {@link MERGE_ACTION#union}.
     * @param listMA          the action to take when a key collision for a list is encountered.
     *                        Typically this will be {@link MERGE_ACTION#union}.
     * @return the configurations merged and parsed up as a tree represented as Map and wrapped as YAML.
     * @throws IOException           if a configuration could not be fetched.
     * @throws FileNotFoundException if none of the given configResources could be resolved.
     * @throws AccessDeniedException if any of the resolved config files could not be read.
     * @see #resolveMultiConfig for alternative.
     */
    public static YAML12 resolveLayeredConfigs(MERGE_ACTION defaultMA, MERGE_ACTION listMA, String... configResources)
            throws IOException {
        List<Path> configPaths = Arrays.stream(configResources)
                                       .map(Resolver::resolveGlob).flatMap(Collection::stream)
                                       .collect(Collectors.toList());
        if (configPaths.isEmpty()) {
            throw new FileNotFoundException("No paths resolved from " + Arrays.toString(configResources));
        }
        YAML12 compound = new YAML12();
        for (Path configPath : configPaths) {
            compound = compound.merge(YAML12.parse(configPath), defaultMA, listMA);
        }
        return compound;
    }
    
    private static InputStream openStream(Path path) {
        try {
            return path.toUri().toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException("IOException opening stream for '" + path + "'", e);
        }
    }
    
    private static InputStream openStream(File file) {
        try {
            return file.toPath().toUri().toURL().openStream();
        } catch (IOException e) {
            throw new RuntimeException("IOException opening stream for '" + file + "'", e);
        }
    }
    
    /**
     * Merges the extra YAML into this YAML.
     * <p>
     * The merge uses {@link MERGE_ACTION#union} aka extra-wins: The values for duplicate keys in YAMLs are merged,
     * lists and atomic values are overwritten with the values from extra.
     * <p>
     * Shallow copying is used when possible, so updates to extra after the merge is strongly discouraged.
     *
     * @param extra the YAML that will be added to this.
     * @return this YAML, updated with the values from extra.
     */
    public YAML12 merge(YAML12 extra) {
        return merge(this, extra, MERGE_ACTION.union, MERGE_ACTION.keep_extra);
    }
    
    /**
     * Merges the extra YAML into this YAML. In case of key collisions, the stated merge actions are taken.
     * <p>
     * Shallow copying is used when possible, so updates to extra after the merge is strongly discouraged.
     *
     * @param extra     the YAML that will be added to this.
     * @param defaultMA the general action to take when a key collision is encountered. Also used for maps (YAMLs)
     *                  Typically this will be {@link MERGE_ACTION#union}.
     * @param listMA    the action to take when a key collision for a list is encountered.
     *                  Typically this will be {@link MERGE_ACTION#union}.
     * @return this YAML, udpated with the values from extra.
     */
    public YAML12 merge(YAML12 extra, MERGE_ACTION defaultMA, MERGE_ACTION listMA) {
        return merge(this, extra, defaultMA, listMA);
    }
    
    /**
     * Merges the extra YAML into the base YAML.
     * The merge uses union/extra-wins: The values for duplicate keys in YAMLs are merged, lists and atomic values are
     * overwritten with the values from extra.
     * <p>
     * Shallow copying is used when possible, so updates to extra after the merge is strongly discouraged.
     *
     * @param base  the YAML that will be updated with the content from extra.
     * @param extra the YAML that will be added to base.
     * @return the updated base YAML.
     */
    public static YAML12 merge(YAML12 base, YAML12 extra) {
        return merge(base, extra, MERGE_ACTION.union, MERGE_ACTION.keep_extra);
    }
    
    /**
     * Merges the extra YAML into the base YAML. In case of key collisions, the stated merge actions are taken.
     * <p>
     * Shallow copying is used when possible, so updates to extra after the merge is strongly discouraged.
     * <p>
     * The available MERGE_ACTIONs are<br>
     * union: Duplicate maps are merged, lists are concatenated, atomics are overwritten by last entry.<br>
     * keep_base: Duplicate maps, lists and atomics are ignored.<br>
     * keep_extra: Duplicate maps, lists and atomics are overwrittes, so that the last encounterd key-value pair
     * wins.<br>
     * fail: Duplicate maps, lists and atomics throws an exception.<br>
     *
     * @param base      the YAML that will be updated with the content from extra.
     * @param extra     the YAML that will be added to base.
     * @param defaultMA the general action to take when a key collision is encountered. Also used for maps (YAMLs)
     *                  Typically this will be {@link MERGE_ACTION#union}.
     * @param listMA    the action to take when a key collision for a list is encountered.
     *                  Typically this will be {@link MERGE_ACTION#union}.
     * @return the updated base YAML.
     */
    public static YAML12 merge(YAML12 base, YAML12 extra, MERGE_ACTION defaultMA, MERGE_ACTION listMA) {
        return (YAML12) mergeEntry("", base, extra, defaultMA, listMA);
    }
    
    @SuppressWarnings("unchecked")
    private static Object mergeEntry(
            String path, Object base, Object extra, MERGE_ACTION defaultMA, MERGE_ACTION listMA) {
        final String pre = "Configuration incompatibility at " + path;
        
        if (extra instanceof LinkedHashMap) { // YAML is a LinkedHashMap
            if (!(base instanceof LinkedHashMap)) {
                throw new IllegalArgumentException(
                        pre + ": Attempting to merge value type " + extra.getClass() + " to type " + base.getClass());
            }
            
            LinkedHashMap<Object, Object> bYAML = (LinkedHashMap<Object, Object>) base;
            LinkedHashMap<Object, Object> eYAML = (LinkedHashMap<Object, Object>) extra;
            eYAML.forEach((key, value) -> {
                mergeValueToYAML(path, bYAML, key, value, defaultMA, listMA);
            });
            return base;
        }
        
        if (extra instanceof List) {
            if (!(base instanceof List)) {
                throw new IllegalArgumentException(
                        pre + ": Attempting to merge value type " + List.class + " to type " + base.getClass());
            }
            switch (listMA) {
                case fail:
                    throw new IllegalArgumentException(
                            pre + ": Duplicate keys with list merge action " + MERGE_ACTION.fail);
                case keep_base:
                    return base;
                case keep_extra:
                    return extra;
                case union:
                    ((List<Object>) base).addAll((List<Object>) extra);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown merge action for list '" + defaultMA + "'");
            }
            return base;
        }
        
        // When it's not a map or a list we don't care about type
        switch (defaultMA) {
            case fail:
                throw new IllegalArgumentException(
                        pre + ": Duplicate keys with merge action " + MERGE_ACTION.fail);
            case keep_base:
                return base;
            case keep_extra:
                return extra;
            case union:
                return extra; // TODO: Should we do something else here? Make a type-aware merger? Fail?
            default:
                throw new UnsupportedOperationException("Unknown merge action '" + defaultMA + "'");
        }
    }
    
    private static void mergeValueToYAML(String path, LinkedHashMap<Object, Object> base, Object key, Object value,
                                         MERGE_ACTION defaultMA, MERGE_ACTION listMA) {
        if (!base.containsKey(key)) {
            base.put(key, value);
            return;
        }
        base.put(key, mergeEntry(path + "." + key, base.get(key), value, defaultMA, listMA));
    }
    
    /**
     * If the YAML will extrapolate the current values of System.getProperties() in the values returned
     *
     * @return true if extrapolation is enabled
     */
    public boolean extrapolateSystemProperties() {
        return extrapolateSystemProperties;
    }
    
    /**
     * Set the YAML to extrapolate the current values of System.getProperties() in the values returned
     *
     * @return this YAML, not a copy
     */
    public YAML12 extrapolateSystemProperties(boolean extrapolateSystemProperties) {
        this.extrapolateSystemProperties = extrapolateSystemProperties;
        return this;
    }
    
    /**
     * If the YAML will extrapolate the current values of System.getProperties() in the values returned
     *
     * @return true if extrapolation is enabled
     */
    public boolean isExtrapolateSystemProperties() {
        return extrapolateSystemProperties;
    }
    
    /**
     * Set the YAML to extrapolate the current values of System.getProperties() in the values returned
     */
    public void setExtrapolateSystemProperties(boolean extrapolateSystemProperties) {
        this.extrapolateSystemProperties = extrapolateSystemProperties;
    }
    
    public String toString() {
        return toString(this);
    }
    
    protected String toString(Object object) {
        JsonScalarResolver tagResolver = new JsonScalarResolver();
        
        DumpSettings settings = DumpSettings.builder()
                                            .setDefaultFlowStyle(FlowStyle.BLOCK)
                                            .setIndentWithIndicator(true)
                                            .setIndicatorIndent(2)
                                            .setExplicitEnd(false)
                                            .setScalarResolver(tagResolver)
                                            .setIndent(2)
                                            .build();
        BaseRepresenter representer = new StandardRepresenter(settings) {
            @Override
            protected Node representScalar(Tag tag, String value, ScalarStyle style) {
                if (extrapolateSystemProperties()) {
                    String newValue = StringSubstitutor.replaceSystemProperties(value);
                    if (!newValue.equals(value)) {
                        value = newValue;
                        tag   = tagResolver.resolve(value, tag == Tag.STR);
                    }
                }
                return super.representScalar(tag, value, style);
            }
        };
        
        return new Dump(settings, representer).dumpToString(object).stripTrailing();
    }
    
    public enum MERGE_ACTION {
        /**
         * Duplicate maps are merged, lists are concatenated, atomics are overwritten by last entry
         */
        union,
        /**
         * Duplicate maps, lists and atomics are ignored.
         */
        keep_base,
        /**
         * Duplicate maps, lists and atomics are overwrittes, so that the last encounterd key-value pair wins.
         */
        keep_extra,
        /**
         * Duplicate maps, lists and atomics throws an exception.
         */
        fail
    }
    
    ;
    
}
