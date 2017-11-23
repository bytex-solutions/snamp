package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.attributes.AttributeDescriptorRead;

import javax.management.openmbean.OpenMBeanAttributeInfo;

/**
 * Represents JMX attribute metadata.
 */
interface JmxAttributeMetadata extends OpenMBeanAttributeInfo, JmxFeatureMetadata, AttributeDescriptorRead {
    String getType();
}
