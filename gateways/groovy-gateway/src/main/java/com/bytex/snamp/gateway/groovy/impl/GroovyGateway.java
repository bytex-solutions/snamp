package com.bytex.snamp.gateway.groovy.impl;

import com.google.common.collect.Multimap;
import com.bytex.snamp.gateway.*;
import com.bytex.snamp.gateway.groovy.GatewayInfo;
import com.bytex.snamp.gateway.groovy.GatewayScript;
import com.bytex.snamp.gateway.groovy.GatewayScriptEngine;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.internal.Utils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Represents Groovy Resource Adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyGateway extends AbstractGateway {
    private static final String ADAPTER_INSTANCE_NAME = "adapterInstanceName";
    private final ScriptHolder holder;
    private final ManagementInformationRepository repository;

    /**
     * Initializes a new resource adapter.
     *
     * @param instanceName The name of the adapter instance.
     */
    GroovyGateway(final String instanceName) {
        super(instanceName);
        repository = new ManagementInformationRepository(Utils.getBundleContextOfObject(this));
        holder = new ScriptHolder();
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource extended
     * with the specified feature.
     *
     * @param resourceName The name of the managed resource.
     * @param feature      A new feature of the managed resource.
     * @return A new instance of the resource feature accessor. May be {@literal null}.
     * @see AttributeAccessor
     * @see NotificationAccessor
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName,
                                                                               final M feature) throws Exception {
        if (feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) repository.addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else if (feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) repository.addNotification(resourceName, (MBeanNotificationInfo) feature, holder);
        else return null;
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource
     * was removed from SNAMP.
     *
     * @param resourceName The name of the resource.
     * @return Read-only collection of features tracked by this resource adapter. Cannot be {@literal null}.
     */
    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return repository.clear(resourceName);
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the feature was removed
     * from the specified resource.
     *
     * @param resourceName The name of the managed resource.
     * @param feature      The resource feature that was removed.
     * @return An instance of the feature accessor used by this resource adapter. May be {@literal null}.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if (feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) repository.removeAttribute(resourceName, (MBeanAttributeInfo) feature);
        else if (feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) repository.removeNotification(resourceName, (MBeanNotificationInfo) feature);
        else return null;
    }

    @Override
    protected synchronized void start(final Map<String, String> parameters) throws GroovyAbsentParameterConfigurationException, IOException, ResourceException, ScriptException {
        final GatewayScriptEngine engine = new GatewayScriptEngine(getClass().getClassLoader(),
                Utils.toProperties(parameters),
                GroovyGatewayConfigurationProvider.getScriptPath(parameters));
        engine.setGlobalVariable(ADAPTER_INSTANCE_NAME, getInstanceName());
        engine.setGlobalVariable(GatewayScript.MODEL_GLOBAL_VAR, repository);
        final GatewayScript script = engine.createScript(GroovyGatewayConfigurationProvider.getScriptFile(parameters),
                parameters);
        script.run();
        holder.set(script);
    }

    @Override
    protected synchronized void stop() throws Exception {
        try {
            holder.close();
        } finally {
            repository.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings((AttributeSet<ScriptAttributeAccessor>)repository);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings((NotificationSet<ScriptNotificationAccessor>)repository);
        else return super.getBindings(featureType);
    }

    static Logger getLoggerImpl() {
        return GatewayInfo.getLogger();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }

}
