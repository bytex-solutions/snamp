package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.parser.ParseException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class UnrecognizedGaugeTypeException extends ParseException {
    private static final long serialVersionUID = -1968477629567704209L;

    protected UnrecognizedGaugeTypeException(final String actualGauge) {
        super(String.format("Gauge type '%s' cannot be recognized", actualGauge));
    }
}
