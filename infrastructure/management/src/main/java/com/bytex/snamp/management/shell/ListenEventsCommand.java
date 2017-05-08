package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.console.Session;

import javax.annotation.Nonnull;
import javax.management.*;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Listens event from the specified resource.
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "listen-events",
    description = "Listen and display events from the managed resources")
public final class ListenEventsCommand extends SnampShellCommand {
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
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String resourceName = "";

    @Option(name = "-c", aliases = "--category", required = false, multiValued = true, description = "A set of categories of the events to listen")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] categories = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-s", aliases = {"--size"}, required = false, multiValued = false, description = "Maximum size of the input mailbox")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int capacity = 10;

    @Option(name = "-t", aliases = "--period", multiValued = false, required = false, description = "Period of listening events, in millis")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int listenPeriodMillis = 10;

    @Reference
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private Session session;

    private static String[] getNames(final MBeanNotificationInfo[] attributes) {
        final String[] result = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            final Optional<String> name = ArrayUtils.getFirst(attributes[i].getNotifTypes());
            if (name.isPresent())
                result[i] = name.get();
        }
        return result;
    }

    private static void listenEvents(@Nonnull final NotificationSupport notifSupport,
                                     final String[] categories,
                                     final int capacity,
                                     final Duration timeout,
                                     final PrintStream output) throws ListenerNotFoundException, InterruptedException {
        output.println("Press CTRL+C to stop listening");
        final Mailbox mailbox = MailboxFactory.newFixedSizeMailbox(capacity);
        notifSupport.addNotificationListener(mailbox, new AllowedCategories(categories), null);
        try {
            while (!Thread.interrupted()) {
                final Notification notif = mailbox.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);//InterruptedException when CTRL+C was pressed
                if (notif == null) continue;
                output.println(notif.getType());
                output.println(new Date(notif.getTimeStamp()));
                output.println(notif.getSequenceNumber());
                output.println(notif.getMessage());
                output.println(notif.getUserData());
                output.println();
            }
        } finally {
            notifSupport.removeNotificationListener(mailbox);
        }
    }

    @Override
    public void execute(final PrintWriter output) throws JMException, InterruptedException {
        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName)
                .orElseThrow(() -> new InstanceNotFoundException(String.format("Resource %s doesn't exist", resourceName)))) {
            String[] categories = this.categories;
            if (ArrayUtils.isNullOrEmpty(categories))
                categories = getNames(client.getMBeanInfo().getNotifications());
            final Optional<NotificationSupport> support = client.queryObject(NotificationSupport.class);
            if (support.isPresent())
                listenEvents(support.get(),
                        categories,
                        capacity,
                        Duration.ofMillis(listenPeriodMillis),
                        session.getConsole());
            else
                session.getConsole().println("Notifications are not supported");
        }
    }
}
