package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SetAttributeCommand extends AbstractManagementShellCommand {
    static final String COMMAND_USAGE = "set <attribute-id> <value> [OPTIONS]...";
    static final String COMMAND_NAME = "set";
    static final String COMMAND_DESC = "Override value of the attribute (scalar data type only)";

    static final Options COMMAND_OPTIONS = new Options();
    private static final String DT_FORMAT_OPT = "d";
    private static final String NUM_FORMAT_OPT = "n";

    static {
        Option opt = new Option(DT_FORMAT_OPT, "dateTimeFormat", true, "Date time format");
        opt.setArgName("format");
        opt.setRequired(false);
        COMMAND_OPTIONS.addOption(opt);
        opt = new Option(NUM_FORMAT_OPT, "numberFormat", true, "Number format");
        opt.setArgName("format");
        opt.setRequired(false);
        COMMAND_OPTIONS.addOption(opt);
    }

    SetAttributeCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private void setScalarValue(final String attributeID,
                                final String value,
                                final Format fmt,
                                final PrintWriter output) throws CommandException {
        final SshAttributeView attr = getAdapterController().getAttribute(attributeID);
        if (attr == null) throw new CommandException("Attribute %s doesn't exist.", attributeID);
        try {
            attr.setValue(fmt != null ? fmt.parseObject(value) : value);
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        //handle scalar set
        if(arguments.length == 2) {
            final Format fmt;
            if (input.hasOption(DT_FORMAT_OPT))
                fmt = new SimpleDateFormat(input.getOptionValue(DT_FORMAT_OPT));
            else if (input.hasOption(NUM_FORMAT_OPT))
                fmt = new DecimalFormat(input.getOptionValue(NUM_FORMAT_OPT));
            else fmt = null;
            setScalarValue(arguments[0], arguments[1], fmt, output);
        }
        else throw invalidCommandFormat();
    }
}
