package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.connectors.notifications.NotificationMetadata;

/**
 * Represents notification mapping.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpNotificationMapping {
    private final String eventCategory;

    public HttpNotificationMapping(final NotificationMetadata metadata){
        this.eventCategory = metadata.getCategory();
    }

    public String getCategory(){
        return eventCategory;
    }
}
