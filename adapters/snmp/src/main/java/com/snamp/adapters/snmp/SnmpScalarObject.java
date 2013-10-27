package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import static com.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;

/**
 * Represents a base class for scalar SNMP managed objects.
 * @param <T> Type of the ASN notation.
 */
abstract class SnmpScalarObject<T extends Variable> extends MOScalar<T> implements SnmpAttributeMapping {
    private final T defaultValue;
    private final Reference<ManagementConnector> connector;
    private final TimeSpan timeouts;
    /**
     * Represents the type of the attribute.
     */
    protected final AttributeTypeInfo attributeTypeInfo;

    private SnmpScalarObject(final String oid, final ManagementConnector connector, final AttributeMetadata attributeInfo, final T defval, final TimeSpan timeouts){
        super(new OID(oid), getAccessRestrictions(attributeInfo), defval);
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        this.connector = new WeakReference<ManagementConnector>(connector);
        defaultValue = defval;
        this.timeouts = timeouts;
        this.attributeTypeInfo = attributeInfo.getAttributeType();
    }

    /**
     * Initializes a new SNMP scala value provider.
     * @param oid Unique identifier of the new management object.
     * @param connector The underlying management connector that provides access to the attribute.
     * @param defval The default value of the management object.
     * @param timeouts Read/write timeout.
     * @exception IllegalArgumentException connector is null.
     */
    protected SnmpScalarObject(final String oid, final ManagementConnector connector, final T defval, final TimeSpan timeouts)
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

    private final T getValue(final ManagementConnector connector){
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
        return getValue(connector.get());
    }

    private final int setValue(final T value, final ManagementConnector connector) {
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
        return setValue(value, connector.get());
    }

    private final AttributeMetadata getMetadata(final ManagementConnector connector){
        return connector != null ? connector.getAttributeInfo(Objects.toString(getID(), "")) : null;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final AttributeMetadata getMetadata() {
        return getMetadata(connector.get());
    }
}
