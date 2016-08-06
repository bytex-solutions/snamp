package com.bytex.snamp.security.auth.login.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bytex.snamp.security.auth.login.json.spi.JsonConfigurationSpi;
import org.junit.Assert;
import org.junit.Test;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.File;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class JsonConfigurationTest extends Assert {
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
