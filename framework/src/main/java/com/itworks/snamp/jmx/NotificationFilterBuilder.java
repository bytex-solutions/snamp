package com.itworks.snamp.jmx;

import com.google.common.base.Supplier;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * Represents builder for {@link javax.management.NotificationFilter}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationFilterBuilder implements Supplier<NotificationFilter> {
    /**
     * Represents notification filter that accepts any notification.
     */
    public static final NotificationFilter ANY = new NotificationFilter() {
        @Override
        public boolean isNotificationEnabled(final Notification notification) {
            return true;
        }
    };

    private NotificationFilter filter = null;

    public NotificationFilterBuilder(){
        this(null);
    }

    public NotificationFilterBuilder(final NotificationFilter filter){
        this.filter = filter;
    }

    public NotificationFilterBuilder allOf(final NotificationFilter... filters){
        this.filter = allOf(this.filter == null ? ANY : this.filter, filters);
        return this;
    }

    public NotificationFilterBuilder and(final NotificationFilter other){
        return allOf(new NotificationFilter[]{other});
    }

    public static NotificationFilter allOf(final NotificationFilter filter,
                                         final NotificationFilter... filters){
        return new NotificationFilter() {
            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                if(filter.isNotificationEnabled(notification)) {
                    for (final NotificationFilter filter : filters)
                        if (!filter.isNotificationEnabled(notification)) return false;
                    return true;
                }
                else return false;
            }
        };
    }

    public NotificationFilterBuilder anyOf(final NotificationFilter... filters){
        if(this.filter != null)
            this.filter = anyOf(this.filter, filters);
        return this;
    }

    public static NotificationFilter anyOf(final NotificationFilter filter,
                                           final NotificationFilter... filters){
        return new NotificationFilter() {
            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                if(filter.isNotificationEnabled(notification)) return true;
                else for(final NotificationFilter other: filters)
                    if(other.isNotificationEnabled(notification)) return true;
                return false;
            }
        };
    }

    /**
     * Constructs a new notification filter.
     * @return A new notification filter.
     */
    @Override
    public NotificationFilter get() {
        return build();
    }

    /**
     * Constructs a new notification filter.
     * @return A new notification filter.
     */
    public NotificationFilter build(){
        return filter == null ? ANY : filter;
    }
}
