package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Saves JAAS configuration into external file.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "dump-jaas",
    description = "Saves JAAS configuration into external file.")
public final class DumpJaasConfigurationCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "fileName", required = true, description = "Path to file used to store JAAS config")
    @SpecialUse
    private String fileName = "";


    @Override
    protected Void doExecute() throws IOException {
        try(final Writer out = new FileWriter(fileName)){
            SnampManagerImpl.dumpJaasConfiguration(bundleContext, out);
        }
        return null;
    }
}
