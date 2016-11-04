package com.bytex.snamp.connector.notifications.measurement;

import com.bytex.snamp.Identifier;
import com.bytex.snamp.io.SerializableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents notification about spanning inside of external component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SpanMeasurement extends StopwatchMeasurement {
    /**
     * Represents type of this notification.
     */
    public static final String TYPE = "com.bytex.measurement.span";

    public static final class Builder extends StopwatchMeasurement.Builder{
        private Identifier spanID;
        private Identifier parentSpan;
        private Identifier correlationID;
        private final Map<String, String> context;

        private Builder(){
            context = new SpanContext();
        }

        public Builder setSpanID(final Identifier value){
            spanID = Objects.requireNonNull(value);
            return this;
        }

        public Builder setParentSpanID(final Identifier value){
            parentSpan = value;
            return this;
        }

        public Builder setCorrelationID(final Identifier value){
            correlationID = value;
            return this;
        }

        public Builder putInContext(final String key, final String value){
            context.put(key, value);
            return this;
        }

        public Builder putInContext(final Map<String, String> pairs){
            context.putAll(pairs);
            return this;
        }

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public SpanMeasurement get() {
            final SpanMeasurement result = new SpanMeasurement(spanID, getSource(), getMessage());
            result.getContext().putAll(context);
            result.setDuration(getDuration());
            result.setCorrelationID(correlationID);
            result.setParentSpanID(parentSpan);
            result.setSequenceNumber(getSequenceNumber(true));
            result.setTimeStamp(getTimeStamp());
            result.setUserData(getUserData());
            return result;
        }
    }

    /**
     * Represents span context.
     */
    public static final class SpanContext extends HashMap<String, String> implements SerializableMap<String, String>{
        private static final long serialVersionUID = -7265377256048282163L;

        private SpanContext(){

        }
    }

    private static final long serialVersionUID = 6986676615521377795L;

    private final Identifier spanID;
    private Identifier parentSpan;
    private Identifier correlationID;
    private final SpanContext context;

    private SpanMeasurement(final Identifier spanID,
                           final Object source,
                           final String message){
        super(TYPE, source, message);
        this.spanID = Objects.requireNonNull(spanID);
        context = new SpanContext();
    }

    /**
     * Constructs builder for {@link SpanMeasurement} class.
     * @return A new builder.
     */
    public static Builder builder(){
        return new Builder();
    }

    /**
     * Gets context associated with this event.
     * @return Notification context.
     */
    public SpanContext getContext(){
        return context;
    }

    /**
     * Gets correlation identifier of this span.
     * @return Correlation identifier of this span.
     */
    public Identifier getCorrelationID(){
        return correlationID;
    }

    /**
     * Sets correlation identifier of this span.
     * @param value Correlation identifier of this span.
     */
    public void setCorrelationID(final Identifier value){
        correlationID = value;
    }

    /**
     * Gets unique identifier of this notification.
     * @return Unique identifier of this notification.
     */
    public Identifier getSpanID(){
        return spanID;
    }

    public Identifier getParentSpanID(){
        return parentSpan;
    }

    public void setParentSpanID(final Identifier value){
        parentSpan = value;
    }
}
