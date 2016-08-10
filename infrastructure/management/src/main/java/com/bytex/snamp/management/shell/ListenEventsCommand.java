package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import javax.management.*;
import java.io.PrintStream;
import java.time.Duration;
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
public final class ListenEventsCommand extends OsgiCommandSupport implements SnampShellCommand {
    private static final class AllowedCategories extends HashSet<String> implements NotificationFilter{
        private static final long serialVersionUID = -4310589461921201562L;

        private AllowedCategories(final String... categories){
            super(Arrays.asList(categories));
        }

        @Override
        public boolean isNotificationEnabled(final Notification notification) {
            return contains(notification.getType());
        }
    }

    @Argument(index = 0, required = true, name = "resource", description = "The name of the resource to listen")
    @SpecialUse
    private String resourceName = "";

    @Option(name = "-c", aliases = "--category", required = false, multiValued = true, description = "A set of categories of the events to listen")
    @SpecialUse
    private String[] categories = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-s", aliases = {"--size"}, required = false, multiValued = false, description = "Maximum size of the input mailbox")
    @SpecialUse
    private int capacity = 10;

    @Option(name = "-t", aliases = "--period", multiValued = false, required = false, description = "Period of listening events, in millis")
    @SpecialUse
    private int listenPeriodMillis = 10;

    private static String[] getNames(final MBeanNotificationInfo[] attributes) {
        final String[] result = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++)
            result[i] = ArrayUtils.getFirst(attributes[i].getNotifTypes());
        return result;
    }

    private static void listenEvents(final NotificationSupport notifSupport,
                                     final String[] categories,
                                     final int capacity,
                                     final Duration timeout,
                                     final PrintStream output) throws ListenerNotFoundException, InterruptedException {
        if(notifSupport == null){
            output.println("Notifications are not supported");
            return;
        }
        output.println("Press CTRL+C to stop listening");
        final Mailbox mailbox = MailboxFactory.newFixedSizeMailbox(capacity);
        notifSupport.addNotificationListener(mailbox, new AllowedCategories(categories), null);
        try{
            while (true){
                final Notification notif = mailbox.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);//InterruptedException when CTRL+C was pressed
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
            listenEvents(client.queryObject(NotificationSupport.class),
                    categories,
                    capacity,
                    Duration.ofMillis(listenPeriodMillis),
                    session.getConsole());
            return null;
        } finally {
            client.release(bundleContext);
        }
    }
}
