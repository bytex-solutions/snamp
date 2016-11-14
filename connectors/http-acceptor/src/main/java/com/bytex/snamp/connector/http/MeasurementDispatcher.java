package com.bytex.snamp.connector.http;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.instrumentation.Measurement;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface MeasurementDispatcher extends Acceptor<Measurement, DispatchException>{
    @Override
    void accept(final Measurement measurement) throws DispatchException;
}
