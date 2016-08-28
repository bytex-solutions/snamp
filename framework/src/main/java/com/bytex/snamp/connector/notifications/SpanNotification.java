package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.SerializableMap;
import com.bytex.snamp.TypeTokens;
import com.google.common.reflect.TypeToken;

import javax.management.Notification;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents notification about spanning inside of external component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SpanNotification extends Notification {
    private static final TypeToken<Map<String, String>> USER_DATA_TYPE = new TypeToken<Map<String, String>>() {};

    private static final class SpanContext extends HashMap<String, String> implements SerializableMap<String, String>{
        private static final long serialVersionUID = -7265377256048282163L;
        private SpanContext(){

        }

        private SpanContext(final Map<String, String> values){
            super(values);
        }
    }

    /**
     * Represents type of this notification.
     */
    public static final String TYPE = "com.bytex.notifications.span";

    private static final long serialVersionUID = 6986676615521377795L;

    private final Comparable<?> spanID;
    private Comparable<?> parentSpan;
    private Duration duration;

    public SpanNotification(final Comparable<?> spanID,
                            final String componentName,
                            final String instanceName,
                            final String message,
                            final long stopTimeMillis){
        super(TYPE, new SpanSource(componentName, instanceName), 0L, message);
        this.spanID = Objects.requireNonNull(spanID);
        this.duration = Duration.ZERO;
        setTimeStamp(stopTimeMillis);
        setUserData(new SpanContext());
    }

    /**
     * Gets the source of this span.
     *
     * @return The source of this span.
     */
    @Override
    public final SpanSource getSource() {
        return (SpanSource) super.getSource();
    }

    private void setSource(final SpanSource value){
        super.setSource(value);
    }

    public final void setSource(final String componentName, final String instanceName){
        setSource(new SpanSource(componentName, instanceName));
    }

    @Override
    public final void setSource(final Object value) {
        setSource((SpanSource) value);
    }

    /**
     * Gets context of this span.
     * @return The context of this span.
     */
    @Override
    public final Map<String, String> getUserData() {
        return TypeTokens.cast(super.getUserData(), USER_DATA_TYPE);
    }

    /**
     * Sets the context of this span.
     * @param userData The context of this span.
     */
    public final void setUserData(final Map<String, String> userData){
        super.setUserData(new SpanContext(userData));
    }

    /**
     * Sets the context of this span.
     * @param userData The context of this span. Should implements {@link Map}&lt;{@link String}, {@link String}&gt; interface.
     */
    @Override
    public final void setUserData(final Object userData) {
        super.setUserData(TypeTokens.cast(userData, USER_DATA_TYPE));
    }

    /**
     * Gets parent span.
     * @return The identifier of the parent span.
     */
    public final Comparable<?> getParentSpan(){
        return parentSpan;
    }

    /**
     * Sets parent span.
     * @param value The identifier of the parent span.
     */
    public final void setParentSpan(final Comparable<?> value){
        parentSpan = value;
    }

    /**
     * Gets identifier of this span.
     * @return The identifier of this span.
     */
    public final Comparable<?> getIdentifier(){
        return spanID;
    }

    /**
     * Gets duration of this span.
     * @return The duration of this span.
     */
    public final Duration getDuration(){
        return duration;
    }

    /**
     * Sets duration of this span.
     * @param value A new duration of this span.
     */
    public final void setDuration(final Duration value){
        this.duration = Objects.requireNonNull(value);
    }

    /**
     * Gets start time of this span.
     * @return The start time of this span.
     */
    public final Instant getStartTime() {
        return Instant.ofEpochMilli(Duration.ofMillis(getTimeStamp()).minus(duration).toMillis());
    }
}
