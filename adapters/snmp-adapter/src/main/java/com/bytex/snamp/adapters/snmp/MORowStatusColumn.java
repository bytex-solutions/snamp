package com.bytex.snamp.adapters.snmp;

import com.google.common.base.Function;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import javax.management.DescriptorRead;

/**
 * Represents RowStatus column as described in SMIv2 (RFC 1903).
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see <a href="http://www.ietf.org/rfc/rfc1903.txt">RFC 1903</a>
 */
final class MORowStatusColumn extends MONamedColumn {

    /**
     * Represents name of this column.
     */
    static final String NAME = "RowStatus";

    /**
     * Initializes a new RowStatus column.
     * @param columnId The column identifier.
     * @param typeMapper A mapper between SNAMP and SNMP type system. Cannot be {@literal null}.
     */
    MORowStatusColumn(final int columnId,
                      final Function<WellKnownType, SnmpType> typeMapper){
        super(columnId, NAME, WellKnownType.INT, typeMapper, MOAccessImpl.ACCESS_READ_WRITE, false);
    }

    /**
     * Determines whether this column is synthetic and doesn't contain any payload.
     *
     * @return {@literal true}, if this column is synthetic and doesn't contain any payload; otherwise, {@literal false}.
     */
    @Override
    boolean isSynthetic() {
        return true;
    }

    @Override
    TableRowStatus parseCellValue(final Variable value, final DescriptorRead conversionOptions) {
        return TableRowStatus.parse(value.toInt());
    }

    @Override
    Integer32 createCellValue(final Object cell, final DescriptorRead conversionOptions) {
        return TableRowStatus.ACTIVE.toManagedScalarValue();
    }

    static boolean isInstance(final MOMutableColumn<? extends Variable> column){
        return column instanceof MORowStatusColumn;
    }
}
