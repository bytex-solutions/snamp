package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.jmx.WellKnownType;

import java.util.function.Function;

/**
 * Provides mapping between type system of SNAMP Managemement Information Model and SNMP type system.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@FunctionalInterface
interface SnmpTypeMapper extends Function<WellKnownType, SnmpType> {
    @Override
    SnmpType apply(final WellKnownType type);
}
