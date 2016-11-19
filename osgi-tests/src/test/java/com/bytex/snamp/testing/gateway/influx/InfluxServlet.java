package com.bytex.snamp.testing.gateway.influx;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

final class InfluxServlet extends ServletContainer {
    private static final long serialVersionUID = 5710139261115306229L;

    InfluxServlet(){
        super(createAppConfig());
    }

    private static Application createAppConfig(){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new InfluxDBMock());
        return result;
    }
}
