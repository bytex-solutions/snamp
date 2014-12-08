package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.internal.Utils;
import org.snmp4j.smi.Variable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
    private static final MethodHandles.Lookup METHOD_LOOKUP = MethodHandles.lookup();
    private final MOSyntax syntax;
    private volatile MethodHandle toVariableConverter;
    private volatile MethodHandle fromVariableConverter;

    private SnmpType(final Class<? extends SnmpAttributeMapping> mapping){
        this.mapping = mapping;
        this.syntax = mapping.getAnnotation(MOSyntax.class);
        this.toVariableConverter = null;
        this.fromVariableConverter = null;
    }

    boolean isScalar(){
        return SnmpScalarObject.class.isAssignableFrom(mapping);
    }

    /**
     * Creates a new instance of the SNMP managed object.
     * @param oid OID of the managed object.
     * @param accessor An object that provides access to the individual management attribute.
     * @return A new mapping between resource attribute and its SNMP representation.
     */
    SnmpAttributeMapping createManagedObject(final String oid, final AttributeAccessor accessor){
        try {
            final MethodHandle ctor = METHOD_LOOKUP.findConstructor(mapping, MethodType.methodType(void.class, String.class, AttributeAccessor.class));
            return (SnmpAttributeMapping)ctor.invoke(oid, accessor);
        }
        catch (final Throwable e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
            return null;
        }
    }

    /**
     * Returns a value from {@link org.snmp4j.smi.SMIConstants} that represents value syntax type.
     * @return The value syntax type.
     */
    int getSyntax(){
        return syntax != null ? syntax.value() : EXCEPTION_NO_SUCH_OBJECT;
    }

    private MethodHandle getToVariableConverter() throws ReflectiveOperationException{
        MethodHandle converter = toVariableConverter;
        if(converter == null)
            synchronized (this){
                converter = toVariableConverter;
                if(converter == null)
                    try {
                        converter = toVariableConverter = METHOD_LOOKUP.unreflect(mapping.getMethod("convert", Object.class, ManagedEntityType.class));
                    }
                    catch (final ReflectiveOperationException e) {
                        converter = toVariableConverter = METHOD_LOOKUP.unreflect(mapping.getMethod("convert", Object.class, ManagedEntityType.class, Map.class));
                    }
            }
        return converter;
    }

    Variable convert(final Object value, final ManagedEntityType valueType, final Map<String, String> options) throws Throwable {
        final MethodHandle converter = getToVariableConverter();
        switch (converter.type().parameterCount()){
            case 2: return Utils.safeCast(converter.invoke(value, valueType), Variable.class);
            case 3: return Utils.safeCast(converter.invoke(value, valueType, options), Variable.class);
            default: throw new ReflectiveOperationException("SnmpAgent: java-to-snmp converter not found.");
        }
    }

    /**
     * Converts the specified value to the SNMP-compliant value.
     * @param value The value to convert.
     * @param valueType The value type.
     * @return SNMP-compliant value.
     */
    Variable convert(final Object value, final ManagedEntityType valueType) throws Throwable {
        return convert(value, valueType, Collections.<String, String>emptyMap());
    }

    private MethodHandle getFromVariableConverter() throws ReflectiveOperationException {
        MethodHandle converter = fromVariableConverter;
        if (converter == null)
            synchronized (this) {
                converter = fromVariableConverter;
                if (converter == null)
                    try {
                        converter = fromVariableConverter =
                                METHOD_LOOKUP.unreflect(mapping.getMethod("convert", Variable.class, ManagedEntityType.class));
                    } catch (final ReflectiveOperationException e) {
                        converter = fromVariableConverter = METHOD_LOOKUP.unreflect(mapping.getMethod("convert", Variable.class, ManagedEntityType.class, Map.class));
                    }
            }
        return converter;
    }

    Object convert(final Variable value, final ManagedEntityType valueType, final Map<String, String> options) throws Throwable {
        final MethodHandle converter = getFromVariableConverter();
        switch (converter.type().parameterCount()){
            case 2: return converter.invoke(value, valueType);
            case 3: return converter.invoke(value, valueType, options);
            default: throw new ReflectiveOperationException("java-to-snmp converter not found.");
        }
    }

    Object convert(final Variable value, final ManagedEntityType valueType) throws Throwable {
        return convert(value, valueType, Collections.<String, String>emptyMap());
    }

    /**
     * Maps the attribute type to the SNMP-compliant type.
     * @param attributeType Resource-specific type of the attribute.
     * @return SNMP-compliant projection of the attribute type.
     */
    static SnmpType map(final ManagedEntityType attributeType){
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
        else if(isTable(attributeType) || isMap(attributeType) || isArray(attributeType))
            return TABLE;
        else return TEXT;
    }
}
