package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Loads JAAS configuration from external file.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "setup-jaas",
    description = "Loads JAAS configuration from external file.")
public final class SetupJaasConfiguration extends OsgiCommandSupport implements SnampShellCommand {
    @SpecialUse
    @Argument(index = 0, name = "fileName", required = true, description = "Path to the file with JAAS configuration to load")
    private String fileName = "";

    @Override
    protected Void doExecute() throws IOException {
        try(final Reader reader = new FileReader(fileName)){
            SnampManagerImpl.saveJaasConfiguration(bundleContext, reader);
        }
        return null;
    }
}
