package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.adapters.FeatureAccessor;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosAdapter extends AbstractResourceAdapter {
    static final String NAME = "nagios";
    private final NagiosActiveCheckService service;
    private final HttpService publisher;

    NagiosAdapter(final String instanceName,
                  final HttpService servletPublisher) {
        super(instanceName);
        service = new NagiosActiveCheckService();
        publisher = Objects.requireNonNull(servletPublisher);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName,
                                                                               final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)service.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) {
        return service.clear(resourceName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)service.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    private String getServletContext(){
        final String SERVLET_CONTEXT = "/snamp/adapters/nagios/%s";
        return String.format(SERVLET_CONTEXT, getInstanceName());
    }

    @Override
    protected void start(final Map<String, String> parameters) throws ServletException, NamespaceException {
        publisher.registerServlet(getServletContext(), new NagiosServlet(service), null, null);
    }

    @Override
    protected void stop() throws Exception {
        publisher.unregister(getServletContext());
        service.clear();
        System.gc();
    }

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
