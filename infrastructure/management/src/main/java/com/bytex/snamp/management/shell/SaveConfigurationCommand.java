package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.management.http.model.AgentDataObject;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.*;

/**
 * Saves configuration into file.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "save-configuration",
        description = "Save configuration into file")
@Service
public final class SaveConfigurationCommand extends SnampShellCommand {
    @Argument(name = "fileName", index = 0, required = true, description = "Full path to the file")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String fileName = "";

    private static void saveConfiguration(final AgentConfiguration configuration, final String fileName) throws IOException {
        final ObjectWriter jsonSerializer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        try (final OutputStream output = new FileOutputStream(fileName, false);
             final Writer writer = new OutputStreamWriter(output, IOUtils.DEFAULT_CHARSET)) {
            jsonSerializer.writeValue(writer, new AgentDataObject(configuration));
        }
    }

    @Override
    public Object execute() throws Exception {
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef != null)
            try {
                adminRef.get().readConfiguration(config -> saveConfiguration(config, fileName));
                return "Configuration saved successfully";
            } finally {
                adminRef.release(getBundleContext());
            }
        else throw new IOException("Configuration storage is not available");
    }
}
