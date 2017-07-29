package com.bytex.snamp.connector.notifications;

import javax.management.DescriptorRead;

/**
 * Interface to read the Descriptor of a management notification.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface NotificationDescriptorRead extends DescriptorRead {
    /**
     * Returns a copy of Descriptor.
     *
     * @return Descriptor associated with the component implementing this interface.
     * The return value is never null, but the returned descriptor may be empty.
     */
    @Override
    NotificationDescriptor getDescriptor();
}
