package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.connectors.ConfigurationEntityRuntimeMetadata;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.connectors.notifications.NotificationSupport.*;

/**
 * Represents notification descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<EventConfiguration> {

    /**
     * The type of the configuration entity.
     *
     * @return The type of the configuration entity.
     */
    @Override
    public final Class<EventConfiguration> getEntityType() {
        return EventConfiguration.class;
    }



    /**
     * Fills the specified configuration entity.
     *
     * @param entity The configuration entity to fill.
     */
    @Override
    public final void fill(final EventConfiguration entity) {

    }

    public static String getDescription(final Descriptor metadata){
        return getField(metadata, DESCRIPTION_FIELD, String.class);
    }

    public static String getDescription(final MBeanNotificationInfo metadata){
        return metadata.getDescription();
    }

    public static String getNotificationCategory(final Descriptor metadata){
        return getField(metadata, NOTIFICATION_CATEGORY_FIELD, String.class);
    }

    public static String getNotificationCategory(final MBeanNotificationInfo metadata){
        final String[] notifTypes = metadata.getNotifTypes();
        return notifTypes.length > 0 ? notifTypes[0] : getNotificationCategory(metadata.getDescriptor());
    }

    public static Severity getSeverity(final Descriptor metadata) {
        Severity result = getField(metadata, SEVERITY_FIELD, Severity.class);
        return result != null ? result :
                Severity.resolve(getField(metadata, SEVERITY_FIELD, Integer.class, Severity.UNKNOWN.getLevel()));
    }

    public static Severity getSeverity(final MBeanNotificationInfo metadata){
        return getSeverity(metadata.getDescriptor());
    }
}