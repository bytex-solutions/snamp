package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.connectors.ManagedEntityTypeBuilder;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.util.Collections;
import java.util.Map;

/**
 * Represents SNMP scalar type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpManagedEntityScalarType<V extends Variable> extends ManagedEntityTypeBuilder.AbstractManagedEntityType implements SnmpManagedEntityType {
    /**
     * Represents type of the wrapper for SNMP scalar type.
     */
    public final Class<V> snmpWrapperType;
    private final int syntax;

    protected SnmpManagedEntityScalarType(final Class<V> wrapperType, final int syntax, final SMITypeProjection<V, ?>... converters){
        super(converters);
        this.snmpWrapperType = wrapperType;
        this.syntax = syntax;
    }

    protected final InvalidSnmpValueException createInvalidValueException(final Object source){
        return new InvalidSnmpValueException(source, syntax);
    }

    protected abstract V convertToScalar(final Object value) throws InvalidSnmpValueException;

    @Override
    public final Map<OID, Variable> convertToSnmp(final Object value) throws InvalidSnmpValueException{
        return Collections.<OID, Variable>singletonMap(new OID(), convertToScalar(value));
    }
}
