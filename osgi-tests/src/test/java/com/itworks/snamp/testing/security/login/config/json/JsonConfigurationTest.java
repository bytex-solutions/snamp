package com.itworks.snamp.testing.security.login.config.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.security.auth.login.json.JsonConfiguration;
import com.itworks.snamp.security.auth.login.json.spi.JsonConfigurationSpi;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.File;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class JsonConfigurationTest extends AbstractUnitTest {
    @Test
    public void serializationDeserializationTest() throws IOException {
        final Gson formatter = JsonConfigurationSpi.init(new GsonBuilder()).create();
        JsonConfiguration config = new JsonConfiguration();
        final ImmutableMap<String, String> opts = ImmutableMap.of("debug", "true");
        config.put("a", new AppConfigurationEntry("org.eclipse.jetty.jaas.spi.PropertyFileLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, opts));
        final String value = formatter.toJson(config);
        assertNotNull(value);
        assertFalse(value.isEmpty());
        assertNotEquals("{}", value);
        config =  formatter.fromJson(value, JsonConfiguration.class);
        assertNotNull(config);
        assertTrue(config.containsKey("a"));
        assertNotNull(config.getAppConfigurationEntry("a"));
        assertEquals(1, config.getAppConfigurationEntry("a").length);
        final File tempFile = File.createTempFile("snamp", "test");
        config.serialize(formatter, tempFile);
    }
}
