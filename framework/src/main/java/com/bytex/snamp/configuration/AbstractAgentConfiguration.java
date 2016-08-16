package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents a base class for custom agent configuration holders.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
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
        getEntities(GatewayConfiguration.class).clear();
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
    public static void copy(final AttributeConfiguration source, final AttributeConfiguration dest){
        dest.setReadWriteTimeout(source.getReadWriteTimeout());
        dest.setParameters(source.getParameters());
    }

    /**
     * Copies management events.
     * @param source The event to copy.
     * @param dest The event to fill.
     */
    public static void copy(final EventConfiguration source, final EventConfiguration dest){
        dest.setParameters(source.getParameters());
    }

    public static void copy(final OperationConfiguration source, final OperationConfiguration dest){
        dest.setInvocationTimeout(source.getInvocationTimeout());
        dest.setParameters(source.getParameters());
    }

    private static void copyAttributes(final Map<String, ? extends AttributeConfiguration> input,
                                       final EntityMap<? extends AttributeConfiguration> output){
        if(input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends AttributeConfiguration> entry : input.entrySet()) {
                final AttributeConfiguration inputAttr = entry.getValue();
                //factory registers a new attribute in the output collection
                output.consumeOrAdd(inputAttr, entry.getKey(), AbstractAgentConfiguration::copy);
            }
        }
    }

    private static void copyEvents(final Map<String, ? extends EventConfiguration> input,
                                   final EntityMap<? extends EventConfiguration> output) {
        if (input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends EventConfiguration> entry : input.entrySet()) {
                final EventConfiguration inputEv = entry.getValue();
                output.consumeOrAdd(inputEv, entry.getKey(), AbstractAgentConfiguration::copy);
            }
        }
    }

    private static void copyOperations(final Map<String, ? extends OperationConfiguration> input,
                                   final EntityMap<? extends OperationConfiguration> output) {
        if (input != null && output != null) {
            output.clear();
            for (final Map.Entry<String, ? extends OperationConfiguration> entry : input.entrySet()) {
                final OperationConfiguration inputOp = entry.getValue();
                output.consumeOrAdd(inputOp, entry.getKey(), AbstractAgentConfiguration::copy);
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

    public static void copy(final ManagedResourceGroupConfiguration input, final ManagedResourceGroupConfiguration output){
        output.setParameters(input.getParameters());
    }

    public static void copy(final ManagedResourceConfiguration input, final ManagedResourceConfiguration output){
        output.setConnectionString(input.getConnectionString());
        output.setType(input.getType());
        //import additional elements
        output.setParameters(input.getParameters());
        //import managementAttributes
        copyAttributes(input.getFeatures(AttributeConfiguration.class),
                output.getFeatures(AttributeConfiguration.class)
        );
        copyEvents(input.getFeatures(EventConfiguration.class),
                output.getFeatures(EventConfiguration.class)
        );
        copyOperations(input.getFeatures(OperationConfiguration.class),
                output.getFeatures(OperationConfiguration.class)
        );
    }

    public static void copy(final GatewayConfiguration input, final GatewayConfiguration output){
        output.setType(input.getType());
        output.setParameters(input.getParameters());
    }

    private static <T extends EntityConfiguration> void copy(final Map<String, ? extends T> input,
                             final EntityMap<? extends T> output,
                             final ConfigurationEntityCopier<T> copier) {
        output.clear();
        for (final Map.Entry<String, ? extends T> entry : input.entrySet()) {
            final T source = entry.getValue();
            output.consumeOrAdd(source, entry.getKey(), copier::copy);
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
        copy(input.getEntities(GatewayConfiguration.class),
                output.getEntities(GatewayConfiguration.class),
                (ConfigurationEntityCopier<GatewayConfiguration>) AbstractAgentConfiguration::copy);
        //import management targets
        copy(input.getEntities(ManagedResourceConfiguration.class),
                output.getEntities(ManagedResourceConfiguration.class),
                (ConfigurationEntityCopier<ManagedResourceConfiguration>) AbstractAgentConfiguration::copy);
        //import thread pools
        copy(input.getEntities(ThreadPoolConfiguration.class),
                output.getEntities(ThreadPoolConfiguration.class),
                (ConfigurationEntityCopier<ThreadPoolConfiguration>) AbstractAgentConfiguration::copy);
        //import groups
        copy(input.getEntities(ManagedResourceGroupConfiguration.class),
                output.getEntities(ManagedResourceGroupConfiguration.class),
                (ConfigurationEntityCopier<ManagedResourceGroupConfiguration>)AbstractAgentConfiguration::copy);
    }
}
