package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import javax.management.*;
import java.util.Objects;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.getOID;

/**
 * Represents SNMP attribute accessor.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SnmpAttributeAccessor<V extends Variable> extends AttributeAccessor{
    protected static final class SnmpAttributeException extends InterceptionException{
        private static final long serialVersionUID = -1843462979592005665L;

        public SnmpAttributeException(final Exception e) {
            super(e);
        }
    }

    private final Class<V> variableType;
    private final OID attributeID;
    private final V defaultValue;

    SnmpAttributeAccessor(final MBeanAttributeInfo metadata,
                          final V defaultValue,
                          final Class<V> variableType) {
        super(metadata);
        this.variableType = Objects.requireNonNull(variableType);
        this.attributeID = new OID(getOID(metadata));
        this.defaultValue = defaultValue;
    }

    final V getSnmpValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return variableType.cast(getValue());
    }

    final void setSnmpValue(final V value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        setValue(value);
    }

    final V getDefaultValue(){
        return defaultValue;
    }

    final OID getID(){
        return attributeID;
    }

    protected abstract Object interceptSet(final V value) throws SnmpAttributeException;

    @Override
    protected final Object interceptSet(final Object value) throws InvalidAttributeValueException, SnmpAttributeException {
        final V snmpValue;
        try {
            snmpValue = variableType.cast(value);
        }
        catch (final ClassCastException e){
            throw new InvalidAttributeValueException(e.getMessage());
        }
        return interceptSet(snmpValue);
    }

    @Override
    protected abstract V interceptGet(final Object value) throws SnmpAttributeException;


}
