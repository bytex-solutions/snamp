package com.bytex.snamp.connector.jmx;

import javax.management.DescriptorRead;
import javax.management.ObjectName;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
interface JmxFeatureMetadata extends Serializable, DescriptorRead {
    ObjectName getOwner();

    String getName();

    String getDescription();

    String getAlias();
}
