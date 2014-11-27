package com.itworks.snamp.adapters.jmx;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Date;
import java.util.Map;

import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.SEVERITY_PARAM;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxNotificationMapping implements JmxFeature<MBeanNotificationInfo> {
    private final String category;
    private final String description;
    private final Integer jmxSeverity;
    private final ManagedEntityType attachmentType;
    private final Map<String, String> options;

    JmxNotificationMapping(final NotificationMetadata meta,
                           final String category) {
        this.options = meta;
        this.category = category;
        final String descr = meta.getDescription(null);
        this.description = descr == null || descr.isEmpty() ?
                String.format("Description stub for %s event", category) :
                descr;
        //identify the severity
        jmxSeverity = getSeverity(meta);
        this.attachmentType = meta.getAttachmentType();
    }

    private static Integer getSeverity(final Map<String, String> meta) {
        if (meta.containsKey(SEVERITY_PARAM))
            switch (meta.get(SEVERITY_PARAM).toLowerCase()) {
                case "1": //jmx severity level
                case "panic":
                    return 1;
                case "2": //jmx severity level
                case "alert":
                    return 2;
                case "3": //jmx severity level
                case "critical":
                    return 3;
                case "4": //jmx severity level
                case "error":
                    return 4;
                case "5": //jmx severity level
                case "warning":
                    return 5;
                case "6": //jmx severity level
                case "notice":
                    return 6;
                case "7": //jmx severity level
                case "info":
                    return 7;
                case "8": //jmx severity level
                case "debug":
                    return 8;
                case "0": //jmx severity level
                default:
                    return 0;
            }
        else return 0;
    }

    String getCategory(){
        return category;
    }

    Object convertAttachment(final Object attachment) throws OpenDataException {
        if(attachment == null)
            return null;
        else if(attachment instanceof Number ||
                attachment instanceof String ||
                attachment instanceof Boolean ||
                attachment instanceof Date ||
                TypeLiterals.isInstance(attachment, JmxTypeSystem.COMPOSITE_DATA) ||
                TypeLiterals.isInstance(attachment, JmxTypeSystem.TABULAR_DATA))
            return attachment;
        else if(attachment instanceof ManagedEntityValue<?>)
            return JmxTypeSystem.getValue((ManagedEntityValue<?>) attachment, options);
        else if(attachmentType != null)
            return JmxTypeSystem.getValue(new ManagedEntityValue<>(attachment, attachmentType), options);
        else return null;
    }

    private OpenType<?> getAttachmentOpenType(){
        OpenType<?> userDataType;
        try {
            userDataType = attachmentType != null ? JmxTypeSystem.getType(attachmentType, options) : null;
        }
        catch (final OpenDataException ignored) {
            userDataType = null;
        }
        return userDataType;
    }

    ManagedEntityType getAttachmentType(){
        return attachmentType;
    }

    @Override
    public MBeanNotificationInfo createFeature(final String featureName) {
        final String JMX_SEVERITY = "severity", JMX_OPEN_TYPE = "openType";
        final OpenType<?> attachmentType = getAttachmentOpenType();
        final Map<String, Object> descriptor = attachmentType != null ?
                ImmutableMap.<String, Object>of(JMX_SEVERITY, jmxSeverity, JMX_OPEN_TYPE, attachmentType):
                ImmutableMap.<String, Object>of(JMX_SEVERITY, jmxSeverity);
        return new MBeanNotificationInfo(new String[]{category},
                featureName,
                description,
                new ImmutableDescriptor(descriptor));
    }
}
