package com.bytex.snamp.connector.composite;

import javax.management.DescriptorRead;

/**
 * Represents feature as a part of composition of features.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
interface CompositeFeature extends DescriptorRead, Cloneable {
    String getConnectorType();
}
