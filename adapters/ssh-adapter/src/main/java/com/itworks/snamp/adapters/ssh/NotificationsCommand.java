package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Notification;
import java.io.InputStream;
import java.io.PrintWriter;
import static com.itworks.snamp.io.IOUtils.hasMoreData;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationsCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "notifs";
    static final String COMMAND_USAGE = "notifs [-f expression]";
    static final String COMMAND_DESC = "Notifications management";
    private static final Option FILTER_OPTION = new Option("f", "filter", true, "Notification filter");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(FILTER_OPTION);

    NotificationsCommand(final CommandExecutionContext context) {
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }


    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final ExpressionBasedDescriptorFilter filter;
        if(input.hasOption(FILTER_OPTION.getOpt()))
            try {
                filter = new ExpressionBasedDescriptorFilter(input.getOptionValue(FILTER_OPTION.getOpt()));
            }
            catch (final InvalidSyntaxException e){
                throw new CommandException(e);
            }
        else filter = null;
        final InputStream consoleInput = getConsoleInputStream();
        final AdapterController controller = getAdapterController();
        while (!hasMoreData(consoleInput)) {
            final Notification notif = controller.poll(filter);
            if(notif == null) try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                throw new CommandException(e);
            }
            else controller.print(notif, output);
        }

    }
}
