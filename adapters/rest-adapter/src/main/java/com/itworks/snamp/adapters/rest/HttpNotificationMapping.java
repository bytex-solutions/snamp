package com.itworks.snamp.adapters.rest;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;

/**
 * Represents notification mapping.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpNotificationMapping implements Function<Object, JsonElement> {
    private final String eventCategory;
    private final ManagedEntityType attachmentType;
    private final Gson jsonFormatter;

    HttpNotificationMapping(final NotificationMetadata metadata,
                            final Gson formatter){
        this.eventCategory = metadata.getCategory();
        this.attachmentType = metadata.getAttachmentType();
        this.jsonFormatter = formatter;
    }

    JsonElement getAttachment(final Object attachment){
        if(attachment == null) return JsonNull.INSTANCE;
        else if(attachment instanceof ManagedEntityValue<?>)
            return JsonTypeSystem.toJson((ManagedEntityValue<?>)attachment, jsonFormatter);
        else if(attachmentType != null)
            return JsonTypeSystem.toJson(new ManagedEntityValue<>(attachment, attachmentType), jsonFormatter);
        else return JsonNull.INSTANCE;
    }

    String getCategory(){
        return eventCategory;
    }

    @Override
    public JsonElement apply(final Object input) {
        return getAttachment(input);
    }
}
