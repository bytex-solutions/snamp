package com.snamp.adapters;

import com.snamp.connectors.ManagementEntityType;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import java.util.Map;

/**
 * Represents RowStatus column as described in SMIv2 (RFC 1903). This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see <a href="http://www.ietf.org/rfc/rfc1903.txt">RFC 1903</a>
 */
final class MORowStatusColumn extends MONamedColumn<Integer32> {
    /**
     * Represents name of this column.
     */
    public static final String NAME = "RowStatus";

    /**
     * Initializes a new RowStatus column.
     * @param columnId The column identifier.
     */
    public MORowStatusColumn(final int columnId){
        super(columnId, NAME, SnmpType.INTEGER, MOAccessImpl.ACCESS_READ_WRITE, false);
    }

    /**
     * Determines whether this column is synthetic and doesn't contain any payload.
     *
     * @return {@literal true}, if this column is synthetic and doesn't contain any payload; otherwise, {@literal false}.
     */
    @Override
    public final boolean isSynthetic() {
        return true;
    }

    @Override
    public final Object parseCellValue(final Integer32 value, final ManagementEntityType ct, final Map<String, String> conversionOptions) {
        return TableRowStatus.parse(value.toInt());
    }

    @Override
    public final Integer32 createCellValue(final Object cell, final ManagementEntityType ct, final Map<String, String> conversionOptions) {
        return TableRowStatus.ACTIVE.toManagedScalarValue();
    }

    public static boolean isInstance(final MOMutableColumn<? extends Variable> column){
        return column instanceof MORowStatusColumn;
    }

    public static MORowStatusColumn cast(final MONamedColumn<? extends Variable> column) {
        return (MORowStatusColumn)column;
    }
}
