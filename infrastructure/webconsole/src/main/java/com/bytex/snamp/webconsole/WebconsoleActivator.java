package com.bytex.snamp.webconsole;

import com.bytex.snamp.webconsole.data.api.ExampleService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

/**
 * Web console activator. We reg it via Http Whiteboard
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class WebconsoleActivator implements BundleActivator {

    private ServiceRegistration registration;

    public void start(BundleContext context) throws Exception {
        Hashtable props = new Hashtable();
/*        props.put("osgi.http.whiteboard.servlet.pattern", "/hello");
        props.put("servlet.init.message", "Hello World!");*/

        this.registration = context.registerService(ExampleService.class.getName(), new ExampleService(), props);
    }

    public void stop(BundleContext context) throws Exception {
        this.registration.unregister();
    }
}
