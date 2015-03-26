package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.adapters.ReadAttributeLogicalOperation;
import com.itworks.snamp.adapters.WriteAttributeLogicalOperation;
import com.itworks.snamp.core.LogicalOperation;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Objects;
import java.util.logging.Level;
import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.OID_PARAM_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MOScalarAccessor<V extends Variable> extends MOScalar<V> implements SnmpAttributeMapping {
    private static final class SnmpReadAttributeLogicalOperation extends ReadAttributeLogicalOperation {
        private SnmpReadAttributeLogicalOperation(final AttributeAccessor accessor,
                                                  final OID oid){
            super(accessor.getName(), accessor.toString(), OID_PARAM_NAME, oid);
        }
    }

    private static final class SnmpWriteAttributeLogicalOperation extends WriteAttributeLogicalOperation {
        private SnmpWriteAttributeLogicalOperation(final AttributeAccessor accessor,
                                                   final OID oid){
            super(accessor.getName(), accessor.toString(), OID_PARAM_NAME, oid);
        }
    }

    private final SnmpAttributeAccessor<V> accessor;

    MOScalarAccessor(final SnmpAttributeAccessor<V> accessor,
                            final V defaultValue) {
        super(accessor.getID(),
                SnmpHelpers.getAccessRestrictions(accessor.getMetadata()),
                defaultValue);
        this.accessor = Objects.requireNonNull(accessor);
        setVolatile(true);
    }

    @Override
    public V getValue() {
        try(final LogicalOperation ignored = new SnmpReadAttributeLogicalOperation(accessor, getOid())) {
            return accessor.getSnmpValue();
        }
        catch (final JMException e) {
            SnmpHelpers.log(Level.WARNING, "Read operation failed for %s attribute. Context: %s",
                    accessor, LogicalOperation.current(), e);
            return accessor.getDefaultValue();
        }
    }

    @Override
    public int setValue(final V value) {
        int result;
        try(final LogicalOperation ignored = new SnmpWriteAttributeLogicalOperation(accessor, getOid())) {
            accessor.setSnmpValue(value);
            result = SnmpConstants.SNMP_ERROR_SUCCESS;
        } catch (final Exception e) {
            SnmpHelpers.log(Level.WARNING, "Writing operation failed for %s attribute. Context: %s",
                    accessor,
                    LogicalOperation.current(),
                    e);
            result = SnmpConstants.SNMP_ERROR_RESOURCE_UNAVAILABLE;
        }
        return result;
    }

    @Override
    public MBeanAttributeInfo getMetadata() {
        return accessor.getMetadata();
    }
}
