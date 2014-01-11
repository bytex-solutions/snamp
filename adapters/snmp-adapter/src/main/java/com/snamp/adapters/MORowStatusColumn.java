package com.snamp.adapters;

import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

/**
 * Represents RowStatus column as described in SMIv2 (RFC 1903). This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see <a href="http://www.ietf.org/rfc/rfc1903.txt">RFC 1903</a>
 */
final class MORowStatusColumn<V extends Variable> extends MONamedColumn<V> {
    /**
     * Represents name of this column.
     */
    public static final String NAME = "RowStatus";

    /**
     * Initializes a new RowStatus column.
     * @param columnId The column identifier.
     */
    public MORowStatusColumn(final int columnId){
        super(columnId, NAME, SMIConstants.SYNTAX_INTEGER32, MOAccessImpl.ACCESS_READ_WRITE, false);
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
}
