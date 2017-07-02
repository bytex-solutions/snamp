package com.bytex.snamp.connector.composite;

import javax.annotation.Nonnull;
import javax.management.DescriptorRead;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface DistributedAttribute extends DescriptorRead {
    boolean isReadable();
    boolean isWritable();
    boolean isIs();
    @Nonnull
    Serializable takeSnapshot();
    void loadFromSnapshot(@Nonnull final Serializable state);
}
