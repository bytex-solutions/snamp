package com.bytex.snamp.gateway.nagios;

import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NagiosGateway extends AbstractGateway {
    private final NagiosActiveCheckService service;
    private final HttpService publisher;

    NagiosGateway(final String instanceName,
                  @Nonnull final HttpService servletPublisher) {
        super(instanceName);
        service = new NagiosActiveCheckService();
        publisher = servletPublisher;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName,
                                                                               final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)service.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return service.clear(resourceName).stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)service.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    private String getServletContext(){
        final String SERVLET_CONTEXT = "/snamp/gateway/nagios/%s";
        return String.format(SERVLET_CONTEXT, instanceName);
    }

    @Override
    protected void start(final Map<String, String> parameters) throws ServletException, NamespaceException {
        publisher.registerServlet(getServletContext(), new NagiosServlet(service), null, null);
    }

    @Override
    protected void stop() throws Exception {
        publisher.unregister(getServletContext());
        service.clear();
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final String servletContext,
                                                                                                    final AttributeSet<NagiosAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result =
                HashMultimap.create();
        attributes.forEachAttribute( (resourceName, accessor) -> result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                "path", accessor.getPath(servletContext, resourceName),
                FeatureBindingInfo.MAPPED_TYPE, "Text"
        )));
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if (featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>) getAttributes(getServletContext(), service);
        return super.getBindings(featureType);
    }
}
