package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.connectors.notifications.NotificationMetadata;

import javax.management.MBeanNotificationInfo;
import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.NOTIF_TYPE_PARAM;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxNotificationMapping implements JmxFeature<MBeanNotificationInfo> {
    private final String category;
    private final String description;

    public JmxNotificationMapping(final NotificationMetadata meta){
        this.category = meta.containsKey(NOTIF_TYPE_PARAM) ?
                meta.get(NOTIF_TYPE_PARAM) :
                meta.getCategory();
        final String descr = meta.getDescription(null);
        this.description = descr == null || descr.isEmpty() ?
                String.format("Description stub for %s event", category) :
                descr;
    }

    public String getCategory(){
        return category;
    }

    @Override
    public MBeanNotificationInfo createFeature(final String featureName) {
        return new MBeanNotificationInfo(new String[]{category}, featureName, description);
    }
}
