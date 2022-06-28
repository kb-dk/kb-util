package dk.kb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Parses the property file 'build.properties' which has been populated with application name, version and build time.
 * The information is typically logged upon application start to make it easier for Operations an other log users
 * to determine what is running.
 *
 * As of 2022-06-27 this requires that the project pom.xml contains the sections
 * <pre>
 *   <properties>
 *     ...
 *     <!-- Needed to populate build.properties -->
 *     <timestamp>${maven.build.timestamp}</timestamp>
 *     <maven.build.timestamp.format>yyyy-MM-dd HH:mm</maven.build.timestamp.format>
 *     ...
 *   </properties>
 * </pre>
 * and
 * <pre>
 *   <build>
 *     <resources>
 *       <resource>
 *         <directory>src/main/resources/</directory>
 *         <includes>
 *           <include>build.properties</include>
 *         </includes>
 *         <filtering>true</filtering>
 *       </resource>
 *     </resources>
 *     ...
 *   </build>
 * </pre>
 * The file {@code src/main/resources/build.properties} should contain at least
 * <pre>
 *   # Build Time Information
 *   APPLICATION.NAME=${pom.name}
 *   APPLICATION.VERSION=${pom.version}
 *   APPLICATION.BUILDTIME=${timestamp}
 * </pre>
 */
public class BuildInfoManager {
    static final String BUILD_PROPERTY_FILE = "build.properties";

    private static final Logger log = LoggerFactory.getLogger(BuildInfoManager.class);

    private static String name = null;
    private static String version = null;
    private static String buildTime = null;

    /**
     * @return the human readable name of the application, as defined in pom.xml.
     */
    public static String getName() {
        if (name == null) {
            loadBuildInfo();
        }
        return name;
    }

    /**
     * @return the version of the application, as defined in pom.xml.
     */
    public static String getVersion() {
        if (version == null) {
            loadBuildInfo();
        }

        return version;
    }

    /**
     * @return the build time of the application.
     */
    public static String getBuildTime() {
        if (buildTime == null) {
            loadBuildInfo();
        }
        return buildTime;
    }

    /**
     * Load build information from {@link #BUILD_PROPERTY_FILE} on the path.
     */
    private static synchronized void loadBuildInfo() {
        if (name != null) { // Already resolved
            return;
        }

        Properties properties = new Properties();
        try (InputStream is = Resolver.resolveStream(BUILD_PROPERTY_FILE)) {
            if (is == null) {
                log.warn("Unable to load '" + BUILD_PROPERTY_FILE + "' from the classpath. " +
                         "Build information will be unavailable");
            } else {
                properties.load(is);
            }
        } catch (IOException e) {
            log.warn("Could not load build information from:" + BUILD_PROPERTY_FILE, e);
        }

        name = properties.getProperty("APPLICATION.NAME", "unknown");
        version = properties.getProperty("APPLICATION.VERSION", "unknown");
        buildTime = properties.getProperty("APPLICATION.BUILDTIME", "unknown");
    }
}
