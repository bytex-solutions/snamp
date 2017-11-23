package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.operations.OperationDescriptorRead;

import javax.management.MBeanParameterInfo;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
interface JmxOperationMetadata extends JmxFeatureMetadata, OperationDescriptorRead {
    String getReturnType();

    MBeanParameterInfo[] getSignature();

    int getImpact();
}
