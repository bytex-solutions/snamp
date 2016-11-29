package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import groovy.lang.Closure;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.*;
import java.util.function.Predicate;

/**
 * Represents analyzer of input stream of notifications received from managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceNotificationsAnalyzer implements ResourceFeaturesAnalyzer, NotificationListener {
    private interface NotificationStatement extends FeatureStatement{

    }

    public static class FilterAndProcessNotificationStatement implements Predicate<Notification> {
        private final Predicate<Notification> condition;
        private final WriteOnceRef<NotificationListener> handler;

        protected FilterAndProcessNotificationStatement(final Predicate<Notification> condition){
            this.condition = Objects.requireNonNull(condition);
            this.handler = new WriteOnceRef<>(null);
        }

        @Override
        public final boolean test(final Notification notif){
            return condition.test(notif);
        }

        public final void then(final NotificationListener listener){
            handler.set(listener);
        }

        @SpecialUse
        public final void then(final Closure<?> listener){
            then(Closures.toNotificationHandler(listener));
        }

        private void onSuccess(final MBeanNotificationInfo metadata,
                               final Notification notif){
            final NotificationListener listener = this.handler.get();
            if(listener != null)
                listener.handleNotification(new NotificationEvent(metadata, notif));
        }
    }

    public static class NotificationSelectStatement extends AbstractSelectStatement implements NotificationStatement{
        private final List<FilterAndProcessNotificationStatement> handlers;

        protected NotificationSelectStatement(final String expression) throws InvalidSyntaxException {
            super(expression);
            handlers = new LinkedList<>();
        }

        protected FilterAndProcessNotificationStatement createStatement(final Predicate<Notification> pred){
            return new FilterAndProcessNotificationStatement(pred);
        }

        public final FilterAndProcessNotificationStatement when(final Predicate<Notification> condition){
            final FilterAndProcessNotificationStatement result = createStatement(condition);
            handlers.add(result);
            return result;
        }

        @SpecialUse
        public final FilterAndProcessNotificationStatement when(final Closure<Boolean> condition){
            return when(Closures.toPredicate(condition));
        }

        private void process(final MBeanNotificationInfo metadata,
                             final Notification notif){
            handlers.stream()
                    .filter(statement -> statement.test(notif))
                    .forEach(statement -> statement.onSuccess(metadata, notif));
        }
    }

    private final Set<NotificationSelectStatement> selectionStatements = new LinkedHashSet<>(10);

    protected NotificationSelectStatement createSelector(final String expression) throws InvalidSyntaxException {
        return new NotificationSelectStatement(expression);
    }

    /**
     * Creates a new selection statement using RFC 1960-based filter that will be applied to feature metadata (and configuration properties).
     *
     * @param expression An expression used to select resource features. Cannot be {@literal null} or empty.
     * @return Selection statement.
     * @throws InvalidSyntaxException Incorrect expression.
     */
    @Override
    public final NotificationSelectStatement select(final String expression) throws InvalidSyntaxException {
        final NotificationSelectStatement result = createSelector(expression);
        selectionStatements.add(result);
        return result;
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public final void handleNotification(final NotificationEvent event) {
        handleNotification(event.getSource(), event.getNotification());
    }

    public final void handleNotification(final MBeanNotificationInfo metadata,
                                         final Notification notif){
        selectionStatements.stream()
                .filter(stmt -> stmt.match(metadata))
                .forEach(stmt -> stmt.process(metadata, notif));
    }
}
