package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.parser.ParseException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class IncorrectGaugeOperatorException extends ParseException {
    private static final long serialVersionUID = 1027208531409017755L;

    protected IncorrectGaugeOperatorException(final String gaugeType, final String operator) {
        super(String.format("Operator '%s' cannot be applied to gauge '%s'", operator, gaugeType));
    }
}
