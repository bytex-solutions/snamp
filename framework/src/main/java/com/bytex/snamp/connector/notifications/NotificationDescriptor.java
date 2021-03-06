package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.FeatureDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.connector.notifications.NotificationSupport.*;

/**
 * Represents notification descriptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class NotificationDescriptor extends ImmutableDescriptor implements FeatureDescriptor<EventConfiguration> {
    /**
     * Gets name of the parameter in {@link EventConfiguration}
     * which describes the notification severity.
     */
    public static final String SEVERITY_PARAM = NotificationSupport.SEVERITY_FIELD;
    private static final long serialVersionUID = 6447489441284228878L;
    public static final NotificationDescriptor EMPTY_DESCRIPTOR = new NotificationDescriptor(ImmutableMap.<String, String>of());

    public NotificationDescriptor(final Map<String, ?> options){
        super(options);
    }

    public NotificationDescriptor(final EventConfiguration eventConfig) {
        this((Map<String, String>) eventConfig);
    }

    @Override
    public final NotificationDescriptor setFields(final Map<String, ?> values){
        if(values == null || values.isEmpty()) return this;
        final Map<String, Object> newFields = DescriptorUtils.toMap(this, false);
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
        for (final String fieldName : getFieldNames())
            entity.put(fieldName, Objects.toString(getFieldValue(fieldName)));
    }

    @Nonnull
    public final String getDescription(){
        return getDescription(this, "");
    }

    public final String getDescription(final String defval){
        return getDescription(this, defval);
    }

    public static String getDescription(final Descriptor metadata, final String defval){
        return DescriptorUtils.getField(metadata, DESCRIPTION_FIELD, Objects::toString).orElse(defval);
    }

    public static String getDescription(final MBeanNotificationInfo metadata){
        return getDescription(metadata.getDescriptor(), metadata.getDescription());
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

    public static OpenType<?> getUserDataType(final Descriptor metadata){
        return DescriptorUtils.getField(metadata, USER_DATA_TYPE, value -> (OpenType<?>)value).orElse(null);
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
    
    public static String getName(final MBeanNotificationInfo metadata) {
        return FeatureDescriptor.getName(metadata.getDescriptor())
                .orElseGet(() -> ArrayUtils.getFirst(metadata.getNotifTypes()).orElseThrow(AssertionError::new));
    }
}