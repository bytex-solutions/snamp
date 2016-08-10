package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.smi.*;

import javax.management.DescriptorRead;
import javax.management.InvalidAttributeValueException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.util.Date;

/**
 * Represents SNMP managed object factory.
 * @author Roman Sakno
 */
public enum SnmpType {
    /**
     * Represents type mapping for arbitrary-precision integers and decimals.
     */
    NUMBER(true, SnmpBigNumberObject.SYNTAX) {

        @Override
        protected SnmpBigNumberObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpUnixTimeObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpLongObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpIntegerObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpFloatObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpBooleanObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpStringObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpBufferObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
     * Represents SNMP mapping for byte arrays and bool arrays.
     */
    BLOB(true, SnmpBlobObject.SYNTAX) {
        @Override
        protected SnmpBlobObject createManagedObject(final SnmpAttributeAccessor accessor) {
            return new SnmpBlobObject(accessor);
        }

        @Override
        OctetString convert(final Object value, final DescriptorRead options) {
            return SnmpBlobObject.toSnmpObject(value);
        }

        @Override
        Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException {
            return SnmpBlobObject.fromSnmpObject(value, valueType);
        }
    },

    /**
     * Represents SNMP table mapping.
     */
    TABLE(false, SnmpTableObject.SYNTAX) {
        @Override
        protected SnmpTableObject createManagedObject(final SnmpAttributeAccessor accessor) {
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
        protected SnmpFallbackObject createManagedObject(final SnmpAttributeAccessor accessor) {
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

    private final boolean isScalar;
    private final int syntax;

    SnmpType(final boolean scalar, final int syntax){
        this.isScalar = scalar;
        this.syntax = syntax;
    }

    final boolean isScalar(){
        return isScalar;
    }

    protected abstract SnmpAttributeMapping createManagedObject(final SnmpAttributeAccessor accessor);

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
        return convert(value, DescriptorUtils.EMPTY_DESCRIPTOR);
    }

    abstract Object convert(final Variable value, final Type valueType, final DescriptorRead options) throws InvalidAttributeValueException;

    final Object convert(final Variable value, final Type valueType) throws InvalidAttributeValueException {
        return convert(value, valueType, DescriptorUtils.EMPTY_DESCRIPTOR);
    }

    @Override
    public final String toString() {
        return AbstractVariable.getSyntaxString(syntax);
    }

    static SnmpType map(final WellKnownType type){
        if(type != null)
            switch (type){
                case BOOL: return SnmpType.BOOLEAN;
                case CHAR:
                case OBJECT_NAME:
                case STRING: return SnmpType.TEXT;
                case BIG_DECIMAL:
                case BIG_INT: return SnmpType.NUMBER;
                case BYTE:
                case INT:
                case SHORT: return SnmpType.INTEGER;
                case LONG: return SnmpType.LONG;
                case FLOAT:
                case DOUBLE: return SnmpType.FLOAT;
                case DATE: return SnmpType.UNIX_TIME;
                case BYTE_BUFFER:
                case SHORT_BUFFER:
                case CHAR_BUFFER:
                case INT_BUFFER:
                case LONG_BUFFER:
                case FLOAT_BUFFER:
                case DOUBLE_BUFFER: return SnmpType.BUFFER;
                case BYTE_ARRAY:
                case WRAPPED_BYTE_ARRAY:
                case BOOL_ARRAY:
                case WRAPPED_BOOL_ARRAY:
                    return SnmpType.BLOB;
                case FLOAT_ARRAY:
                case WRAPPED_FLOAT_ARRAY:
                case SHORT_ARRAY:
                case WRAPPED_SHORT_ARRAY:
                case INT_ARRAY:
                case WRAPPED_INT_ARRAY:
                case LONG_ARRAY:
                case WRAPPED_LONG_ARRAY:
                case DICTIONARY:
                case TABLE: return SnmpType.TABLE;
            }
        return SnmpType.FALLBACK;
    }
}
