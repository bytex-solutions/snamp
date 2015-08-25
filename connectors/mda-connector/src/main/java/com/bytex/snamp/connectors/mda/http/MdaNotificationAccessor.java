package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.bytex.snamp.connectors.mda.MdaResourceConfigurationDescriptorProvider.parseType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MdaNotificationAccessor extends CustomNotificationInfo {
    private static final String MESSAGE_FIELD  = "message";
    private static final String TIME_STAMP_FIELD = "timeStamp";
    private static final String SEQ_NUM_FIELD = "sequenceNumber";
    private static final String DATA_FIELD = "data";

    private static final Function<JsonElement, Serializable> FALLBACK_PARSER = new Function<JsonElement, Serializable>() {
        private Serializable apply(final JsonPrimitive input){
            if(input.isBoolean())
                return input.getAsBoolean();
            else if(input.isNumber())
                return input.getAsDouble();
            else if(input.isString())
                return input.getAsString();
            else return input.toString();
        }

        @Override
        public Serializable apply(final JsonElement input) {
            if(input.isJsonNull())
                return null;
            else if(input.isJsonPrimitive())
                return apply(input.getAsJsonPrimitive());
            else return input.toString();
        }
    };

    private static final class SimpleAttachmentParser implements Function<JsonElement, Object>{
        private final WellKnownType attachmentType;
        private final Gson jsonFormatter;

        private SimpleAttachmentParser(final Gson jsonFormatter, final OpenType<?> type){
            this.attachmentType = WellKnownType.getType(type);
            this.jsonFormatter = jsonFormatter;
        }

        @Override
        public Object apply(final JsonElement input) {
            return jsonFormatter.fromJson(input, attachmentType.getJavaType());
        }
    }

    private static final class CompositeDataParser implements Function<JsonElement, CompositeData>{
        private final CompositeType attachmentType;
        private final Gson jsonFormatter;

        private CompositeDataParser(final Gson formatter, final CompositeType type){
            this.jsonFormatter = Objects.requireNonNull(formatter);
            this.attachmentType = Objects.requireNonNull(type);
        }

        private CompositeData apply(final JsonObject input){
            try {
                return CompositeAttributeManager.deserialize(input, jsonFormatter, attachmentType);
            } catch (final OpenDataException ignored) {
                return null;
            }
        }

        @Override
        public CompositeData apply(final JsonElement input) {
            return input.isJsonObject() ? apply(input.getAsJsonObject()) : null;
        }
    }

    private final AtomicLong sequenceNumberCounter;
    private final Function<JsonElement, ?> attachmentParser;

    MdaNotificationAccessor(final String notifType,
                            final NotificationDescriptor descriptor,
                            final Gson formatter) throws OpenDataException {
        super(notifType, descriptor.getDescription(descriptor.getNotificationCategory()), descriptor);
        sequenceNumberCounter = new AtomicLong(0L);
        this.attachmentParser = createAttachmentParser(formatter, parseType(descriptor));
    }

    private static Function<JsonElement, ?> createAttachmentParser(final Gson formatter,
                                                                   final OpenType<?> attachmentType){
        if(attachmentType == null)
            return FALLBACK_PARSER;
        else if(attachmentType instanceof SimpleType<?> || attachmentType instanceof ArrayType<?>)
            return new SimpleAttachmentParser(formatter, attachmentType);
        else if(attachmentType instanceof CompositeType)
            return new CompositeDataParser(formatter, (CompositeType)attachmentType);
        else return FALLBACK_PARSER;
    }

    static String getMessage(final JsonObject notification){
        return notification.has(MESSAGE_FIELD) ? notification.get(MESSAGE_FIELD).getAsString() : "";
    }

    static long getTimeStamp(final JsonObject notification){
        return notification.has(TIME_STAMP_FIELD) ? notification.get(TIME_STAMP_FIELD).getAsLong() : System.currentTimeMillis();
    }

    long getSequenceNumber(final JsonObject notification){
        return notification.has(SEQ_NUM_FIELD) ?
                notification.get(SEQ_NUM_FIELD).getAsLong() : sequenceNumberCounter.getAndIncrement();
    }

    Object getUserData(final JsonObject notification){
        return notification.has(DATA_FIELD) ? attachmentParser.apply(notification.get(DATA_FIELD)) : null;
    }
}
