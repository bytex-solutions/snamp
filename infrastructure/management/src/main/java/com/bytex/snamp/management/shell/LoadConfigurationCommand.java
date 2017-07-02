package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.management.http.model.AgentDataObject;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.Optional;

/**
 * Loads configuration from file.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "load-configuration",
        description = "Load configuration from file")
@Service
public final class LoadConfigurationCommand extends SnampShellCommand {
    @Argument(name = "fileName", index = 0, required = true, description = "Full path to the file")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String fileName = "";

    private static boolean loadConfiguration(final AgentConfiguration destination, final String fileName) throws IOException {
        final ObjectMapper jsonDeserializer = new ObjectMapper();
        try(final InputStream input = new FileInputStream(fileName);
            final Reader reader = new InputStreamReader(input, IOUtils.DEFAULT_CHARSET)){
            final AgentDataObject dto = jsonDeserializer.readValue(reader, AgentDataObject.class);
            destination.clear();
            dto.exportTo(destination);
        }
        return true;
    }

    @Override
    public void execute(final PrintWriter writer) throws Exception {
        final Optional<ServiceHolder<ConfigurationManager>> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef.isPresent()) {
            final ServiceHolder<ConfigurationManager> admin = adminRef.get();
            try {
                admin.get().processConfiguration(config -> loadConfiguration(config, fileName));
                writer.append("Configuration saved successfully");
            } finally {
                admin.release(getBundleContext());
            }
        } else
            throw new IOException("Configuration storage is not available");
    }
}
