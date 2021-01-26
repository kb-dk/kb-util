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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolves resources using best effort (verbatim given string, looking in user home, on the path etc).
 */
public class Resolver {
    private static final Logger log = LoggerFactory.getLogger(Resolver.class);
    
    
    
    /**
     * Resolve the given resource to an URL. Order of priority:
     * 1. Verbatim as file
     * 2. On the path
     * 3. In user home
     * @param resourceName the name of the resource, typically a file name.
     * @return an URL to the resource.
     * @throws FileNotFoundException if the resource could not be located.
     * @throws MalformedURLException if the resource location could not be converted to an URL.
     * @deprecated use {@link #resolveURL(String)} instead.
     */
    @Deprecated
    public static URL resolveConfigFile(String resourceName) throws FileNotFoundException, MalformedURLException {
        return resolveURL(resourceName);
    }
    
    
    /**
     * Resolve the given resource to an URL. Order of priority:
     * 1. Verbatim as file
     * 2. On the path
     * 3. In user home
     *
     * @param resourceName the name of the resource, typically a file name.
     * @return an URL to the resource.
     * @throws FileNotFoundException              if the resource could not be located.
     * @throws MalformedURLException              if the resource location could not be converted to an URL.
     * @throws NullPointerException               if resourceName is null
     * @throws java.nio.file.InvalidPathException if the resourceName is not a legal path-name
     */
    public static URL resolveURL(String resourceName)
            throws NullPointerException, FileNotFoundException, MalformedURLException, InvalidPathException {
        URL configURL;
        Path verbatimPath = Path.of(resourceName);
        if (Files.exists(verbatimPath)) {
            configURL = verbatimPath.toUri().toURL();
        } else {
            log.debug("Looking for '{}' on the classpath", resourceName);
            configURL = Thread.currentThread().getContextClassLoader().getResource(resourceName);
            if (configURL == null) {
                log.debug("'{}' not found on the classpath, so looking for in the user home path", resourceName);
                Path configPath = Path.of(System.getProperty("user.home"), resourceName);
                if (!Files.exists(configPath)) {
                    String message = "Unable to locate '" + resourceName + "' on the classpath or in user.home";
                    //log.error(message);
                    throw new FileNotFoundException(message);
                }
                configURL = configPath.toUri().toURL();
            }
        }
        log.debug("Resolved '{}' to '{}'", resourceName, configURL);
        return configURL;
    }

    /**
     * Resolve 0 or more files from a given glob. If no absolute path is given, the current path and users home is used.
     * Examples: {@code myconfig*.yaml}, {@code setup/myconfig.yaml}, {@code /home/someapp/conf/prod-*-conf/*.yaml}.
     * @param glob a Unix-style glob, using the syntax from {@link java.nio.file.FileSystem#getPathMatcher(String)}
     *             with the exception of {@code **}.
     * @return a list of Paths matching the given glob or the empty list if there were no matches.
     */
    public static List<Path> resolveGlob(String glob) {
        List<Path> paths = new ArrayList<>();
        if (glob == null || glob.isEmpty()) {
            return paths;
        }

        List<String> prefixes = new ArrayList<>();
        if (Paths.get(glob).isAbsolute()) {
            prefixes.add(""); // Empty String is prefix for absolute paths
        } else { // user home & current
            prefixes.add(System.getProperty("user.home") + File.separator);
            prefixes.add(FileSystems.getDefault().getPath("").toAbsolutePath().toString() + File.separator);
        }

        for (String prefix: prefixes) {
            String absoluteGlob = prefix + glob;
            List<PathMatcher> matchers = Arrays.stream(absoluteGlob.split(Pattern.quote(File.separator))).
                    filter(segment -> !segment.isEmpty()).
                    map(segment -> FileSystems.getDefault().getPathMatcher("glob:" + segment)).
                    collect(Collectors.toList());
            for (File root: File.listRoots()) {
                walkMatches(root.toPath(), matchers, 0, paths::add);
            }
        }
        log.debug("Resolved glob '" + glob + "' to " + paths.size() + " files");
        return paths;
    }
    // Recursive descend through folders matching the segments
    private static void walkMatches(
            Path current, List<PathMatcher> matchers, int matchersIndex, Consumer<Path> consumer) {
        if (!Files.isDirectory(current)) {
            if (matchersIndex == matchers.size()) {
                consumer.accept(current);
            }
            return;
        }
        try {
            Files.list(current).
                    filter(path -> matchers.get(matchersIndex).matches(path.getFileName())).
                    forEach(path -> walkMatches(path, matchers, matchersIndex+1, consumer));
        } catch (IOException e) {
            throw new RuntimeException("IOException while walking " + current, e);
        }
    }

