package com.bytex.snamp.testing.gateway.influx;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.function.Supplier;

abstract class InfluxMethodServlet<M extends InfluxMethodMock> extends ServletContainer {
    private static final long serialVersionUID = 4162829052355639348L;

    InfluxMethodServlet(final Supplier<? extends M> methodCtor){
        super(createAppConfig(methodCtor));
    }

    private static Application createAppConfig(final Supplier<? extends InfluxMethodMock> mockFactory){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(mockFactory.get());
        return result;
    }
}
