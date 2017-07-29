package com.bytex.snamp.testing.gateway.influx;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class InfluxQueryServlet extends InfluxMethodServlet<InfluxQueryMock> {
    static final String CONTEXT = "/query";
    private static final long serialVersionUID = -7227833415693320126L;

    InfluxQueryServlet(){
        super(InfluxQueryMock::new);
    }
}
