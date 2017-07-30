package com.bytex.snamp.testing.gateway.influx;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class InfluxPingServlet extends InfluxMethodServlet<InfluxPingMock> {
    static final String CONTEXT = "/ping";
    private static final long serialVersionUID = -4268142273780826274L;

    InfluxPingServlet() {
        super(InfluxPingMock::new);
    }
}
