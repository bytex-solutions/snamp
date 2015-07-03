package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Function;
import com.itworks.snamp.jmx.WellKnownType;

/**
 * Provides mapping between type system of SNAMP Managemement Information Model and SNMP type system.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpTypeMapper extends Function<WellKnownType, SnmpType> {
    @Override
    SnmpType apply(final WellKnownType type);
}
