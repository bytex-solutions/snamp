package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.ManagedEntityType;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.convertFrom;
import static com.itworks.snamp.connectors.ManagedEntityTypeHelper.supportsProjection;
import static org.snmp4j.smi.SMIConstants.SYNTAX_INTEGER32;

/**
 * Represents Integer SNMP mapping,
 */
@MOSyntax(SYNTAX_INTEGER32)
final class SnmpIntegerObject extends SnmpScalarObject<Integer32>{
    public static final int defaultValue = -1;

    public SnmpIntegerObject(final String oid, final AttributeAccessor connector){
        super(oid, connector, new Integer32(defaultValue));
    }

    public static Integer32 convert(final Object value, final ManagedEntityType attributeTypeInfo){
        final Number convertedValue = convertFrom(attributeTypeInfo, value, TypeLiterals.NUMBER, TypeLiterals.BYTE, TypeLiterals.SHORT, TypeLiterals.INTEGER);
        return new Integer32(convertedValue.intValue());
    }

    public static Object convert(final Variable value, final ManagedEntityType attributeTypeInfo){
        if(supportsProjection(attributeTypeInfo, TypeLiterals.LONG)) return value.toLong();
        else if(supportsProjection(attributeTypeInfo, TypeLiterals.SHORT)) return (short) value.toLong();
        else if(supportsProjection(attributeTypeInfo, TypeLiterals.INTEGER)) return value.toInt();
        else if(supportsProjection(attributeTypeInfo, TypeLiterals.LONG)) return (byte) value.toLong();
        else if(supportsProjection(attributeTypeInfo, TypeLiterals.STRING)) return value.toString();
        else return logAndReturnDefaultValue(defaultValue, value, attributeTypeInfo);
    }

    @Override
    protected Integer32 convert(final Object value){
        return convert(value, getMetadata().getType());
    }

    @Override
    protected Object convert(final Integer32 value) {
        return convert(value, getMetadata().getType());
    }
}
