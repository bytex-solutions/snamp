package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.AttributeMetadata;
import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.agent.mo.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import static com.itworks.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;
import static com.itworks.snamp.connectors.util.ManagementEntityTypeHelper.ConversionFallback;

/**
 * Represents a base class for scalar SNMP managed objects.
 * @param <T> Type of the ASN notation.
 */
abstract class SnmpScalarObject<T extends Variable> extends MOScalar<T> implements SnmpAttributeMapping {
    private final T defaultValue;
    private final AttributeSupport connector;
    private final TimeSpan timeouts;
    /**
     * Represents the type of the attribute.
     */
    protected final ManagementEntityType attributeTypeInfo;

    private SnmpScalarObject(final String oid, final AttributeSupport connector, final AttributeMetadata attributeInfo, final T defval, final TimeSpan timeouts){
        super(new OID(oid), getAccessRestrictions(attributeInfo), defval);
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        defaultValue = defval;
        this.connector = connector;
        this.timeouts = timeouts;
        this.attributeTypeInfo = attributeInfo.getType();
    }

    protected static <T> T logAndReturnDefaultValue(final T defaultValue, final Variable originalValue, final ManagementEntityType attributeType){
        log.log(Level.WARNING, String.format("Cannot convert '%s' value to '%s' attribute type.", originalValue, attributeType));
        return defaultValue;
    }

    protected static  <T> ConversionFallback<T> fallbackWithDefaultValue(final T defaultValue, final Variable originalValue, final ManagementEntityType attributeType){
        return new ConversionFallback<T>() {
            @Override
            public T call() {
                return logAndReturnDefaultValue(defaultValue, originalValue, attributeType);
            }
        };
    }

    /**
     * Initializes a new SNMP scala value provider.
     * @param oid Unique identifier of the new management object.
     * @param connector The underlying management connector that provides access to the attribute.
     * @param defval The default value of the management object.
     * @param timeouts Read/write timeout.
     * @exception IllegalArgumentException connector is null.
     */
    protected SnmpScalarObject(final String oid, final AttributeSupport connector, final T defval, final TimeSpan timeouts)
    {
        this(oid, connector, connector.getAttributeInfo(oid), defval, timeouts);
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     * @param value The value to convert.
     * @return
     */
    protected abstract T convert(final Object value);

    /**
     * Converts the SNMP-compliant value to the management connector native value.
     * @param value The value to convert.
     * @return
     */
    protected abstract Object convert(final T value);

    private final T getValue(final AttributeSupport connector){
        if(connector == null) return defaultValue;
        Object result = null;
        try{
            result = connector.getAttribute(super.getID().toString(), timeouts, defaultValue);
        }
        catch (final TimeoutException timeout){
            log.log(Level.WARNING, timeout.getLocalizedMessage(), timeout);
            result = defaultValue;

        }
        return result == null ? defaultValue : convert(result);
    }

    /**
     * Returns the SNMP-compliant value of the attribute.
     * @return
     */
    @Override
    public final T getValue() {
        return getValue(connector);
    }

    private final int setValue(final T value, final AttributeSupport connector) {
        if(connector == null) return SnmpConstants.SNMP_ERROR_RESOURCE_UNAVAILABLE;
        int result = SnmpConstants.SNMP_ERROR_SUCCESS;
        try {
            result = connector.setAttribute(super.getID().toString(), timeouts, convert(value)) ? SnmpConstants.SNMP_ERROR_BAD_VALUE : SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final TimeoutException timeout) {
            log.log(Level.WARNING, timeout.getLocalizedMessage(), timeout);
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        }
        return result;
    }

    /**
     * Changes the SNMP management object.
     * @param value
     * @return
     */
    @Override
    public final int setValue(final T value) {
        return setValue(value, connector);
    }

    private final AttributeMetadata getMetadata(final AttributeSupport connector){
        return connector != null ? connector.getAttributeInfo(Objects.toString(getID(), "")) : null;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final AttributeMetadata getMetadata() {
        return getMetadata(connector);
    }
}
