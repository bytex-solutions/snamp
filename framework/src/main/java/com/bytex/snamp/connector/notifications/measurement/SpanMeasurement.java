package com.bytex.snamp.connector.notifications.measurement;

import com.bytex.snamp.Identifier;
import com.bytex.snamp.TypeTokens;
import com.bytex.snamp.io.SerializableMap;
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
public final class SpanMeasurement extends StopwatchMeasurement {
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

    private final Identifier spanID;
    private Identifier parentSpan;
    private Identifier correlationID;

    public SpanMeasurement(final Identifier spanID,
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
