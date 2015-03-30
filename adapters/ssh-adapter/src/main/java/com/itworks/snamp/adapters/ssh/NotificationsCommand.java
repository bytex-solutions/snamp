package com.itworks.snamp.adapters.ssh;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.StringAppender;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.management.Notification;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationsCommand extends AbstractManagementShellCommand {
    static final String COMMAND_NAME = "notifications";
    static final String COMMAND_USAGE = "notifications [listen [-r <resources> | -n <notifs>]]";
    static final String COMMAND_DESC = "Notifications management";
    private static final Option NAME_OPTION = new Option("n", "notification", true, "The ID of the notification");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(RESOURCE_OPTION)
            .addOption(NAME_OPTION);

    NotificationsCommand(final CommandExecutionContext context) {
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void displayNotifications(final AdapterController controller,
                                             final PrintWriter output) throws IOException{
        for (final String resourceName : controller.getConnectedResources())
            new StringAppender()
                    .appendln("%s available notifications:", resourceName)
                    .appendln(controller.getNotifications(resourceName))
                    .newLine()
                    .flush(output);
    }

    private void print(final Notification notif, final PrintWriter output){
        getAdapterController().processNotification(Objects.toString(notif.getSource()), notif.getType(), new SafeConsumer<SshNotificationMapping>() {
            @Override
            public void accept(final SshNotificationMapping metadata) {
                metadata.print(notif, output);
            }
        });
    }

    private void pendingAllNotifications(final InputStream reader,
                                         final PrintWriter output) throws CommandException{
        try {
            while (reader.available() <= 0) {
                final Notification notif = getAdapterController().poll();
                if(notif != null)
                    print(notif, output);
                else Thread.sleep(1);
            }
        }
        catch (final IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    private void pendingEnabledNotifications(final ImmutableSet<String> enabledNotifs,
                                      final InputStream reader,
                                      final PrintWriter output) throws CommandException{
        try {
            while (reader.available() <= 0) {
                final Notification notif = getAdapterController().poll();
                if(notif != null) {
                    if (enabledNotifs.contains(notif.getType()))
                        print(notif, output);
                }
                else Thread.sleep(1);
            }
        }
        catch (final IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    private void pendingEnabledResources(final ImmutableSet<String> resources,
                                             final InputStream reader,
                                             final PrintWriter output) throws CommandException{
        try {
            while (reader.available() <= 0) {
                for(final String resourceName: resources){
                    final Notification notif = getAdapterController().poll(resourceName);
                    if(notif != null) print(notif, output);
                }
                Thread.sleep(1);
            }
        }
        catch (final IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    private void pendingNotifications(final ImmutableSet<String> resources,
                                      final ImmutableSet<String> notifications,
                                      final InputStream reader,
                                      final PrintWriter output) throws CommandException {
        try {
            while (reader.available() <= 0) {
                for(final String resourceName: resources){
                    final Notification notif = getAdapterController().poll(resourceName);
                    if(notif != null && notifications.contains(notif.getType())) print(notif, output);
                }
                Thread.sleep(1);
            }
        }
        catch (final IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    private void pendingNotifications(final ImmutableSet<String> resources,
                                      final ImmutableSet<String> notifications,
                                      final PrintWriter output) throws CommandException {
        final InputStream reader = getConsoleInputStream();
        output.println("Press any key to abort listening session");
        if(resources.isEmpty())
            if(notifications.isEmpty()) pendingAllNotifications(reader, output);
            else pendingEnabledNotifications(notifications, reader, output);
        else if(notifications.isEmpty())
            pendingEnabledResources(resources, reader, output);
        else pendingNotifications(resources, notifications, reader, output);
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final String[] arguments = input.getArgs();
        switch (arguments.length) {
            case 0:
                try {
                    displayNotifications(getAdapterController(), output);
                }
                catch (final IOException e){
                    throw new CommandException(e);
                }
                return;
            default:
                final String[] resources = input.getOptionValues(RESOURCE_OPTION.getOpt());
                final String[] eventNames = input.getOptionValues(NAME_OPTION.getOpt());
                switch (arguments[0]) {
                    case "listen":
                        pendingNotifications(
                                ImmutableSet.copyOf(resources),
                                ImmutableSet.copyOf(eventNames),
                                output);
                    default: throw invalidCommandFormat();
                }
        }
    }
}
