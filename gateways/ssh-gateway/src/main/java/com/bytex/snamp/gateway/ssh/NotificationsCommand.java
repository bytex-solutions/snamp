package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.management.Notification;
import java.io.InputStream;
import java.io.PrintWriter;

import static com.bytex.snamp.internal.Utils.callAndWrapException;
import static com.bytex.snamp.io.IOUtils.hasMoreData;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class NotificationsCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "notifs";
    static final String COMMAND_USAGE = String.format(SshNotificationAccessor.LISTEN_COMMAND_PATTERN, "[-f <expression>]");
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
        final ExpressionBasedDescriptorFilter filter = input.hasOption(FILTER_OPTION.getOpt()) ?
                callAndWrapException(() -> new ExpressionBasedDescriptorFilter(input.getOptionValue(FILTER_OPTION.getOpt())), CommandException::new) :
                null;
        final InputStream consoleInput = getConsoleInputStream();
        final GatewayController controller = getGatewayController();
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
