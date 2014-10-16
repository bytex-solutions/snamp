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
final class SetArrayCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "set-array";
    static final String COMMAND_USAGE = "set-array <attributeID> [OPTIONS]...";
    static final String COMMAND_DESC = "Sets array value";
    static final Options COMMAND_OPTIONS = new Options();
    private static final String IDX_OPT = "i";  //index option
    private static final String VAL_OPT = "v"; //value option
    private static final String INS_OPT = "a";  //insert value at index
    private static final String DEL_OPT = "d";  //delete value at index
    private static final String NUM_FMT_OPT = "n"; //number format
    private static final String DT_FMT_OPT = "f"; //date format

    static {
        COMMAND_OPTIONS.addOption(new Option(IDX_OPT, "index", true, "Array element index"));
        COMMAND_OPTIONS.addOption(new Option(VAL_OPT, "value", true, "Array element value"));
        COMMAND_OPTIONS.addOption(new Option(INS_OPT, "insert", false, "Insert element into an array"));
        COMMAND_OPTIONS.addOption(new Option(DEL_OPT, "delete", false, "Delete element from the array"));
        COMMAND_OPTIONS.addOption(new Option(NUM_FMT_OPT, "numberFormat", true, "Number parsing format"));
        COMMAND_OPTIONS.addOption(new Option(DT_FMT_OPT, "dateFormat", true, "Date/time parsing format"));
    }

    SetArrayCommand(final AdapterController controller){
        super(controller);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void insertArrayElement(final SshAttributeView attr,
                                           final String index,
                                           final String value,
                                           final Format fmt,
                                           final PrintWriter output) throws CommandException {
        try {
            final boolean result = attr.applyTransformation(SshAttributeView.InsertRowTransformation.class,
                    new SshAttributeView.Row(Integer.parseInt(index), fmt != null ? fmt.parseObject(value) : value));
            output.println(result ? "OK" :
                    "Unable to insert element");
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    private static void deleteArrayElement(final SshAttributeView attr,
                                           final String index,
                                           final PrintWriter output) throws CommandException {
        try {
            output.println(attr.applyTransformation(SshAttributeView.DeleteRowTransformation.class,
                    Integer.parseInt(index)) ? "OK" :
                    "Unable to delete element");
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    private static void updateArrayElement(final SshAttributeView attr,
                                           final String index,
                                           final String value,
                                           final Format fmt,
                                           final PrintWriter output) throws CommandException{
        try {
            final boolean result = attr.applyTransformation(SshAttributeView.UpdateRowTransformation.class,
                    new SshAttributeView.Row(Integer.parseInt(index), fmt != null ? fmt.parseObject(value) : value));
            output.println(result ? "OK" :
                    "Unable to insert element");
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        if(arguments.length == 1) {
            final SshAttributeView attr = controller.getAttribute(arguments[0]);
            if(attr == null) throw new CommandException("Attribute %s doesn't exist", arguments[0]);
            final Format fmt;
            if(input.hasOption(DT_FMT_OPT))
                fmt = new SimpleDateFormat(input.getOptionValue(DT_FMT_OPT));
            else if(input.hasOption(NUM_FMT_OPT))
                fmt = new DecimalFormat(input.getOptionValue(NUM_FMT_OPT));
            else fmt = null;
            //delete element
            if (input.hasOption(IDX_OPT) && input.hasOption(DEL_OPT)) {
                deleteArrayElement(attr, input.getOptionValue(IDX_OPT), output);
                return;
            }
            //update or insert element
            else if(input.hasOption(IDX_OPT) && input.hasOption(VAL_OPT)) {
                if (input.hasOption(INS_OPT))
                    insertArrayElement(attr, input.getOptionValue(IDX_OPT), input.getOptionValue(VAL_OPT), fmt, output);
                else
                    updateArrayElement(attr, input.getOptionValue(IDX_OPT), input.getOptionValue(VAL_OPT), fmt, output);
                return;
            }
        }
        throw new CommandException("Invalid command format");
    }
}
