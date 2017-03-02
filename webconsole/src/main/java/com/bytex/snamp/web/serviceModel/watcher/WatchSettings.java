package com.bytex.snamp.web.serviceModel.watcher;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.management.Notification;
import javax.management.NotificationFilter;
import java.util.Objects;

/**
 * Represents notification settings.
 */
@JsonTypeName("notificationSettings")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public final class WatchSettings implements NotificationFilter {
    private boolean trackHealthStatus;

    @JsonProperty
    public boolean isTrackHealthStatus() {
        return trackHealthStatus;
    }

    public void setTrackHealthStatus(boolean trackHealthStatus) {
        this.trackHealthStatus = trackHealthStatus;
    }

    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return false;
    }
}
