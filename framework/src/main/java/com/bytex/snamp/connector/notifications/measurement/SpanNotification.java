package com.bytex.snamp.connector.notifications.measurement;

import com.bytex.snamp.io.SerializableMap;
import com.bytex.snamp.TypeTokens;
import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents notification about spanning inside of external component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SpanNotification extends StopwatchNotification {
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
    public static final String TYPE = "com.bytex.measurement.span";

    private static final long serialVersionUID = 6986676615521377795L;

    private final Comparable<?> spanID;
    private Comparable<?> parentSpan;

    public SpanNotification(final Comparable<?> spanID,
                            final String componentName,
                            final String instanceName,
                            final String message){
        super(TYPE, componentName, instanceName, message);
        this.spanID = Objects.requireNonNull(spanID);
        setUserData(new SpanContext());
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
}
