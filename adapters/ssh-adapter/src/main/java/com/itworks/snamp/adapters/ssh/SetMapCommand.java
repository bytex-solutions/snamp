package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.MapBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SetMapCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "set-map";
    static final String COMMAND_USAGE = "set-map <attributeID> [OPTIONS]...";
    static final String COMMAND_DESC = "Modifies the attribute as map";
    static final Options COMMAND_OPTIONS = new Options();
    private static final String PAIR_OPT = "p";

    static {
        Option opt = new Option(PAIR_OPT, "option", true, "Key/value pair");
        opt.setRequired(true);
        opt.setArgName("key=value");
        opt.setArgs(2);
        opt.setValueSeparator('=');
        COMMAND_OPTIONS.addOption(opt);
    }

    SetMapCommand(final AdapterController controller){
        super(controller);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void updateMapEntries(final SshAttributeView attr,
                                         final Properties entries,
                                         final PrintWriter output) throws CommandException{
        try {
            output.println(attr.applyTransformation(SshAttributeView.UpdateMapTransformation.class,
                    MapBuilder.toMap(entries)) ? "OK" : "Unable to set map entries");
        } catch (final ReflectiveOperationException | TimeoutException e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        if(arguments.length == 1 && input.hasOption(PAIR_OPT)){
            final SshAttributeView attr = controller.getAttribute(arguments[0]);
            updateMapEntries(attr, input.getOptionProperties(PAIR_OPT), output);
        }
        else throw invalidArgFormat();
    }
}
