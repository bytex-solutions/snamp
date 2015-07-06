package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.jmx.DescriptorUtils;
import org.snmp4j.agent.*;
import org.snmp4j.smi.*;

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.InvalidAttributeValueException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.util.Date;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseOID;

/**
 * Represents SNMP managed object factory.
 * @author Roman Sakno
 */
enum SnmpType {
    /**
     * Represents type mapping for arbitrary-precision integers and decimals.
     */
    NUMBER(true, SnmpBigNumberObject.SYNTAX) {

        @Override
        protected SnmpBigNumberObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpBigNumberObject(accessor);
        }

        @Override
        protected OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpBigNumberObject.toSnmpObject(value);
        }

        @Override
        protected Number convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpBigNumberObject.fromSnmpObject((AssignableFromByteArray)value, valueType);
        }
    },

    /**
     * Represents unix time.
     */
    UNIX_TIME(true, SnmpUnixTimeObject.SYNTAX) {

        @Override
        protected SnmpUnixTimeObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpUnixTimeObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpUnixTimeObject.toSnmpObject(value, options);
        }

        @Override
        Date convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException{
            return SnmpUnixTimeObject.fromSnmpObject(value, options);
        }
    },

    /**
     * Represents Long SNMP type mapping.
     */
    LONG(true, SnmpLongObject.SYNTAX) {
        @Override
        protected SnmpLongObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpLongObject(accessor);
        }

        @Override
        Counter64 convert(final Object value, final DescriptorRead options) {
            return SnmpLongObject.toSnmpObject(value);
        }

        @Override
        Long convert(final Variable value, final Type valueType, final DescriptorRead options) {
            return SnmpLongObject.fromSnmpObject(value);
        }
    },

    /**
     * Represents Integer SNMP type mapping.
     */
    INTEGER(true, SnmpIntegerObject.SYNTAX) {
        @Override
        protected SnmpIntegerObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpIntegerObject(accessor);
        }

        @Override
        Integer32 convert(final Object value, final DescriptorRead options) {
            return SnmpIntegerObject.toSnmpObject(value);
        }

        @Override
        Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpIntegerObject.fromSnmpObject(value, valueType);
        }
    },

    /**
     * Represents Float SNMP type mapping.
     */
    FLOAT(true, SnmpFloatObject.SYNTAX) {
        @Override
        protected SnmpFloatObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpFloatObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpFloatObject.toSnmpObject(value);
        }

        @Override
        Number convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpFloatObject.fromSnmpObject((AssignableFromByteArray)value, valueType);
        }
    },

    /**
     * Represents Boolean SNMP type mapping.
     */
    BOOLEAN(true, SnmpBooleanObject.SYNTAX) {
        @Override
        protected SnmpBooleanObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpBooleanObject(accessor);
        }

        @Override
        Integer32 convert(final Object value, final DescriptorRead options) {
            return SnmpBooleanObject.toSnmpObject(value);
        }

        @Override
        Boolean convert(final Variable value, final Type valueType, final DescriptorRead options) {
            return SnmpBooleanObject.fromSnmpObject(value);
        }
    },

    /**
     * Represents String SNMP type mapping.
     */
    TEXT(true, SnmpStringObject.SYNTAX) {
        @Override
        protected SnmpStringObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpStringObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpStringObject.toSnmpObject(value);
        }

        @Override
        Serializable convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpStringObject.fromSnmpObject((AssignableFromByteArray)value, valueType);
        }
    },

    /**
     * Represents SNMP mapping for {@link java.nio.Buffer}.
     */
    BUFFER(true, SnmpBufferObject.SYNTAX) {
        @Override
        protected SnmpBufferObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpBufferObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpBufferObject.toSnmpObject(value);
        }

        @Override
        Buffer convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpBufferObject.fromSnmpObject(value, valueType);
        }
    },

    /**
     * Represents SNMP mapping for byte arrays.
     */
    BYTE_ARRAY(true, SnmpByteArrayObject.SYNTAX) {
        @Override
        protected SnmpByteArrayObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpByteArrayObject( accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpByteArrayObject.toSnmpObject(value);
        }

        @Override
        Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpByteArrayObject.fromSnmpObject(value, valueType);
        }
    },

    /**
     * Represents SNMP table mapping.
     */
    TABLE(false, SnmpTableObject.SYNTAX) {
        @Override
        protected SnmpTableObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpTableObject(accessor);
        }

        @Override
        Variable convert(final Object value, final DescriptorRead options) {
            throw new UnsupportedOperationException("SNMP Table doesn't support conversion");
        }

        @Override
        Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            throw new InvalidAttributeValueException("SNMP Table doesn't support conversion");
        }
    },

    /**
     * Represents OctetString SNMP type mapping used as a fallback for attributes with unknown types.
     */
    FALLBACK(true, SnmpFallbackObject.SYNTAX) {
        @Override
        protected SnmpFallbackObject createManagedObject(final AttributeAccessor accessor) {
            return new SnmpFallbackObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpFallbackObject.toSnmpObject(value);
        }

        @Override
        Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            throw SnmpFallbackObject.readOnlyException();
        }
    };

    private static final DescriptorRead EMPTY_DESCRIPTOR = new DescriptorRead() {
        @Override
        public Descriptor getDescriptor() {
            return DescriptorUtils.EMPTY;
        }
    };

    private final boolean isScalar;
    private final int syntax;

    SnmpType(final boolean scalar, final int syntax){
        this.isScalar = scalar;
        this.syntax = syntax;
    }

    final boolean isScalar(){
        return isScalar;
    }

    protected abstract SnmpAttributeMapping createManagedObject(final AttributeAccessor accessor);

    private static ManagedObject unregisterManagedObject(final OID attributeID,
                                                         final MOServer server){
        final MOQuery query = new DefaultMOQuery(new DefaultMOContextScope(null, attributeID, true, attributeID, true));
        ManagedObject result = server.lookup(query);
        if(result != null)
            result = server.unregister(result, null);
        return result;
    }

    static ManagedObject unregisterManagedObject(final AttributeAccessor accessor,
                                                 final MOServer server){
        final OID attributeID = new OID(parseOID(accessor));
        return unregisterManagedObject(attributeID, server);
    }

    /**
     * Creates a new instance of the SNMP managed object.
     * @param accessor An object that provides access to the individual management attribute.
     * @return A new mapping between resource attribute and its SNMP representation.
     */
    final SnmpAttributeMapping registerManagedObject(final AttributeAccessor accessor,
                                                        final OID context,
                                                        final MOServer server) throws DuplicateRegistrationException {
        final OID attributeID = new OID(parseOID(accessor));
        final SnmpAttributeMapping mapping;
        //do not add the attribute with invalid prefix
        if (attributeID.startsWith(context)) {
            mapping = createManagedObject(accessor);
            server.register(mapping, null);
        }
        else mapping = null;
        return mapping;
    }

    /**
     * Returns a value from {@link org.snmp4j.smi.SMIConstants} that represents value syntax type.
     * @return The value syntax type.
     */
    final int getSyntax(){
        return syntax;
    }

    abstract Variable convert(final Object value, final DescriptorRead options);

    /**
     * Converts the specified value to the SNMP-compliant value.
     * @return SNMP-compliant value.
     */
    final Variable convert(final Object value)  {
        return convert(value, EMPTY_DESCRIPTOR);
    }

    abstract Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException;

    final Object convert(final Variable value, final Type valueType) throws InvalidAttributeValueException {
        return convert(value, valueType, EMPTY_DESCRIPTOR);
    }
}
