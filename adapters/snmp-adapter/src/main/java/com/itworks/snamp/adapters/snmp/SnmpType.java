package com.itworks.snamp.adapters.snmp;

import static com.itworks.snamp.internal.ReflectionUtils.wrapReference;

import com.itworks.snamp.connectors.AttributeMetadata;
import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.TimeSpan;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.Variable;

import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import static org.snmp4j.smi.SMIConstants.EXCEPTION_NO_SUCH_OBJECT;

import java.util.*;
import java.util.logging.Level;

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

    /**
     * Initializes a new well-known SNMP value provider.
     * @param mapping
     */
    private SnmpType(final Class<? extends SnmpAttributeMapping> mapping){
        this.mapping = mapping;
        this.syntax = mapping.getAnnotation(MOSyntax.class);
        this.toVariableConverter = null;
        this.fromVariableConverter = null;
    }

    /**
     * Creates a new instance of the SNMP managed object.
     * @param oid OID of the managed object.
     * @param connector The underlying connector that is used to
     * @param timeouts
     * @return
     */
    public SnmpAttributeMapping createManagedObject(final String oid, final AttributeSupport connector, final TimeSpan timeouts){
        try {
            final Constructor<? extends SnmpAttributeMapping> ctor = mapping.getConstructor(String.class, AttributeSupport.class, TimeSpan.class);
            return ctor.newInstance(oid, wrapReference(new WeakReference<>(connector), AttributeSupport.class), timeouts);
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

    public Variable convert(final Object value, final ManagementEntityType valueType, final Map<String, String> options){
        if(toVariableConverter == null)
            try {
                toVariableConverter = mapping.getMethod("convert", Object.class, ManagementEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                try {
                    toVariableConverter = mapping.getMethod("convert", Object.class, ManagementEntityType.class, Map.class);
                } catch (NoSuchMethodException e1) {
                    SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                    return new Null();
                }
            }
        //attempts to invoke the converter.
        try {
            switch (toVariableConverter.getParameterTypes().length){
                case 2: return (Variable)toVariableConverter.invoke(null, new Object[]{value, valueType});
                case 3: return (Variable)toVariableConverter.invoke(null, new Object[]{value, valueType, options});
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
    public Variable convert(final Object value, final ManagementEntityType valueType){
        return convert(value, valueType, Collections.<String, String>emptyMap());
    }

    public Object convert(final Variable value, final ManagementEntityType valueType, final Map<String, String> options){
        if(fromVariableConverter == null)
            try {
                fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagementEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                try {
                    fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagementEntityType.class);
                } catch (final NoSuchMethodException e1) {
                    SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                    return null;
                }

            }
        //attempts to invoke the converter.
        try {
            switch (fromVariableConverter.getParameterTypes().length){
                case 2: return fromVariableConverter.invoke(null, new Object[]{value, valueType});
                case 3: return fromVariableConverter.invoke(null, new Object[]{value, valueType, options});
                default: throw new ReflectiveOperationException("java-to-snmp converter not found.");
            }
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    public Object convert(final Variable value, final ManagementEntityType valueType){
        if(fromVariableConverter == null)
            try {
                fromVariableConverter = mapping.getMethod("convert", Variable.class, ManagementEntityType.class);
            }
            catch (final NoSuchMethodException e) {
                SnmpAttributeMapping.log.log(Level.SEVERE, "Internal error. Call for SNAMP developers.", e);
                return null;
            }
        //attempts to invoke the converter.
        try {
            return fromVariableConverter.invoke(null, new Object[]{value, valueType});
        }
        catch (final ReflectiveOperationException e) {
            SnmpAttributeMapping.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Maps the attribute type to the SNMP-compliant type.
     * @param attributeType
     * @return
     */
    public static SnmpType map(final ManagementEntityType attributeType){
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

    /**
     * Creates a new mapping for the specified attribute.
     * @param connector
     * @param oid
     * @param attributeName
     * @param options
     * @param timeouts
     * @return
     */
    public static SnmpAttributeMapping createManagedObject(final AttributeSupport connector,
                                                    final String oid,
                                                    final String attributeName,
                                                    final Map<String, String> options,
                                                   final TimeSpan timeouts){
        final AttributeMetadata attribute = connector.connectAttribute(oid, attributeName, options);
        if(attribute == null) return null;
        final SnmpType type = map(attribute.getType());
        final SnmpAttributeMapping mapping = type != null ? type.createManagedObject(oid, connector, timeouts) : null;
        return mapping;
    }

    /**
     * Returns a value from {@link org.snmp4j.smi.SMIConstants} that represents the specified attribute type.
     * @param typeInfo
     * @return
     */
    public static int getSyntax(final ManagementEntityType typeInfo){
        return map(typeInfo).getSyntax();
    }
}
