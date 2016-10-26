package com.bytex.snamp.connector.notifications.measurement;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes the source of all measurements.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MeasurementSource implements Serializable {
    private static final long serialVersionUID = -3632338167739067932L;

    private final String componentName;
    private final String instanceName;

    public MeasurementSource(final String componentName, final String instanceName){
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

    private boolean equals(final MeasurementSource other){
        return componentName.equals(other.getComponentName()) && instanceName.equals(other.getInstanceName());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof MeasurementSource && equals((MeasurementSource)other);
    }

    @Override
    public String toString() {
        return String.format("MonitoringNotificationSource: {%s:%s}", componentName, instanceName);
    }
}
