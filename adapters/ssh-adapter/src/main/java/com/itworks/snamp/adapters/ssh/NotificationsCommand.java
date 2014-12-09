package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.StringAppender;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.mapping.RecordReader;
import com.itworks.snamp.mapping.RecordSet;
import com.itworks.snamp.mapping.RowSet;
import com.itworks.snamp.mapping.TypeLiterals;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.sshd.common.Session;

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
    static final String COMMAND_USAGE = "notifications [enable|disable] [OPTIONS]...";
    static final String COMMAND_DESC = "Notifications management";
    static final Options COMMAND_OPTIONS = new Options();
    private static String RES_OPT = "r";  //enable notifs for the specified resource only
    private static String EV_OPT = "n";   //enable notifs for the specified event name
    private static String Q_SIZE = "q";

    static {
        COMMAND_OPTIONS.addOption(new Option(RES_OPT, "resource", true, "The name of the connected resource"));
        COMMAND_OPTIONS.addOption(new Option(EV_OPT, "notification", true, "The ID of the notification"));
        COMMAND_OPTIONS.addOption(new Option(Q_SIZE, "queueSize", true, "Capacity of the queue of the notifications mailbox"));
    }

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
            StringAppender.init()
                    .appendln("%s available notifications:", resourceName)
                    .appendln(controller.getNotifications(resourceName))
                    .newLine()
                    .flush(output);
    }

    private static void enableNotifications(
            final NotificationManager manager,
            final String resourceName,
            final String notificationName,
            final PrintWriter output) {
        //enable all notifications
        if (resourceName.isEmpty() && notificationName.isEmpty()) {
            manager.enableAll();
            output.println("All notifications enabled");
            return;
        }
        if (resourceName.length() > 0) {
            manager.enableNotificationsByResource(resourceName);
            output.println(String.format("%s resource notifications enabled", resourceName));
        }
        if (notificationName.length() > 0) {
            manager.enableNotificationsByEvent(notificationName);
            output.println(String.format("%s notification enabled", notificationName));
        }
    }

    private static void disableNotifications(
            final NotificationManager manager,
            final String resourceName,
            final String notificationName,
            final PrintWriter output) {
        //disable all notifications
        if (resourceName.isEmpty() && notificationName.isEmpty()) {
            manager.enableAll();
            output.println("All notifications disabled");
            return;
        }
        if (resourceName.length() > 0) {
            manager.enableNotificationsByResource(resourceName);
            output.println(String.format("%s resource notifications disabled", resourceName));
        }
        if (notificationName.length() > 0) {
            manager.enableNotificationsByEvent(notificationName);
            output.println(String.format("%s notification disabled", notificationName));
        }
    }

    private static void notificationsStatus(final NotificationManager manager,
                                            final PrintWriter output) throws IOException {
            StringAppender.init()
                    .appendln("Disabled notifications from resources:")
                    .appendln(manager.getDisabledResources())
                    .newLine()
                    .appendln("Disabled notifications:")
                    .appendln(manager.getDisabledNotifs())
                    .flush(output);
    }

    private static void printAttachment(final RecordSet<String, ?> attachment,
                                        final StringAppender output){
        attachment.sequential().forEach(new RecordReader<String, Object, ExceptionPlaceholder>() {
            @Override
            public void read(final String name, final Object value) {
                output.appendln("%s = %s", name, value);
            }
        });
    }

    private static void printAttachment(final RowSet<?> attachment,
                                        final StringAppender output){
        attachment.sequential().forEach(new RecordReader<Integer, RecordSet<String, ?>, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final RecordSet<String, ?> value) {
                output.appendln("Entry #%s", index);
                value.sequential().forEach(new RecordReader<String, Object, ExceptionPlaceholder>() {
                    @Override
                    public void read(final String name, final Object value) {
                        output.appendln("\t%s = %s", name, value);
                    }
                });
                output.newLine();
            }
        });
    }

    private static void printAttachment(Object attachment,
                                        final StringAppender output){
        if(attachment instanceof ManagedEntityValue<?>)
            attachment = ((ManagedEntityValue<?>)attachment).rawValue;
        if(TypeLiterals.isInstance(attachment, TypeLiterals.NAMED_RECORD_SET))
            printAttachment(TypeLiterals.cast(attachment, TypeLiterals.NAMED_RECORD_SET), output);
        else if(TypeLiterals.isInstance(attachment, TypeLiterals.ROW_SET))
            printAttachment(TypeLiterals.cast(attachment, TypeLiterals.ROW_SET), output);
        else
            output.appendln(Objects.toString(attachment));
    }

    private void pendingNotifications(final NotificationManager manager,
                                      final int capacity,
                                      final PrintWriter output) throws CommandException {
        final SshNotificationBox mailbox = new SshNotificationBox(capacity, manager);
        final AdapterController controller = getAdapterController();
        final InputStream reader = getConsoleInputStream();
        output.println("Press any key to abort listening session");
        final long listenerID = controller.addNotificationListener(mailbox);
        try {
            while (reader.available() <= 0) {
                final Notification notif = mailbox.poll();
                if (notif == null) { //mailbox is empty
                    Thread.sleep(1);
                    continue;
                }
                final SshNotificationBox.AdditionalNotificationInfo info = SshNotificationBox.getAdditionalInfo(notif);
                final StringAppender appender = StringAppender.init();
                if (info != null) {
                    appender.appendln("Event %s from resource %s", info.eventName, info.resourceName);
                }
                appender
                        .appendln("Sequence #%s", notif.getSequenceNumber())
                        .appendln("Timestamp: %s", notif.getTimeStamp())
                        .appendln("Severity %s", notif.getSeverity())
                        .appendln("Correlation ID %s", notif.getCorrelationID())
                        .appendln(notif.getMessage());
                final Object attachment = notif.getAttachment();
                if(attachment != null)
                    printAttachment(attachment, appender.appendln("== Additional notification payload =="));
                appender.newLine().flush(output);
            }
        } catch (final IOException | InterruptedException e) {
            throw new CommandException(e);
        } finally {
            controller.removeNotificationListener(listenerID);
        }
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final Session serverSession = getSession();
        if (serverSession == null) throw new CommandException("Session is not available.");
        final NotificationManager manager = NotificationManager.getNotificationManager(serverSession);
        if (manager == null) throw new CommandException("Notifications managed is not allowed");
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
            case 1:
                final String resourceName = input.getOptionValue(RES_OPT, "");
                final String eventName = input.getOptionValue(EV_OPT, "");
                switch (arguments[0]) {
                    case "enable":
                        enableNotifications(manager, resourceName, eventName, output);
                        return;
                    case "disable":
                        disableNotifications(manager, resourceName, eventName, output);
                        return;
                    case "status":
                        try {
                            notificationsStatus(manager, output);
                        } catch (final IOException e) {
                            throw new CommandException(e);
                        }
                        return;
                    case "start":
                        pendingNotifications(manager,
                                input.hasOption(Q_SIZE) ? Integer.parseInt(input.getOptionValue(Q_SIZE)) : 10,
                                output);
                        return;
                }
            default:
                throw invalidCommandFormat();
        }
    }
}
