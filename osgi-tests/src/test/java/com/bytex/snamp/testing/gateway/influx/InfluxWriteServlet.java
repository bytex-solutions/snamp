package com.bytex.snamp.testing.gateway.influx;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class InfluxWriteServlet extends InfluxMethodServlet<InfluxWriteMock> {
    static final String CONTEXT = "/write";
    private static final long serialVersionUID = -6716218359254986868L;

    InfluxWriteServlet(){
        super(InfluxWriteMock::new);
    }
}
