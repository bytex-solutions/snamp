package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.notifications.NotificationBox;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import javax.management.*;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Listens event from the specified resource.
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "listen-events",
    description = "Listen and display events from the managed resources")
public final class ListenEventCommand extends OsgiCommandSupport implements SnampShellCommand {
    private static final class AllowedCategories extends HashSet<String> implements NotificationFilter{
        private AllowedCategories(final String... categories){
            super(Arrays.asList(categories));
        }

        @Override
        public boolean isNotificationEnabled(final Notification notification) {
            return contains(notification.getType());
        }
    }

    @Argument(index = 0, required = true, name = "resource", description = "The name of the resource to listen")
    private String resourceName = "";

    @Option(name = "-c", aliases = "--category", required = false, multiValued = true, description = "A set of categories of the events to listen")
    private String[] categories = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-s", aliases = {"--size"}, required = false, multiValued = false, description = "Maximum size of the input mailbox")
    private int capacity = 10;

    private static String[] getNames(final MBeanNotificationInfo[] attributes) {
        final String[] result = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++)
            result[i] = ArrayUtils.getFirst(attributes[i].getNotifTypes());
        return result;
    }

    private static void listenEvents(final NotificationSupport notifSupport,
                                     final String[] categories,
                                     final int capacity,
                                     final PrintStream output) throws ListenerNotFoundException, InterruptedException {
        if(notifSupport == null){
            output.println("Notifications are not supported");
            return;
        }
        output.println("Press CTRL+C to stop listening");
        final NotificationBox mailbox = new NotificationBox(capacity);
        notifSupport.addNotificationListener(mailbox, new AllowedCategories(categories), null);
        try{
            while (true){
                final Notification notif = mailbox.poll(10, TimeUnit.MILLISECONDS);
                if(notif == null) continue;
                output.println(notif.getType());
                output.println(new Date(notif.getTimeStamp()));
                output.println(notif.getSequenceNumber());
                output.println(notif.getMessage());
                output.println(notif.getUserData());
                output.println();
            }
        }
        finally {
            notifSupport.removeNotificationListener(mailbox);
        }
    }

    @Override
    protected Object doExecute() throws JMException, InterruptedException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(bundleContext, resourceName);
        try {
            String[] categories = this.categories;
            if(ArrayUtils.isNullOrEmpty(categories))
                categories = getNames(client.getMBeanInfo().getNotifications());
            listenEvents(client.queryObject(NotificationSupport.class), categories, capacity, session.getConsole());
            return null;
        } finally {
            client.release(bundleContext);
        }
    }
}
