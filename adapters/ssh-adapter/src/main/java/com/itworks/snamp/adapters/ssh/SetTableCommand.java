package com.itworks.snamp.adapters.ssh;

import com.google.common.collect.Maps;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
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
final class SetTableCommand extends AbstractManagementShellCommand{
    static final String COMMAND_NAME = "set-table";
    static final String COMMAND_USAGE = "set-table <attributeID> [OPTIONS]...";
    static final String COMMAND_DESC = "Modifies table";
    static final Options COMMAND_OPTIONS = new Options();
    private static final String IDX_OPT = "i";    //row index
    private static final String DEL_OPT = "d";    //delete row
    private static final String INS_OPT = "a";    //insert row
    private static final String ROW_OPT = "r";    //row content
    private static final String NUM_FMT_OPT = "n"; //number format
    private static final String DT_FMT_OPT = "f"; //date format

    static {
        Option opt = new Option(ROW_OPT, "row", true, "Row value");
        opt.setArgs(2);
        opt.setArgName("key=value");
        opt.setValueSeparator('=');
        COMMAND_OPTIONS.addOption(opt);
        COMMAND_OPTIONS.addOption(new Option(INS_OPT, "insert", false, "Insert element into an array"));
        COMMAND_OPTIONS.addOption(new Option(DEL_OPT, "delete", false, "Delete element from the array"));
        COMMAND_OPTIONS.addOption(new Option(NUM_FMT_OPT, "numberFormat", true, "Number parsing format"));
        COMMAND_OPTIONS.addOption(new Option(DT_FMT_OPT, "dateFormat", true, "Date/time parsing format"));
        opt = new Option(IDX_OPT, "index", true, "Row index");
        opt.setRequired(true);
        COMMAND_OPTIONS.addOption(opt);
    }

    SetTableCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void deleteTableRow(final SshAttributeView attr,
                                       final String index,
                                       final PrintWriter output) throws CommandException {
        try {
            attr.applyTransformation(SshAttributeView.DeleteRowTransformation.class,
                    Integer.parseInt(index));
            output.println("Deleted");
        } catch (final Exception e) {
            throw new CommandException(e);
        }
    }

    private static void insertTableRow(final SshAttributeView attr,
                                       final String index,
                                       final Properties entries,
                                       final Format fmt,
                                       final PrintWriter output) throws CommandException {
        try {
            final Map<String, Object> newRow = Maps.newHashMapWithExpectedSize(entries.size());
            for (final String key : entries.stringPropertyNames())
                newRow.put(key, fmt != null ? fmt.parseObject(entries.getProperty(key)) : entries.getProperty(key));
            output.println(attr.applyTransformation(SshAttributeView.InsertRowTransformation.class,
                    new SshAttributeView.Row(Integer.parseInt(index), newRow)) ? "OK" :
                    "Unable to insert row");
        }
        catch (final Exception e){
            throw new CommandException(e);
        }
    }

    private static void updateTableRow(final SshAttributeView attr,
                                       final String index,
                                       final Properties entries,
                                       final Format fmt,
                                       final PrintWriter output) throws CommandException {
        try {
            final Map<String, Object> newRow = Maps.newHashMapWithExpectedSize(entries.size());
            for (final String key : entries.stringPropertyNames())
                newRow.put(key, fmt != null ? fmt.parseObject(entries.getProperty(key)) : entries.getProperty(key));
            output.println(attr.applyTransformation(SshAttributeView.UpdateRowTransformation.class,
                    new SshAttributeView.Row(Integer.parseInt(index), newRow)) ? "OK" :
                    "Unable to insert row");
        }
        catch (final Exception e){
            throw new CommandException(e);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        if(arguments.length == 1 && input.hasOption(IDX_OPT)){
            final SshAttributeView attr = getAdapterController().getAttribute(arguments[0]);
            if(attr == null) throw new CommandException("Attribute %s doesn't exist", arguments[0]);
            else try(final LogicalOperation ignored = new WriteAttributeLogicalOperation(attr.getName(), arguments[0])) {
                final String index = input.getOptionValue(IDX_OPT);
                final Format fmt;
                if (input.hasOption(DT_FMT_OPT))
                    fmt = new SimpleDateFormat(input.getOptionValue(DT_FMT_OPT));
                else if (input.hasOption(NUM_FMT_OPT))
                    fmt = new DecimalFormat(input.getOptionValue(NUM_FMT_OPT));
                else fmt = null;
                //delete row
                if (input.hasOption(DEL_OPT)) {
                    deleteTableRow(attr, index, output);
                    return;
                }
                //update or insert row
                else if (input.hasOption(ROW_OPT)) {
                    if (input.hasOption(INS_OPT))
                        insertTableRow(attr, index, input.getOptionProperties(ROW_OPT), fmt, output);
                    else updateTableRow(attr, index, input.getOptionProperties(ROW_OPT), fmt, output);
                    return;
                }
            }
        }
        throw invalidCommandFormat();
    }
}
