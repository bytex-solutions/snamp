package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.MapBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

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
    private static final String NUM_FMT_OPT = "n"; //number format
    private static final String DT_FMT_OPT = "d"; //date format

    static {
        Option opt = new Option(PAIR_OPT, "option", true, "Key/value pair");
        opt.setRequired(true);
        opt.setArgName("key=value");
        opt.setArgs(2);
        opt.setValueSeparator('=');
        COMMAND_OPTIONS.addOption(opt);
        COMMAND_OPTIONS.addOption(new Option(NUM_FMT_OPT, "numberFormat", true, "Number parsing format"));
        COMMAND_OPTIONS.addOption(new Option(DT_FMT_OPT, "dateFormat", true, "Date/time parsing format"));
    }

    SetMapCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void updateMapEntries(final SshAttributeView attr,
                                         final Properties entries,
                                         final Format fmt,
                                         final PrintWriter output) throws CommandException {
        try {
            final Map<String, Object> map = MapBuilder.createStringHashMap(entries.size());
            for (final String key : entries.stringPropertyNames())
                map.put(key, fmt != null ? fmt.parseObject(entries.getProperty(key)) : entries.getProperty(key));
            output.println(attr.applyTransformation(SshAttributeView.UpdateMapTransformation.class,
                    map) ? "OK" : "Unable to set map entries");
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        if(arguments.length == 1 && input.hasOption(PAIR_OPT)){
            final Format fmt;
            if(input.hasOption(NUM_FMT_OPT))
                fmt = new DecimalFormat(input.getOptionValue(NUM_FMT_OPT));
            else if(input.hasOption(DT_FMT_OPT))
                fmt = new SimpleDateFormat(input.getOptionValue(DT_FMT_OPT));
            else fmt = null;
            final SshAttributeView attr = getAdapterController().getAttribute(arguments[0]);
            updateMapEntries(attr, input.getOptionProperties(PAIR_OPT), fmt, output);
        }
        else throw invalidCommandFormat();
    }
}
