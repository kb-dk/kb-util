package dk.kb.util.yaml;

import dk.kb.util.Resolver;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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
class AutoYAMLTest {

    // The only thing we test here is that configuration loading succeeds
    @Test
    void loadConfigTest() throws IOException {
        new AutoYAML(Resolver.resolveURL("system_properties.yaml").getPath());
    }

    @Tag("slow")
    @Test
    void autoLoadTest() throws IOException, InterruptedException {
        final String CONF0 = "config:\n  autoupdate:\n    enabled: true\n    intervalms: 100\n  somevalue: 0";
        final String CONF1 = "config:\n  autoupdate:\n    enabled: true\n    intervalms: 100\n  somevalue: 1";
        final String CONF2 = "config:\n  autoupdate:\n    enabled: false\n    intervalms: 100\n  somevalue: 2";
        final AtomicInteger reloads = new AtomicInteger(0);
        final String VALUE_KEY = ".config.somevalue";

        AutoYAML auto = new AutoYAML();

        // Initial state
        File conf = File.createTempFile("java-webapp_config_", ".yaml");
        FileUtils.writeStringToFile(conf, CONF0, StandardCharsets.UTF_8);
        auto.registerObserver(yaml -> reloads.incrementAndGet());
        final int baseReloads = reloads.get(); // autoht might have been initialized already so keep track

        auto.initialize(conf.toString());
        assertTrue(auto.isAutoUpdating(), "Config should be auto updating");
        assertEquals(baseReloads+1, reloads.get(), "After init, reloads should be " + (baseReloads+1));
        assertEquals(0, auto.getYAML().getInteger(VALUE_KEY), "Initial value should match");

        Thread.sleep(200);
        assertEquals(baseReloads+1, reloads.get(), "After first sleep, reloads should still be correct");

        // Update config file with same content: Should not trigger anything
        FileUtils.writeStringToFile(conf, CONF0, StandardCharsets.UTF_8);
        Thread.sleep(200);
        assertEquals(baseReloads+1, reloads.get(), "After second sleep, reloads should still be correct (new config is identical to old)");

        // Update config with new content
        FileUtils.writeStringToFile(conf, CONF1, StandardCharsets.UTF_8);
        Thread.sleep(200);
        assertEquals(baseReloads+2, reloads.get(), "After third sleep, reloads should still be correct");
        assertEquals(1, auto.getYAML().getInteger(VALUE_KEY), "First change value should match");

        Thread.sleep(200);
        assertEquals(baseReloads+2, reloads.get(), "After fourth sleep, reloads should still be 1 (no change at all)");

        // Second update and disabling of auto-update
        FileUtils.writeStringToFile(conf, CONF2, StandardCharsets.UTF_8);
        Thread.sleep(200);
        assertEquals(baseReloads+3, reloads.get(), "After fifth sleep, reloads should be correct");
        assertEquals(2, auto.getYAML().getInteger(VALUE_KEY), "Second change value should match");
        assertFalse(auto.isAutoUpdating(), "Config should have auto updating turned off");
    }

    @Tag("fast")
    @Test
    void systemProperties() throws IOException {
        AutoYAML auto = new AutoYAML(Resolver.resolveURL("system_properties.yaml").getPath());

        {
            String homeQuote = auto.getYAML().getString("config.userhome");
            assertFalse(homeQuote.contains("$"), "The value for 'config.userhome' should be expanded");
        }
        {
            Integer fallback = auto.getYAML().getInteger("config.fallback");
            assertEquals(87, fallback, "Expanding a non-existing property with fallback should yield the fallback");
        }

    }
}
