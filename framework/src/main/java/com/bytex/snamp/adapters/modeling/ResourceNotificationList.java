package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.ArrayUtils;

import javax.management.MBeanNotificationInfo;

/**
 * Represents a collection of managed resource notifications.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ResourceNotificationList<TAccessor extends NotificationAccessor> extends ResourceFeatureList<MBeanNotificationInfo, TAccessor> {
    private static final long serialVersionUID = -2082078761110390221L;

    public ResourceNotificationList() {
        super(10);
    }

    /**
     * Gets identity of the managed resource feature.
     *
     * @param feature The managed resource feature.
     * @return The identity of the managed resource feature.
     * @see javax.management.MBeanAttributeInfo#getName()
     * @see javax.management.MBeanNotificationInfo#getNotifTypes()
     */
    @Override
    protected String getKey(final MBeanNotificationInfo feature) {
        return ArrayUtils.getFirst(feature.getNotifTypes());
    }
}
