package com.bytex.snamp.adapters.nagios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.AttributeSet;
import com.bytex.snamp.adapters.modeling.FeatureAccessor;
import com.bytex.snamp.EntryReader;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class NagiosAdapter extends AbstractResourceAdapter {
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
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName,
                                                                               final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)service.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return service.clear(resourceName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)service.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
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
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final String servletContext,
                                                                                                    final AttributeSet<NagiosAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result =
                HashMultimap.create();
        attributes.forEachAttribute(new EntryReader<String, NagiosAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final NagiosAttributeAccessor accessor)  {
                return result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor,
                        "path", accessor.getPath(servletContext, resourceName),
                        FeatureBindingInfo.MAPPED_TYPE, "Text"
                ));
            }
        });
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
