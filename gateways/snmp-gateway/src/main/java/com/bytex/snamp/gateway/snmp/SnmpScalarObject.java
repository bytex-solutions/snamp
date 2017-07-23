package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.ReadAttributeLoggingScope;
import com.bytex.snamp.gateway.modeling.WriteAttributeLoggingScope;
import com.bytex.snamp.internal.Utils;
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
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.bytex.snamp.gateway.snmp.SnmpGatewayDescriptionProvider.parseOID;
import static com.bytex.snamp.gateway.snmp.SnmpHelpers.getAccessRestrictions;

/**
 * Represents a base class for scalar SNMP managed objects.
 * @param <T> Type of the ASN notation.
 */
abstract class SnmpScalarObject<T extends Variable> extends MOScalar<T> implements SnmpAttributeMapping {
    private static final class SnmpWriteAttributeLoggingScope extends WriteAttributeLoggingScope {

        private SnmpWriteAttributeLoggingScope(final AttributeAccessor accessor,
                                               final OID oid){
            super(accessor, accessor.getName(), oid.toString());
        }

        private void failedToWrite(final JMException e){
            log(Level.WARNING, String.format("Writing operation failed for attribute %s", attributeID), e);
        }
    }

    private static final class SnmpReadAttributeLoggingScope extends ReadAttributeLoggingScope {
        private SnmpReadAttributeLoggingScope(final AttributeAccessor accessor,
                                              final OID oid){
            super(accessor, accessor.getName(), oid.toString());
        }

        private void failedToRead(final JMException e){
            log(Level.WARNING, String.format("Reading operation failed for attribute %s", attributeID), e);
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

    static <T extends Variable> InvalidAttributeValueException unexpectedSnmpType(final Class<T> type){
        return new InvalidAttributeValueException(String.format("%s expected", type));
    }

    static InvalidAttributeValueException unexpectedAttributeType(final Type type){
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
        final SnmpReadAttributeLoggingScope logger = new SnmpReadAttributeLoggingScope(accessor, getOid());
        try {
            result = accessor.getValue();
        } catch (final JMException e) {
            logger.failedToRead(e);
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
        final SnmpWriteAttributeLoggingScope logger =
                new SnmpWriteAttributeLoggingScope(accessor, getOid());
        try {
            accessor.setValue(convert(value));
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final JMException e) {
            logger.failedToWrite(e);
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

    final Type getAttributeType() throws ReflectionException {
        final WellKnownType knownType = accessor.getType();
        return knownType != null ? knownType : accessor.getRawType();
    }

    @Override
    public final boolean connect(final OID context, final MOServer server) throws DuplicateRegistrationException {
        //do not add the attribute if it is exist
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

    private Supplier<OID> getOidGenerator(){
        return SnmpHelpers.getOidGenerator(Utils.getBundleContextOfObject(this));
    }

    @Override
    public final boolean equals(final MBeanAttributeInfo metadata) {
        return Objects.equals(getID(), parseOID(metadata, getOidGenerator()));
    }

    @Override
    public final String toString() {
        return String.format("Scalar. Metadata: %s; OID: %s; Access: %s", accessor.toString(), getID(), getAccess());
    }
}
