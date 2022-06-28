package dk.kb.util.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Handling of auto-reloading of YAML configs with notification og changes to listeners.
 * See {@link {@url https://en.wikipedia.org/wiki/Observer_pattern}}
 *
 * If wanted, changes to the configuration source (typically files) can result in an update and
 * a callback to relevant classes. To enable this, add autoupdate keys to the YAML config:
 * <pre>
 * config:
 *   autoupdate:
 *     enabled: true
 *     intervalms: 60000
 * </pre>
 * Notifications on config changes can be received using {@link #registerObserver(Observer)}.
 *
 * Alternatively {@link #AUTO_UPDATE_DEFAULT} and {@link #AUTO_UPDATE_MS_DEFAULT} can be set so that auto-update is
 * enabled by default for the application.
 *
 * Implementation note: Watching for changes is a busy-wait, i.e. the ServiceConfig actively reloads the configuration
 * each {@link #autoUpdateMS} milliseconds and checks if is has changed. This is necessary as the source for the
 * configuration is not guaranteed to be a file (it could be a URL or packed in a WAR instead), so watching for file
 * system changes is not solid enough. This also means that the check does have a non-trivial overhead so setting the
 * autoupdate interval to less than a minute is not recommended.
 */
public class AutoYAML {
    private static final Logger log = LoggerFactory.getLogger(AutoYAML.class);

    private final Set<Observer> observers = new HashSet<>();

    private static final String AUTO_UPDATE_KEY = ".config.autoupdate.enabled";
    private boolean AUTO_UPDATE_DEFAULT = false;
    private static final String AUTO_UPDATE_MS_KEY = ".config.autoupdate.intervalms";
    private long AUTO_UPDATE_MS_DEFAULT = 60*1000; // every minute

    private boolean autoUpdate = AUTO_UPDATE_DEFAULT;
    private long autoUpdateMS = AUTO_UPDATE_MS_DEFAULT;
    private AutoUpdater autoUpdater = null;

    private String configSource = null;

    /**
     * Besides parsing of YAML files using SnakeYAML, the YAML helper class provides convenience
     * methods like {@code getInteger("someKey", defaultValue)} and {@code getSubMap("config.sub1.sub2")}.
     */
    private YAML yaml;

    /**
     * Construct a null YAML resource.
     * The caller is responsible for calling {@link #initialize(String)} at a later point.
     */
    public AutoYAML() {
        // This code intentionally left blank
    }

    /**
     * Construct a potentially auto loading YAML resource. Default setup is not to auto update:
     * Either call the constructor with explicit auto update parameters or specify auto updating in the provided
     * configSource.
     * @param configSource the configuration to load. Can be null.
     * @throws IOException if the configuration could not be loaded or parsed.
     */
    public AutoYAML(String configSource) throws IOException {
        this(configSource, null, null);
    }

    /**
     * Construct a potentially auto loading YAML resource.
     * @param configSource the configuration to load. Can be null.
     * @param defaultAutoUpdate   whether or not auto updating of the config is enabled per default.
     *                            This can be overridden in the configSource.
     *                            If null, the default value (false) will be used.
     * @param defaultAutoUpdateMS if auto update is enabled, checking for changes will be performed periodically
     *                            with the given milliseconds as delay between checks.
     *                            This can be overridden in the configSource.
     *                            If null, the default value (60000 ms) will be used.
     *
     * @throws IOException if the configuration could not be loaded or parsed.
     */
    public AutoYAML(String configSource, Boolean defaultAutoUpdate, Long defaultAutoUpdateMS) throws IOException {
        if (defaultAutoUpdate != null) {
            AUTO_UPDATE_DEFAULT = defaultAutoUpdate;
        }
        if (defaultAutoUpdateMS != null) {
            AUTO_UPDATE_MS_DEFAULT = defaultAutoUpdateMS;
        }
        if (configSource != null) {
            initialize(configSource);
        }
    }

    /**
     * Initialized the configuration from the provided config source (file, classpath or URL).
     * If used with a web service, this should normally be called from a ContextListener as part of web server
     * initialization of the container.
     * @param configSource the configuration to load.
     * @throws IOException if the configuration could not be loaded or parsed.
     */
    public synchronized void initialize(String configSource) throws IOException {
        this.configSource = configSource;
        assignConfig(YAML.resolveLayeredConfigs(configSource));
    }

    private void assignConfig(YAML conf) {
        log.debug("Assigning config with {} observers and autoUpdate={}",
                  observers.size(), conf.getBoolean(AUTO_UPDATE_KEY, AUTO_UPDATE_DEFAULT));
        conf.setExtrapolate(true); // Enable system properties expansions, such as '${user.home}'
        yaml = conf;
        notifyObservers();
        setupAutoConfig();
    }

    private synchronized void setupAutoConfig() {
        autoUpdate = yaml.getBoolean(AUTO_UPDATE_KEY, AUTO_UPDATE_DEFAULT);
        autoUpdateMS = yaml.getLong(AUTO_UPDATE_MS_KEY, AUTO_UPDATE_MS_DEFAULT);

        if (autoUpdater != null) {
            autoUpdater.shutdown();
        }
        if (!autoUpdate) {
            return;
        }

        autoUpdater = new AutoUpdater(configSource, autoUpdateMS);
    }

    /**
     * Direct access to the backing YAML-class is used for configurations with more flexible content
     * and/or if the service developer prefers key-based property access.
     *
     * Note that reloading of the configuration, either by explicit call to {@link #initialize(String)}
     * or by the auto refreshing framework, does NOT change the {@code YAML} itself as it is immutable.
     * If auto reloading is used, either call {@code getYAML()} each time properties need to be read or
     * subscribe to changes with {@link #registerObserver(Observer)}.
     * @return the current backing YAML-handler for the configuration.
     */
    public YAML getYAML() {
        if (yaml == null) {
            throw new IllegalStateException("The YAML configuration should have been loaded, but was not");
        }
        return yaml;
    }

    /**
     * Shuts down the ServiceConfig, which in practise means shutting down auto-updating of the configuration.
     * The ServiceConfig should not be used after this has been called.
     */
    public void shutdown() {
        log.info("Shutting down");
        if (autoUpdater != null) {
            autoUpdater.shutdown();
        }
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Register an observer of configuration changes.
     * If the configuration has already been loaded, which is the default case, the observer is notified immediately.
     *
     * Reloading of the configuration is project dependent and must be enabled for the observer to be notified on
     * subsequent changes to the configuration.
     * Call {@link #isAutoUpdating()} to determine whether the configuration might be updated post-initialization.
     * @param observer called upon registration if a config exists, and if the configuration changes.
     */
    public synchronized void registerObserver(Observer observer) {
        log.debug("Registering configuration update observer {}", observer);
        observers.add(observer);
        if (yaml != null) {
            observer.setConfig(yaml);
        }
    }

    /**
     * Unregisters a previously registered configuration change observer.
     * @param observer an observer previously added with {@link #registerObserver(Observer)}.
     * @return true if the observer was previously registered, else false.
     */
    public synchronized boolean unregisterObserver(Observer observer) {
        boolean wasThere = observers.remove(observer);
        log.debug(wasThere ?
                          "Unregistered configuration update observer {}" :
                          "Attempted to unregister configuration update observer {} but is was not found",
                  observer);
        return wasThere;
    }

    private void notifyObservers() {
        observers.forEach(o -> o.setConfig(yaml));
    }

    /**
     * Functional equivalent of {@code Consumer<YAML>} with a less generic method name, to support registering observers
     * with {@code registerObserver(this)} instead of {@code registerObserver(this::setConfig}.
     */
    @FunctionalInterface
    public interface Observer {
        void setConfig(YAML config);
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * @return true if the configuration is automatically reloaded if the source files are changed.
     */
    public boolean isAutoUpdating() {
        return autoUpdate;
    }

    /**
     * Checks for changes of the underlying configuration sources and triggers application configuration changes.
     */
    private class AutoUpdater extends Thread {
        private boolean shutdown = false;

        private final String configSource;
        private final long intervalMS;
        private int noSleeps = 0;

        private long lastCheck = -1;

        public AutoUpdater(String configSource, long intervalMS) {
            super("ConfigUpdate_" + System.currentTimeMillis());
            this.configSource = configSource;
            this.intervalMS = intervalMS;
            this.setDaemon(true);
            log.info("Starting config watcher with {} ms interval", intervalMS);
            this.start();
        }

        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            while (!shutdown) {
                checkForChange();
                try {
                    long timeToWait = (lastCheck + intervalMS) - System.currentTimeMillis();
                    if (timeToWait > 0) {
                        Thread.sleep(timeToWait);
                    } else {
                        if (noSleeps++ < 10) {
                            log.warn("AutoUpdate: No sleep before next check for configuration change. " +
                                     "The interval of {} ms should probably be longer. " +
                                     "This message is muted after 10 occurrences for the current config", intervalMS);
                        }
                    }
                } catch (InterruptedException e) {
                    // Do nothing as the while-check handles interruptions
                }
            }
            log.debug("Stopping config watcher as shutdown has been called");
        }

        private void checkForChange() {
            lastCheck = System.currentTimeMillis();
            log.debug("AutoUpdate: Loading YAML from config source '{}'", configSource);
            YAML candidate;
            try {
                candidate = YAML.resolveLayeredConfigs(configSource);
            } catch (IOException e) {
                log.warn("AutoUpdate: Exception while loading config", e);
                return;
            }

            if (candidate == null) {
                log.warn("AutoUpdate: Got null when loading from source config '{}'", configSource);
                return;
            }

            if (yaml == null || !candidate.toString().equals(yaml.toString())) {
                log.debug("AutoUpdate: Detected configuration change, triggering update");
                assignConfig(candidate);
            }
        }

        /**
         * Stop the auto updater.
         */
        public void shutdown() {
            shutdown = true;
            this.interrupt();
        }
    }

}
