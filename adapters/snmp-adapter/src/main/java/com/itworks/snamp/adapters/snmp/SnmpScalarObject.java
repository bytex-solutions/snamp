package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.core.LogicalOperation;
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
    private static final String OID_PARAMETER = "OID";

    private static final class SnmpWriteAttributeLogicalOperation extends WriteAttributeLogicalOperation{
        private SnmpWriteAttributeLogicalOperation(final AttributeAccessor accessor,
                                                   final OID oid){
            super(accessor.getName(), accessor.toString(), OID_PARAMETER, oid);
        }
    }

    private static final class SnmpReadAttributeLogicalOperation extends ReadAttributeLogicalOperation{
        private SnmpReadAttributeLogicalOperation(final AttributeAccessor accessor,
                                                  final OID oid){
            super(accessor.getName(), accessor.toString(), OID_PARAMETER, oid);
        }
    }

    private final T defaultValue;
    private final AttributeAccessor attribute;

    protected SnmpScalarObject(final String oid, final AttributeAccessor attribute, final T defval){
        super(new OID(oid), getAccessRestrictions(attribute), defval);
        this.defaultValue = defval;
        this.attribute = attribute;
    }

    protected static <T> T logAndReturnDefaultValue(final T defaultValue, final Variable originalValue, final ManagedEntityType attributeType){
        SnmpHelpers.log(Level.WARNING, "Cannot convert '%s' value to '%s' attribute type.", originalValue, attributeType, null);
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
        try(final LogicalOperation ignored = new SnmpReadAttributeLogicalOperation(attribute, getOid())) {
            result = attribute.getRawValue();
        }
        catch (final TimeoutException | AttributeSupportException e) {
            SnmpHelpers.log(Level.WARNING, "Read operation failed for %s attribute. Context: %s",
                    attribute, LogicalOperation.current(), e);
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
        try(final LogicalOperation ignored = new SnmpWriteAttributeLogicalOperation(attribute, getOid())) {
            attribute.setValue(convert(value));
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final TimeoutException | AttributeSupportException e) {
            SnmpHelpers.log(Level.WARNING, "Writing operation failed for %s attribute. Context: %s",
                    attribute,
                    LogicalOperation.current(),
                    e);
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
