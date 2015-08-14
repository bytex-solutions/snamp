package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.adapters.modeling.ReadAttributeLogicalOperation;
import com.bytex.snamp.adapters.modeling.WriteAttributeLogicalOperation;
import com.bytex.snamp.core.LogicalOperation;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
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
import java.text.ParseException;
import java.util.Objects;
import java.util.logging.Level;

import static com.bytex.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseOID;
import static com.bytex.snamp.adapters.snmp.SnmpHelpers.getAccessRestrictions;

/**
 * Represents a base class for scalar SNMP managed objects.
 * @param <T> Type of the ASN notation.
 */
abstract class SnmpScalarObject<T extends Variable> extends MOScalar<T> implements SnmpAttributeMapping {
    private static final String OID_PARAMETER = "OID";

    private static final class SnmpWriteAttributeLogicalOperation extends WriteAttributeLogicalOperation{
        private SnmpWriteAttributeLogicalOperation(final AttributeAccessor accessor,
                                                   final OID oid){
            super(SnmpHelpers.getLogger(), accessor.getName(), accessor.toString(), OID_PARAMETER, oid);
        }
    }

    private static final class SnmpReadAttributeLogicalOperation extends ReadAttributeLogicalOperation{
        private SnmpReadAttributeLogicalOperation(final AttributeAccessor accessor,
                                                  final OID oid){
            super(SnmpHelpers.getLogger(), accessor.getName(), accessor.toString(), OID_PARAMETER, oid);
        }
    }

    private final AttributeAccessor accessor;

    protected SnmpScalarObject(final SnmpAttributeAccessor attribute, final T defval) {
        this(attribute, false, defval);
    }

    protected SnmpScalarObject(final SnmpAttributeAccessor attribute,
                               final boolean readOnly,
                               final T defval) {
        super(attribute.getID(),
                readOnly ? MOAccessImpl.ACCESS_READ_ONLY : getAccessRestrictions(attribute.getMetadata()),
                defval);
        this.accessor = attribute;
        setVolatile(true);
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

    private T getDefaultValue(){
        return super.getValue();
    }

    /**
     * Returns SNMP-compliant value of the attribute.
     * @return SNMP-compliant value of the attribute.
     */
    @Override
    public final T getValue() {
        Object result;
        final LogicalOperation logger = new SnmpReadAttributeLogicalOperation(accessor, getOid());
        try {
            result = accessor.getValue();
        } catch (final JMException e) {
            logger.log(Level.WARNING, String.format("Read operation failed for %s attribute", accessor.getName()), e);
            result = getDefaultValue();
        } finally {
            logger.close();
        }
        return result == null ? getDefaultValue() : convert(result);
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
        final LogicalOperation logger =
                new SnmpWriteAttributeLogicalOperation(accessor, getOid());
        try {
            accessor.setValue(convert(value));
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final JMException e) {
            logger.log(Level.WARNING, String.format("Writing operation failed for %s attribute", accessor), e);
            result = SnmpConstants.SNMP_ERROR_RESOURCE_UNAVAILABLE;
        } finally {
            logger.close();
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
        return accessor.getMetadata();
    }

    protected final Type getAttributeType() throws ReflectionException {
        final WellKnownType knownType = accessor.getType();
        return knownType != null ? knownType : accessor.getRawType();
    }

    @Override
    public final boolean connect(final OID context, final MOServer server) throws DuplicateRegistrationException {
        //do not add the attribute
        if (getID().startsWith(context)) {
            server.register(this, null);
            return true;
        }
        else return false;
    }

    @Override
    public final AttributeAccessor disconnect(final MOServer server) {
        if(server != null) {
            server.unregister(this, null);
        }
        else accessor.close();
        return accessor;
    }

    @Override
    public final boolean equals(final MBeanAttributeInfo metadata) {
        try {
            return Objects.equals(getID(), parseOID(metadata));
        } catch (final ParseException ignored) {
            return false;
        }
    }

    @Override
    public final String toString() {
        return String.format("Scalar. Metadata: %s; OID: %s; Access: %s", accessor.toString(), getID(), getAccess());
    }
}
