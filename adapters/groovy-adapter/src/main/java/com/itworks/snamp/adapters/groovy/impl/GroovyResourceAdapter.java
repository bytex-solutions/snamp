package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.groovy.ResourceAdapterInfo;
import com.itworks.snamp.adapters.groovy.ResourceAdapterScript;
import com.itworks.snamp.adapters.groovy.ResourceAdapterScriptEngine;
import com.itworks.snamp.internal.Utils;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents Groovy Resource Adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = ResourceAdapterInfo.NAME;
    private static final String ADAPTER_INSTANCE_NAME = "adapterInstanceName";
    private final ScriptHolder holder;
    private final ManagementInformationRepository repository;

    /**
     * Initializes a new resource adapter.
     *
     * @param instanceName The name of the adapter instance.
     */
    GroovyResourceAdapter(final String instanceName) {
        super(instanceName);
        repository = new ManagementInformationRepository(Utils.getBundleContextByObject(this));
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
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName,
                                                                               final M feature) throws Exception {
        if (feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>) repository.addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>) repository.addNotification(resourceName, (MBeanNotificationInfo) feature, holder);
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
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws Exception {
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
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)repository.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>)repository.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected synchronized void start(final Map<String, String> parameters) throws GroovyAbsentParameterConfigurationException, IOException, ResourceException, ScriptException {
        final ResourceAdapterScriptEngine engine = new ResourceAdapterScriptEngine(getClass().getClassLoader(),
                Utils.toProperties(parameters),
                GroovyResourceAdapterConfigurationProvider.getScriptPath(parameters));
        engine.setGlobalVariable(ADAPTER_INSTANCE_NAME, getInstanceName());
        engine.setGlobalVariable(ResourceAdapterScript.MODEL_GLOBAL_VAR, repository);
        final ResourceAdapterScript script = engine.createScript(GroovyResourceAdapterConfigurationProvider.getScriptFile(parameters),
                parameters);
        script.run();
        holder.set(script);
    }

    @Override
    protected synchronized void stop() throws Exception {
        try {
            holder.close();
        }
        finally {
            repository.clear();
        }
    }

    static Logger getLoggerImpl(){
        return ResourceAdapterInfo.getLogger();
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
