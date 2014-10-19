package com.itworks.snamp.adapters.ssh;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.sshd.common.Session;

import java.io.PrintWriter;

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

    static {
        COMMAND_OPTIONS.addOption(new Option(RES_OPT, "resource", true, "The name of the connected resource"));
        COMMAND_OPTIONS.addOption(new Option(EV_OPT, "notification", true, "The ID of the notification"));
    }

    NotificationsCommand(final CommandExecutionContext context){
        super(context);
    }

    @Override
    protected Options getCommandOptions() {
        return COMMAND_OPTIONS;
    }

    private static void displayNotifications(final AdapterController controller,
                                             final PrintWriter output){
        for(final String resourceName: controller.getConnectedResources()){
            output.println(String.format("%s available notifications:", resourceName));
            for(final String eventName: controller.getNotifications(resourceName))
                output.println(eventName);
            output.println();
        }
    }

    private static void enableNotifications(
                                        final NotificationManager manager,
                                    final String resourceName,
                                     final String notificationName,
                                     final PrintWriter output){
        //enable all notifications
        if(resourceName.isEmpty() && notificationName.isEmpty()){
            manager.enableAll();
            output.println("All notifications enabled");
            return;
        }
        if(resourceName.length() > 0) {
            manager.enableNotificationsByResource(resourceName);
            output.println(String.format("%s resource notifications enabled", resourceName));
        }
        if(notificationName.length() > 0) {
            manager.enableNotificationsByEvent(notificationName);
            output.println(String.format("%s notification enabled", notificationName));
        }
    }

    private static void disableNotifications(
            final NotificationManager manager,
            final AdapterController controller,
            final String resourceName,
            final String notificationName,
            final PrintWriter output){
        //disable all notifications
        if(resourceName.isEmpty() && notificationName.isEmpty()){
            manager.enableAll();
            output.println("All notifications disabled");
            return;
        }
        if(resourceName.length() > 0) {
            manager.enableNotificationsByResource(resourceName);
            output.println(String.format("%s resource notifications disabled", resourceName));
        }
        if(notificationName.length() > 0) {
            manager.enableNotificationsByEvent(notificationName);
            output.println(String.format("%s notification disabled", notificationName));
        }
    }

    private static void notificationsStatus(final NotificationManager manager,
                                            final PrintWriter output) {
        output.println("Disabled notifications from resources:");
        for (final String resourceName : manager.getDisabledResources())
            output.println(resourceName);
        output.println();
        output.println("Disabled notifications:");
        for (final String eventID : manager.getDisabledNotifs())
            output.println(eventID);
    }

    @Override
    protected void doCommand(final CommandLine input, final PrintWriter output) throws CommandException {
        final Session serverSession = getSession();
        if(serverSession == null) throw new CommandException("Session is not available.");
        final NotificationManager manager = NotificationManager.getNotificationManager(serverSession);
        if(manager == null) throw new CommandException("Notifications managed is not allowed");
        final String[] arguments = input.getArgs();
        switch (arguments.length){
            case 0:
                displayNotifications(getAdapterController(), output);
            return;
            case 1:
                final String resourceName = input.getOptionValue(RES_OPT, "");
                final String eventName = input.getOptionValue(EV_OPT, "");
                switch (arguments[0]){
                    case "enable":
                        enableNotifications(manager, resourceName, eventName, output);
                    return;
                    case "disable":
                        disableNotifications(manager, getAdapterController(), resourceName, eventName, output);
                    return;
                    case "status":
                        notificationsStatus(manager, output);
                    return;
                }
            default: throw invalidCommandFormat();
        }
    }
}
