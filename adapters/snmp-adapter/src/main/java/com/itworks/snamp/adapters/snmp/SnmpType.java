package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.Variable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;
import static org.snmp4j.smi.SMIConstants.EXCEPTION_NO_SUCH_OBJECT;

/**
 * Represents SNMP managed object factory.
 * @author Roman Sakno
 */
enum SnmpType {
    /**
     * Represents type mapping for arbitrary-precision integers and decimals.
     */
    NUMBER(SnmpBigNumberObject.class),

    /**
     * Represents unix time.
     */
    UNIX_TIME(SnmpUnixTimeObject.class),

    /**
     * Represents Long SNMP type mapping.
     */
    LONG(SnmpIntegerObject.class),

    /**
     * Represents Integer SNMP type mapping.
     */
    INTEGER(SnmpIntegerObject.class),

    /**
     * Represents Float SNMP type mapping.
     */
    FLOAT(SnmpFloatObject.class),

    /**
     * Represents Boolean SNMP type mapping.
     */
    BOOLEAN(SnmpBooleanObject.class),

    /**
     * Represents String SNMP type mapping.
     */
    TEXT(SnmpStringObject.class),

    /**
     * Represents SNMP table mapping.
     */
    TABLE(SnmpTableObject.class);

    private final Class<? extends SnmpAttributeMapping> mapping;
    private final MOSyntax syntax;
    private Method toVariableConverter;
    private Method fromVariableConverter;

    private SnmpType(final Class<? extends SnmpAttributeMapping> mapping){
        this.mapping = mapping;
        this.syntax = mapping.getAnnotation(MOSyntax.class);
        this.toVariableConverter = null;
        this.fromVariableConverter = null;
    }

    /**
     * Creates a new instance of the SNMP managed object.
     * @param oid OID of the managed object.
     * @param accessor An object that provides access to the individual management attribute.
     * @return A new mapping between resource attribute and its SNMP representation.
     */
    public SnmpAttributeMapping createManagedObject(final String oid, final AttributeAccessor accessor){
        try {
            final Constructor<? extends SnmpAttributeMapping> ctor = mapping.getConstructor(String.class, AttributeAccessor.class);
            return ctor.newInstance(oid, accessor);
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
            return null;
        }
    }

    /**
     * Returns a value from {@link org.snmp4j.smi.SMIConstants} that represents value syntax type.
     * @return The value syntax type.
     */
    public int getSyntax(){
        return syntax != null ? syntax.value() : EXCEPTION_NO_SUCH_OBJECT;
    }

    public Variable convert(final Object value, final ManagedEntityType valueType, final Map<String, String> options){
        if(toVariableConverter == null)
            try {
                toVariableConverter = mapping.getMethod("convert", Object.class, ManagedEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                try {
                    toVariableConverter = mapping.getMethod("convert", Object.class, ManagedEntityType.class, Map.class);
                } catch (NoSuchMethodException e1) {
                    SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                    return new Null();
                }
            }
        //attempts to invoke the converter.
        try {
            switch (toVariableConverter.getParameterTypes().length){
                case 2: return (Variable)toVariableConverter.invoke(null, value, valueType);
                case 3: return (Variable)toVariableConverter.invoke(null, value, valueType, options);
                default: throw new ReflectiveOperationException("SnmpAgent: java-to-snmp converter not found.");
            }
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return new Null();
        }
    }

    /**
     * Converts the specified value to the SNMP-compliant value.
     * @param value The value to convert.
     * @param valueType The value type.
     * @return SNMP-compliant value.
     */
    public Variable convert(final Object value, final ManagedEntityType valueType){
        return convert(value, valueType, Collections.<String, String>emptyMap());
    }

    public Object convert(final Variable value, final ManagedEntityType valueType, final Map<String, String> options){
        if(fromVariableConverter == null)
            try {
                fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagedEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                try {
                    fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagedEntityType.class);
                } catch (final NoSuchMethodException e1) {
                    SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                    return null;
                }

            }
        //attempts to invoke the converter.
        try {
            switch (fromVariableConverter.getParameterTypes().length){
                case 2: return fromVariableConverter.invoke(null, value, valueType);
                case 3: return fromVariableConverter.invoke(null, value, valueType, options);
                default: throw new ReflectiveOperationException("java-to-snmp converter not found.");
            }
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    public Object convert(final Variable value, final ManagedEntityType valueType){
        if(fromVariableConverter == null)
            try {
                fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagedEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                return null;
            }
        //attempts to invoke the converter.
        try {
            return fromVariableConverter.invoke(null, value, valueType);
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Maps the attribute type to the SNMP-compliant type.
     * @param attributeType Resource-specific type of the attribute.
     * @return SNMP-compliant projection of the attribute type.
     */
    public static SnmpType map(final ManagedEntityType attributeType){
        if(supportsBoolean(attributeType))
            return BOOLEAN;
        else if(supportsInt8(attributeType) || supportsInt16(attributeType) || supportsInt32(attributeType))
            return INTEGER;
        else if(supportsInt64(attributeType))
            return LONG;
        else if(supportsFloat(attributeType) || supportsDouble(attributeType))
            return FLOAT;
        else if(supportsInteger(attributeType) || supportsDecimal(attributeType))
            return NUMBER;
        else if(supportsUnixTime(attributeType))
            return UNIX_TIME;
        else if(isTable(attributeType))
            return TABLE;
        else return TEXT;
    }
}
