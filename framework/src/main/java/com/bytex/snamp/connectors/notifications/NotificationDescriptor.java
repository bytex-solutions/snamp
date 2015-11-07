package com.bytex.snamp.connectors.notifications;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.bytex.snamp.configuration.ConfigParameters;
import com.bytex.snamp.connectors.ConfigurationEntityRuntimeMetadata;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.connectors.notifications.NotificationSupport.*;
import static com.bytex.snamp.jmx.CompositeDataUtils.fillMap;

/**
 * Represents notification descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationDescriptor extends ImmutableDescriptor implements ConfigurationEntityRuntimeMetadata<EventConfiguration> {
    /**
     * Gets name of the parameter in {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration}
     * which describes the notification severity.
     */
    public static final String SEVERITY_PARAM = NotificationSupport.SEVERITY_FIELD;
    private static final long serialVersionUID = 6447489441284228878L;
    public static final NotificationDescriptor EMPTY_DESCRIPTOR = new NotificationDescriptor(ImmutableMap.<String, String>of());

    private NotificationDescriptor(final Map<String, ?> fields){
        super(fields);
    }

    public NotificationDescriptor(final EventConfiguration eventConfig,
                                  final NotificationSubscriptionModel subscriptionModel){
        this(eventConfig.getCategory(),
                subscriptionModel,
                new ConfigParameters(eventConfig));
    }

    public NotificationDescriptor(final String category,
                                  final NotificationSubscriptionModel subscriptionModel,
                                  final CompositeData options){
        this(getFields(category, subscriptionModel, options));
    }

    private static Map<String, ?> getFields(final String category,
                                            final NotificationSubscriptionModel subscriptionModel,
                                            final CompositeData options){
        final Map<String, Object> fields = Maps.newHashMapWithExpectedSize(options.values().size() + 3);
        fields.put(NOTIFICATION_CATEGORY_FIELD, category);
        fields.put(SEVERITY_FIELD, getSeverity(options));
        fields.put(SUBSCRIPTION_MODEL_FIELD, subscriptionModel);
        fillMap(options, fields);
        return fields;
    }

    @Override
    public final NotificationDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final String[] fields = getFieldNames();
        final Map<String, Object> newFields = Maps.newHashMapWithExpectedSize(fields.length + values.size());
        for(final String name: fields)
            newFields.put(name, getFieldValue(name));
        newFields.putAll(values);
        return new NotificationDescriptor(newFields);
    }

    @Override
    public final NotificationDescriptor setFields(final Descriptor values){
        return setFields(DescriptorUtils.toMap(values));
    }

    public final NotificationDescriptor setUserDataType(final OpenType<?> type){
        return type != null ? setFields(ImmutableMap.of(USER_DATA_TYPE, type)) : this;
    }

    private static Severity getSeverity(final CompositeData parameters){
        if(parameters.containsKey(SEVERITY_PARAM)){
            final Object result = parameters.get(SEVERITY_PARAM);
            if(result instanceof String)
                return Severity.resolve((String)result);
            else if(result instanceof Integer)
                return Severity.resolve((Integer)result);
            else if(result instanceof Severity)
                return (Severity)result;
        }
        return Severity.UNKNOWN;
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
            switch (fieldName) {
                default:
                    entity.getParameters().put(fieldName, Objects.toString(getFieldValue(fieldName)));
                case NOTIFICATION_CATEGORY_FIELD:
            }
    }

    public final String getDescription(){
        return getDescription(this);
    }

    public final String getDescription(final String defval){
        final String result = getDescription();
        return Strings.isNullOrEmpty(result) ? defval : result;
    }

    public static String getDescription(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, DESCRIPTION_FIELD, String.class);
    }

    public static String getDescription(final MBeanNotificationInfo metadata){
        return metadata.getDescription();
    }

    public static String getNotificationCategory(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, NOTIFICATION_CATEGORY_FIELD, String.class);
    }

    public final String getNotificationCategory(){
        return getNotificationCategory(this);
    }

    public static String getNotificationCategory(final MBeanNotificationInfo metadata){
        return getNotificationCategory(metadata.getDescriptor());
    }

    public static Severity getSeverity(final Descriptor metadata) {
        final Object result = metadata.getFieldValue(SEVERITY_FIELD);
        if(result instanceof Severity)
            return (Severity)result;
        else if(result instanceof Number)
            return Severity.resolve(((Number)result).intValue());
        else if(result instanceof String)
            return Severity.resolve((String)result);
        else return Severity.UNKNOWN;
    }

    public final Severity getSeverity(){
        return getSeverity(this);
    }

    public static Severity getSeverity(final MBeanNotificationInfo metadata){
        return getSeverity(metadata.getDescriptor());
    }

    public static NotificationSubscriptionModel getSubscriptionModel(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, SUBSCRIPTION_MODEL_FIELD, NotificationSubscriptionModel.class, NotificationSubscriptionModel.MULTICAST);
    }

    public static NotificationSubscriptionModel getSubscriptionModel(final MBeanNotificationInfo metadata){
        return getSubscriptionModel(metadata.getDescriptor());
    }

    public final NotificationSubscriptionModel getSubscriptionModel(){
        return getSubscriptionModel(this);
    }

    public static OpenType<?> getUserDataType(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, USER_DATA_TYPE, OpenType.class);
    }

    public static OpenType<?> getUserDataType(final MBeanNotificationInfo metadata){
        return getUserDataType(metadata.getDescriptor());
    }

    public final OpenType<?> getUserDataType(){
        return getUserDataType(this);
    }

    /**
     * Determines whether the field with the specified name is defined in this descriptor.
     * @param fieldName The name of the field to check.
     * @return {@literal true}, if the specified field exists in this descriptor; otherwise, {@literal false}.
     */
    public final boolean hasField(final String fieldName){
        return DescriptorUtils.hasField(this, fieldName);
    }

    public final  <T> T getField(final String fieldName, final Class<T> fieldType){
        return DescriptorUtils.getField(this, fieldName, fieldType);
    }

    /**
     * Indicating that the notification with this descriptor will be added automatically by connector itself.
     * This can be happened because connector is in Smart mode.
     * @return {@literal true}, if the attribute with this notification will be added automatically by connector itself; otherwise, {@literal false}.
     */
    public final boolean isAutomaticallyAdded(){
        return hasField(AUTOMATICALLY_ADDED_FIELD);
    }
}