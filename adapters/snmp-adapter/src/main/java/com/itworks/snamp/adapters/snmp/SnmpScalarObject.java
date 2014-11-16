package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.ConversionFallback;

/**
 * Represents a base class for scalar SNMP managed objects.
 * @param <T> Type of the ASN notation.
 */
abstract class SnmpScalarObject<T extends Variable> extends MOScalar<T> implements SnmpAttributeMapping {
    private final T defaultValue;
    private final AttributeAccessor attribute;

    protected SnmpScalarObject(final String oid, final AttributeAccessor attribute, final T defval){
        super(new OID(oid), getAccessRestrictions(attribute), defval);
        this.defaultValue = defval;
        this.attribute = attribute;
    }

    protected static <T> T logAndReturnDefaultValue(final T defaultValue, final Variable originalValue, final ManagedEntityType attributeType){
        log.log(Level.WARNING, String.format("Cannot convert '%s' value to '%s' attribute type.", originalValue, attributeType));
        return defaultValue;
    }

    protected static  <T> ConversionFallback<T> fallbackWithDefaultValue(final T defaultValue, final Variable originalValue, final ManagedEntityType attributeType){
        return new ConversionFallback<T>() {
            @Override
            public T call() {
                return logAndReturnDefaultValue(defaultValue, originalValue, attributeType);
            }
        };
    }

    /**
     * Converts the attribute value into the SNMP-compliant value.
     * @param value The value to convert.
     * @return SNMP-compliant representation of the specified value.
     */
    protected abstract T convert(final Object value);

    /**
     * Converts SNMP-compliant value to the resource-specific native value.
     * @param value The value to convert.
     * @return Resource-specific representation of SNMP-compliant value.
     */
    protected abstract Object convert(final T value);

    /**
     * Returns SNMP-compliant value of the attribute.
     * @return SNMP-compliant value of the attribute.
     */
    @Override
    public final T getValue() {
        Object result;
        try{
            result = attribute.getRawValue();
        }
        catch (final TimeoutException | AttributeSupportException e){
            log.log(Level.WARNING, String.format("Read operation failed for %s attribute", attribute.getName()), e);
            result = defaultValue;
        }
        return result == null ? defaultValue : convert(result);
    }

    /**
     * Changes the SNMP management object.
     * @param value The value to set.
     * @return SNMP status code.
     * @see {@link org.snmp4j.mp.SnmpConstants}
     */
    @Override
    public final int setValue(final T value) {
        int result;
        try {
            attribute.setValue(convert(value));
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final TimeoutException | AttributeSupportException e) {
            log.log(Level.WARNING, e.getLocalizedMessage(), e);
            result = SnmpConstants.SNMP_ERROR_RESOURCE_UNAVAILABLE;
        }
        return result;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public final AttributeMetadata getMetadata() {
        return attribute;
    }
}
