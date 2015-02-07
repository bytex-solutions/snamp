package com.itworks.snamp.connectors.notifications;

import com.google.common.collect.Maps;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.ConfigurationEntityRuntimeMetadata;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.connectors.notifications.NotificationSupport.*;
import static com.itworks.snamp.jmx.CompositeDataUtils.fillMap;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;

/**
 * Represents notification descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<EventConfiguration> {
    public static final String SEVERITY_PARAM = NotificationSupport.SEVERITY_FIELD;

    public NotificationDescriptor(final EventConfiguration eventConfig,
                                  final NotificationSubscriptionModel subscriptionModel){
        this(eventConfig.getCategory(),
                getSeverity(eventConfig.getParameters()),
                subscriptionModel,
                new ConfigParameters(eventConfig));
    }

    public NotificationDescriptor(final String category,
                                  final Severity severity,
                                  final NotificationSubscriptionModel subscriptionModel,
                                  final CompositeData options){
        super(getFields(category, severity, subscriptionModel, options));
    }

    private static Map<String, ?> getFields(final String category,
                                            final Severity severity,
                                            final NotificationSubscriptionModel subscriptionModel,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 3);
        fields.put(NOTIFICATION_CATEGORY_FIELD, category);
        fields.put(SEVERITY_FIELD, severity);
        fields.put(SUBSCRIPTION_MODEL_FIELD, subscriptionModel);
        fillMap(options, fields);
        return fields;
    }

    private static Severity getSeverity(final Map<String, String> parameters){
        return parameters.containsKey(SEVERITY_PARAM) ?
                Severity.resolve(parameters.get(SEVERITY_PARAM)):
                Severity.UNKNOWN;
    }

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
        entity.setCategory(getNotificationCategory());
        for (final String fieldName : getFieldNames())
            entity.getParameters().put(fieldName, Objects.toString(getFieldValue(fieldName)));
    }

    public final String getDescription(){
        return getDescription(this);
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

    public final String getNotificationCategory(){
        return getNotificationCategory(this);
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

    public final Severity getSeverity(){
        return getSeverity(this);
    }

    public static Severity getSeverity(final MBeanNotificationInfo metadata){
        return getSeverity(metadata.getDescriptor());
    }

    public static NotificationSubscriptionModel getSubscriptionModel(final Descriptor metadata){
        return getField(metadata, SUBSCRIPTION_MODEL_FIELD, NotificationSubscriptionModel.class, NotificationSubscriptionModel.MULTICAST);
    }

    public static NotificationSubscriptionModel getSubscriptionModel(final MBeanNotificationInfo metadata){
        return getSubscriptionModel(metadata.getDescriptor());
    }

    public final NotificationSubscriptionModel getSubscriptionModel(){
        return getSubscriptionModel(this);
    }

    public static OpenType<?> getUserDataType(final Descriptor metadata){
        return getField(metadata, USER_DATA_TYPE, OpenType.class);
    }

    public static OpenType<?> getUserDataType(final MBeanNotificationInfo metadata){
        return getUserDataType(metadata.getDescriptor());
    }
}