package com.bytex.snamp.webconsole;

import com.bytex.snamp.webconsole.data.api.ExampleService;
import com.bytex.snamp.webconsole.data.auth.AuthenticationEndpoint;
import com.bytex.snamp.webconsole.filter.AuthenticationFilter;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * Sample jersey application
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class JerseyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(ExampleService.class);
        result.add(AuthenticationEndpoint.class);
        result.add(AuthenticationFilter.class);
        return result;
    }

}