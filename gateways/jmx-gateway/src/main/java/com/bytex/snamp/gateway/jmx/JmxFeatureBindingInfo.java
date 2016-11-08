package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.connector.notifications.Severity;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanFeatureInfo;
import java.util.Map;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.jmx.DescriptorUtils.toMap;

/**
 * Represents interface for JMX gateway features.
 */
interface JmxFeatureBindingInfo<M extends MBeanFeatureInfo> extends FeatureBindingInfo<M> {
    static Object jmxCompliance(final Object value){
        if(value instanceof Severity)       //convert severity into string due to JMX classloading exception: "no security manager: RMI class loader disabled"
            return value.toString();
        else
            return value;
    }

    M cloneMetadata();

    static ImmutableDescriptor cloneDescriptor(final Descriptor source) {
        final Map<String, Object> descriptor = toMap(source, JmxFeatureBindingInfo::jmxCompliance, false);
        return new ImmutableDescriptor(descriptor);
    }
}
