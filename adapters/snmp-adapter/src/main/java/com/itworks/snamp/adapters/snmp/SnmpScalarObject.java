package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;
import java.lang.reflect.Type;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;
import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.getOID;

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

    protected SnmpScalarObject(final AttributeAccessor attribute, final T defval){
        this(attribute, false, defval);
    }

    protected SnmpScalarObject(final AttributeAccessor attribute,
                               final boolean readOnly,
                               final T defval){
        super(new OID(getOID(attribute.getMetadata())),
                readOnly ? MOAccessImpl.ACCESS_READ_ONLY : getAccessRestrictions(attribute.getMetadata()),
                defval);
        this.defaultValue = defval;
        this.attribute = attribute;
    }

    protected static <T extends Variable> InvalidAttributeValueException unexpectedSnmpType(final Class<T> type){
        return new InvalidAttributeValueException(String.format("%s expected", type));
    }

    protected static InvalidAttributeValueException unexpectedAttributeType(final Type type){
        return new InvalidAttributeValueException(String.format("Unexpected type %s", type));
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
    protected abstract Object convert(final T value) throws JMException;

    /**
     * Returns SNMP-compliant value of the attribute.
     * @return SNMP-compliant value of the attribute.
     */
    @Override
    public final T getValue() {
        Object result;
        try(final LogicalOperation ignored = new SnmpReadAttributeLogicalOperation(attribute, getOid())) {
            result = attribute.getValue();
        }
        catch (final JMException e) {
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
        } catch (final JMException e) {
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
    public final MBeanAttributeInfo getMetadata() {
        return attribute.getMetadata();
    }

    protected final Type getAttributeType() throws ReflectionException {
        final WellKnownType knownType = attribute.getType();
        return knownType != null ? knownType : attribute.getRawType();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public final <O> O queryObject(final Class<O> objectType) {
        return objectType.isAssignableFrom(AttributeAccessor.class) ? objectType.cast(attribute) : null;
    }
}
