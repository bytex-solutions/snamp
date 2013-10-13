package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import static com.snamp.connectors.AttributePrimitiveTypeBuilder.*;
import org.snmp4j.agent.*;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Represents SNMP managed object factory.
 * @author roman
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

    TABLE(SnmpTableObject.class);

    private final Class<? extends SnmpAttributeMapping> mapping;

    /**
     * Initializes a new well-known SNMP value provider.
     * @param mapping
     */
    private SnmpType(final Class<? extends SnmpAttributeMapping> mapping){
        this.mapping = mapping;
    }

    /**
     * Creates a new instance of the SNMP managed object.
     * @param oid OID of the managed object.
     * @param connector The underlying connector that is used to
     * @param timeouts
     * @return
     */
    public SnmpAttributeMapping createManagedObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        try {
            final Constructor<? extends SnmpAttributeMapping> ctor = mapping.getConstructor(String.class, ManagementConnector.class, TimeSpan.class);
            return ctor.newInstance(oid, connector, timeouts);
        }
        catch (final ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Maps the attribute type to the SNMP-compliant type.
     * @param attributeType
     * @return
     */
    public static SnmpType map(final AttributeTypeInfo attributeType){
        if(isBoolean(attributeType))
            return BOOLEAN;
        else if(isInt8(attributeType) || isInt16(attributeType) || isInt32(attributeType))
            return INTEGER;
        else if(isInt64(attributeType))
            return LONG;
        else if(isFloat(attributeType) || isDouble(attributeType))
            return FLOAT;
        else if(isInteger(attributeType) || isDecimal(attributeType))
            return NUMBER;
        else if(isUnixTime(attributeType))
            return UNIX_TIME;
        else if(attributeType instanceof AttributeTabularType || attributeType instanceof AttributeDictionaryType)
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
    public static SnmpAttributeMapping createManagedObject(final ManagementConnector connector,
                                                    final String oid,
                                                    final String attributeName,
                                                    final Map<String, String> options,
                                                    final TimeSpan timeouts){
        final AttributeMetadata attribute = connector.connectAttribute(oid, attributeName, options);
        if(attribute == null) return null;
        final SnmpType type = map(attribute.getAttributeType());
        return type != null ? type.createManagedObject(oid, connector, timeouts) : null;
    }
}
