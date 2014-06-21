package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface HttpNotificationsModel {
    Gson getJsonFormatter();

    HttpNotificationMapping get(final String resourceName, final String userDefineEventName);
}
