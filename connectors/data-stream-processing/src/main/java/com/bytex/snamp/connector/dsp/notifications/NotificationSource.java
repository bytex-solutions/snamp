package com.bytex.snamp.connector.dsp.notifications;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes the source of the notification emitted by remote component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class NotificationSource implements Serializable {
    private static final long serialVersionUID = -3632338167739067932L;

    private final String componentName;
    private final String instanceName;

    public NotificationSource(final String componentName, final String instanceName){
        this.componentName = Objects.requireNonNull(componentName);
        this.instanceName = Objects.requireNonNull(instanceName);
    }

    public String getComponentName(){
        return componentName;
    }

    public String getInstanceName(){
        return instanceName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, instanceName);
    }

    private boolean equals(final NotificationSource other){
        return componentName.equals(other.getComponentName()) && instanceName.equals(other.getInstanceName());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof NotificationSource && equals((NotificationSource)other);
    }

    @Override
    public String toString() {
        return String.format("NotificationSource{%s,%s}", componentName, instanceName);
    }
}
