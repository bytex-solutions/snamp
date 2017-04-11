package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents notification settings.
 */
@JsonTypeName("notificationSettings")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public final class NotificationSettings {
    private Severity severity = Severity.WARNING;
    private final Set<String> notificationTypes = new HashSet<>();

    boolean isNotificationEnabled(final Notification notification, final Severity severity) {
        return this.severity.isAllowed(severity) && (notificationTypes.isEmpty() || notificationTypes.contains(notification.getType()));
    }

    @JsonProperty("notificationTypes")
    public Set<String> getNotificationTypes(){
        return notificationTypes;
    }

    public void setNotificationTypes(final Set<String> value){
        notificationTypes.clear();
        notificationTypes.addAll(value);
    }

    @JsonProperty("severity")
    @JsonSerialize(using = SeveritySerializer.class)
    @JsonDeserialize(using = SeverityDeserializer.class)
    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(final Severity value){
        severity = Objects.requireNonNull(value);
    }
}
