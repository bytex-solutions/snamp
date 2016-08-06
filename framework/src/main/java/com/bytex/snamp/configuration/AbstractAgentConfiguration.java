package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents a base class for custom agent configuration holders.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public abstract class AbstractAgentConfiguration implements AgentConfiguration {
    private interface ConfigurationEntityCopier<T extends EntityConfiguration>{
        void copy(final T input, final T output);
    }

    /**
     * Initializes a new empty agent configuration.
     */
    protected AbstractAgentConfiguration(){

    }

    /**
     * Clears this configuration.
     */
    @Override
    public void clear() {
        getEntities(ManagedResourceConfiguration.class).clear();
        getEntities(ResourceAdapterConfiguration.class).clear();
    }

    /**
     * Clones this instance of agent configuration.
     * @return A new clone of this configuration.
     */
    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException",
            "CloneDoesntCallSuperClone"})
    @Override
    public AbstractAgentConfiguration clone(){
        try {
            final AbstractAgentConfiguration newInstance = getClass().newInstance();
            newInstance.load(this);
            return newInstance;
        }
        catch (final ReflectiveOperationException e){
            return this;
        }
    }

    /**
     * Copies management attributes.
     * @param source The attribute to copy.
     * @param dest The attribute to fill.
     */
    public static void copy(final ManagedResourceConfiguration.AttributeConfiguration source, final ManagedResourceConfiguration.AttributeConfiguration dest){
        dest.setReadWriteTimeout(source.getReadWriteTimeout());
        dest.setParameters(source.getParameters());
    }

    /**
     * Copies management events.
     * @param source The event to copy.
     * @param dest The event to fill.
     */
    public static void copy(final ManagedResourceConfiguration.EventConfiguration source, final ManagedResourceConfiguration.EventConfiguration dest){
        dest.setParameters(source.getParameters());
    }

    public static void copy(final ManagedResourceConfiguration.OperationConfiguration source, final ManagedResourceConfiguration.OperationConfiguration dest){
        dest.setInvocationTimeout(source.getInvocationTimeout());
        dest.setParameters(source.getParameters());
    }

    private static void copyAttributes(final Map<String, ? extends ManagedResourceConfiguration.AttributeConfiguration> input,
                                       final EntityMap<? extends ManagedResourceConfiguration.AttributeConfiguration> output){
        if(input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends ManagedResourceConfiguration.AttributeConfiguration> entry : input.entrySet()) {
                final ManagedResourceConfiguration.AttributeConfiguration inputAttr = entry.getValue();
                //factory registers a new attribute in the output collection
                final ManagedResourceConfiguration.AttributeConfiguration outputAttr = output.getOrAdd(entry.getKey());
                copy(inputAttr, outputAttr);
            }
        }
    }

    private static void copyEvents(final Map<String, ? extends ManagedResourceConfiguration.EventConfiguration> input,
                                   final EntityMap<? extends ManagedResourceConfiguration.EventConfiguration> output) {
        if (input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends ManagedResourceConfiguration.EventConfiguration> entry : input.entrySet()) {
                final ManagedResourceConfiguration.EventConfiguration inputEv = entry.getValue();
                final ManagedResourceConfiguration.EventConfiguration outputEv = output.getOrAdd(entry.getKey());
                copy(inputEv, outputEv);
            }
        }
    }

    private static void copyOperations(final Map<String, ? extends ManagedResourceConfiguration.OperationConfiguration> input,
                                   final EntityMap<? extends ManagedResourceConfiguration.OperationConfiguration> output) {
        if (input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends ManagedResourceConfiguration.OperationConfiguration> entry : input.entrySet()) {
                final ManagedResourceConfiguration.OperationConfiguration inputOp = entry.getValue();
                final ManagedResourceConfiguration.OperationConfiguration outputOp = output.getOrAdd(entry.getKey());
                copy(inputOp, outputOp);
            }
        }
    }

    public static void copy(final ThreadPoolConfiguration input, final ThreadPoolConfiguration output){
        output.setParameters(input.getParameters());
        output.setQueueSize(input.getQueueSize());
        output.setKeepAliveTime(input.getKeepAliveTime());
        output.setMaxPoolSize(input.getMaxPoolSize());
        output.setMinPoolSize(input.getMinPoolSize());
        output.setThreadPriority(input.getThreadPriority());
    }

    public static void copy(final ManagedResourceConfiguration input, final ManagedResourceConfiguration output){
        output.setConnectionString(input.getConnectionString());
        output.setConnectionType(input.getConnectionType());
        //import additional elements
        output.setParameters(input.getParameters());
        //import managementAttributes
        copyAttributes(input.getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class),
                output.getFeatures(ManagedResourceConfiguration.AttributeConfiguration.class)
        );
        copyEvents(input.getFeatures(ManagedResourceConfiguration.EventConfiguration.class),
                output.getFeatures(ManagedResourceConfiguration.EventConfiguration.class)
        );
        copyOperations(input.getFeatures(ManagedResourceConfiguration.OperationConfiguration.class),
                output.getFeatures(ManagedResourceConfiguration.OperationConfiguration.class)
        );
    }

    public static void copy(final ResourceAdapterConfiguration input, final ResourceAdapterConfiguration output){
        output.setAdapterName(input.getAdapterName());
        output.setParameters(input.getParameters());
    }

    private static <T extends EntityConfiguration> void copy(final Map<String, ? extends T> input,
                             final EntityMap<? extends T> output,
                             final ConfigurationEntityCopier<T> copier) {
        output.clear();
        for (final Map.Entry<String, ? extends T> entry : input.entrySet()) {
            final T source = entry.getValue();
            final T dest = output.getOrAdd(entry.getKey());
            copier.copy(source, dest);
        }
    }

    /**
     * Imports the state of specified object into this object.
     *
     * @param input An input configuration which internal state should be copied into this instance.
     */
    @Override
    public final void load(final AgentConfiguration input) {
        copy(input, this);
    }

    /**
     * Copies configuration from one object to another object.
     * @param input The configuration import source.
     * @param output The configuration import destination.
     */
    public static void copy(final AgentConfiguration input, final AgentConfiguration output) {
        if (input == null || output == null) return;
        //import hosting configuration
        copy(input.getEntities(ResourceAdapterConfiguration.class),
                output.getEntities(ResourceAdapterConfiguration.class),
                (ConfigurationEntityCopier<ResourceAdapterConfiguration>) AbstractAgentConfiguration::copy);
        //import management targets
        copy(input.getEntities(ManagedResourceConfiguration.class),
                output.getEntities(ManagedResourceConfiguration.class),
                (ConfigurationEntityCopier<ManagedResourceConfiguration>) AbstractAgentConfiguration::copy);
        //import thread pools
        copy(input.getEntities(ThreadPoolConfiguration.class),
                output.getEntities(ThreadPoolConfiguration.class),
                (ConfigurationEntityCopier<ThreadPoolConfiguration>) AbstractAgentConfiguration::copy);
    }
}