    /**
     * Wrapper for {@link #resolveConfigFile(String)} that opens an InputStream for the given resource.
     * Note: This method has no checked exceptions: It wraps IOExceptions as runtime exceptions.
     *
     * @param resourceName the name of the resource, typically a file name.
     * @return a stream with the given resource.
     */
    public static InputStream resolveStream(String resourceName) {
        try {
            return resolveURL(resourceName).openStream();
        } catch (Exception e) {
            throw new RuntimeException("Exception fetching resource '" + resourceName + "'", e);
        }
    }
    
    /**
     * Resolves the resource using {@link #resolveStream(String)}, assuming UTF-8, and returns its content as a String.
     *
     * @param resourceName the name of the resource, typically a file name.
     * @return the content of the UTF-8 resource as a String.
     * @throws IOException if reading failed
     */
    public static String resolveUTF8String(String resourceName) throws IOException {
        return resolveString(resourceName, StandardCharsets.UTF_8);
    }
    
    /**
     * Resolves the resource using {@link #resolveStream(String)} and returns its content as a String.
     *
     * @param resourceName the name of the resource, typically a file name.
     * @param charset      the charset to use when converting the bytes for the resource to a String.
     * @return the content of the resource as a String.
     * @throws IOException if reading failed
     */
    public static String resolveString(String resourceName, Charset charset) throws IOException {
        try (InputStream in = resolveStream(resourceName);
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            pipe(in, out);
            return out.toString(charset);
        }
    }
    
    /**
     * Shorthand for {@link #pipe(java.io.InputStream, java.io.OutputStream, int)}.
     * A default buffer of 4KB is used.
     *
     * @param in  The source stream.
     * @param out The target stream.
     * @throws java.io.IOException If any sort of read/write error occurs on either stream.
     */
    public static void pipe(InputStream in, OutputStream out) throws IOException {
        pipe(in, out, 4096);
    }
    
    /**
     * Copies the contents of an InputStream to an OutputStream, then closes both.
     *
     * @param in      The source stream.
     * @param out     The destination stram.
     * @param bufSize Number of bytes to attempt to copy at a time.
     * @throws java.io.IOException If any sort of read/write error occurs on either stream.
     */
    public static void pipe(InputStream in, OutputStream out, int bufSize) throws IOException {
        try (in; out) {
            byte[] buf = new byte[bufSize];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
    
    /**
     * Get the absolute path to a file on classpath
     * @param pathOnClasspath the path to the file on classpath
     * @return the absolute path to the file, or null if the file could not be found
     */
    public static Path getPathFromClasspath(String pathOnClasspath) {
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(pathOnClasspath);
            if (resource == null){
                return null;
            }
            return new File(resource.toURI()).toPath().toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Read a file on classpath into a string. The file will be read with the UTF8 charset
     * @param name the path to the file
     * @return the file contents as a string, or null if the file could not be found
     * @throws IOException if reading failed
     */
    public static String readFileFromClasspath(String name) throws IOException {
        try (InputStream resourceAsStream = Thread.currentThread()
                                                  .getContextClassLoader()
                                                  .getResourceAsStream(name);) {
            if (resourceAsStream == null) {
                log.warn("Failed to find file {}, returning null", name);
                return null;
            } else {
                return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            }
        }
    }
    
    /**
     * open an inputstream for a  on classpath
     * @param name the path to the file
     * @return the inputstream to the file
     */
    public static InputStream openFileFromClasspath(String name) {
        return Thread.currentThread()
                                                  .getContextClassLoader()
                                                  .getResourceAsStream(name);
    }
}
