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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
     */
    public static URL resolveConfigFile(String resourceName) throws FileNotFoundException, MalformedURLException {
        Path verbatimPath = Path.of(resourceName);
        if (Files.exists(verbatimPath)) {
            return verbatimPath.toUri().toURL();
        }
        log.debug("Looking for '{}' on the classpath", resourceName);
        URL configURL = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (configURL ==  null) {
            log.debug("Looking for '{}' on the user home path", resourceName );
            Path configPath = Path.of(System.getProperty("user.home"), resourceName);
            if (!Files.exists(configPath)) {
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
