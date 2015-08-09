package com.bytex.snamp.configuration;

import com.google.common.base.Supplier;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a base class for custom agent configuration holders.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
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
        getManagedResources().clear();
        getResourceAdapters().clear();
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
        dest.setAttributeName(source.getAttributeName());
        dest.setReadWriteTimeout(source.getReadWriteTimeout());
        final Map<String, String> additionalElements = dest.getParameters();
        additionalElements.clear();
        additionalElements.putAll(source.getParameters());
    }

    /**
     * Copies management events.
     * @param source The event to copy.
     * @param dest The event to fill.
     */
    public static void copy(final ManagedResourceConfiguration.EventConfiguration source, final ManagedResourceConfiguration.EventConfiguration dest){
        dest.setCategory(source.getCategory());
        final Map<String, String> additionalElements = dest.getParameters();
        additionalElements.clear();
        additionalElements.putAll(source.getParameters());
    }

    private static void copyAttributes(final Map<String, ManagedResourceConfiguration.AttributeConfiguration> input, final Map<String, ManagedResourceConfiguration.AttributeConfiguration> output, final Supplier<ManagedResourceConfiguration.AttributeConfiguration> attributeFactory){
        if(input != null && output != null)
            for(final Map.Entry<String, ManagedResourceConfiguration.AttributeConfiguration> entry: input.entrySet()){
                final ManagedResourceConfiguration.AttributeConfiguration inputAttr = entry.getValue();
                final ManagedResourceConfiguration.AttributeConfiguration outputAttr = attributeFactory.get();
                copy(inputAttr, outputAttr);
                output.put(entry.getKey(), outputAttr);
            }
    }

    private static void copyEvents(final Map<String, ManagedResourceConfiguration.EventConfiguration> input, final Map<String, ManagedResourceConfiguration.EventConfiguration> output, final Supplier<ManagedResourceConfiguration.EventConfiguration> eventFactory){
        if(input != null && output != null)
            for(final Map.Entry<String, ManagedResourceConfiguration.EventConfiguration> entry: input.entrySet()){
                final ManagedResourceConfiguration.EventConfiguration inputEv = entry.getValue();
                final ManagedResourceConfiguration.EventConfiguration outputEv = eventFactory.get();
                copy(inputEv, outputEv);
                output.put(entry.getKey(), outputEv);
            }
    }

    public static void copy(final ManagedResourceConfiguration input, final ManagedResourceConfiguration output){
        output.setConnectionString(input.getConnectionString());
        output.setConnectionType(input.getConnectionType());
        //import additional elements
        final Map<String, String> additionalElements = output.getParameters();
        additionalElements.clear();
        additionalElements.putAll(input.getParameters());
        //import managementAttributes
        copyAttributes(input.getElements(ManagedResourceConfiguration.AttributeConfiguration.class),
                output.getElements(ManagedResourceConfiguration.AttributeConfiguration.class),
                new Supplier<ManagedResourceConfiguration.AttributeConfiguration>() {
                    @Override
                    public ManagedResourceConfiguration.AttributeConfiguration get() {
                        return output.newElement(ManagedResourceConfiguration.AttributeConfiguration.class);
                    }
                }
        );
        copyEvents(input.getElements(ManagedResourceConfiguration.EventConfiguration.class),
                output.getElements(ManagedResourceConfiguration.EventConfiguration.class),
                new Supplier<ManagedResourceConfiguration.EventConfiguration>() {
                    @Override
                    public ManagedResourceConfiguration.EventConfiguration get() {
                        return output.newElement(ManagedResourceConfiguration.EventConfiguration.class);
                    }
                });
    }

    public static void copy(final ResourceAdapterConfiguration input, final ResourceAdapterConfiguration output){
        output.setAdapterName(input.getAdapterName());
        final Map<String, String> additionalElements = output.getParameters();
        additionalElements.clear();
        additionalElements.putAll(input.getParameters());
    }

    private static <T extends EntityConfiguration> void copy(final Map<String, T> input,
                             final Map<String, T> output,
                             final Supplier<T> entityFactory,
                             final ConfigurationEntityCopier<T> copier){
        output.clear();
        for(final Map.Entry<String, T> entry: input.entrySet()){
            final T source = entry.getValue();
            final T dest = entityFactory.get();
            copier.copy(source, dest);
            output.put(entry.getKey(), dest);
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
    public static void copy(final AgentConfiguration input, final AgentConfiguration output){
        if(input == null || output == null) return;
        //import hosting configuration
        copy(input.getResourceAdapters(), output.getResourceAdapters(),
                new Supplier<ResourceAdapterConfiguration>() {
            @Override
            public ResourceAdapterConfiguration get() {
                return output.newConfigurationEntity(ResourceAdapterConfiguration.class);
            }
        },
        new ConfigurationEntityCopier<ResourceAdapterConfiguration>() {
            @Override
            public void copy(final ResourceAdapterConfiguration input, final ResourceAdapterConfiguration output) {
                AbstractAgentConfiguration.copy(input, output);
            }
        });
        //import management targets
        copy(input.getManagedResources(), output.getManagedResources(),
                new Supplier<ManagedResourceConfiguration>() {
                    @Override
                    public ManagedResourceConfiguration get() {
                        return output.newConfigurationEntity(ManagedResourceConfiguration.class);
                    }
                },
        new ConfigurationEntityCopier<ManagedResourceConfiguration>() {
            @Override
            public void copy(final ManagedResourceConfiguration input, final ManagedResourceConfiguration output) {
                AbstractAgentConfiguration.copy(input, output);
            }
        });

    }

    /**
     * Determines whether the attribute descriptors are structurally equal.
     * @param attr1 The first attribute descriptor to compare.
     * @param attr2 The second attribute descriptor to compare.
     * @return {@literal true}, if both descriptors are structurally equal; otherwise, {@literal false}.
     */
    public static boolean equals(final ManagedResourceConfiguration.AttributeConfiguration attr1, final ManagedResourceConfiguration.AttributeConfiguration attr2){
        return attr1 == attr2 ||
                !(attr1 == null || attr2 == null) &&
                        Objects.equals(attr1.getAttributeName(), attr2.getAttributeName()) &&
                        Objects.equals(attr1.getReadWriteTimeout(), attr2.getReadWriteTimeout()) &&
                        equals(attr1.getParameters(), attr2.getParameters());
    }

    public static boolean equals(final ManagedResourceConfiguration.EventConfiguration event1, final ManagedResourceConfiguration.EventConfiguration event2){
        return event1 == event2 ||
                !(event1 == null || event2 == null) &&
                        Objects.equals(event1.getCategory(), event2.getCategory()) &&
                        equals(event1.getParameters(), event2.getParameters());
    }

    public static boolean equals(final ManagedResourceConfiguration.OperationConfiguration op1, final ManagedResourceConfiguration.OperationConfiguration op2){
        return op1 == op2 ||
                !(op1 == null || op2 == null) &&
                        Objects.equals(op1.getOperationName(), op2.getOperationName()) &&
                        Objects.equals(op1.getInvocationTimeout(), op2.getInvocationTimeout()) &&
                        equals(op1.getParameters(), op2.getParameters());
    }

    private static boolean equals(final Map<String, ?> obj1, final Map<String, ?> obj2){
        if(obj1 == obj2) return true;
        else if(obj1 == null || obj2 == null) return false;
        else if(obj1.size() == obj2.size()){
            for(final Map.Entry<String, ?> entry1: obj1.entrySet())
                if(!Objects.equals(entry1.getValue(), obj2.get(entry1.getKey()))) return false;
            return true;
        }
        else return false;
    }

    /**
     * Determines whether the two configurations are structurally equal.
     * @param obj1 The first configuration to compare.
     * @param obj2 The second configuration to compare.
     * @return {@literal true}, if configurations are structurally equal; otherwise, {@literal false}.
     */
    public static boolean equals(final AgentConfiguration obj1, final AgentConfiguration obj2){
        return obj1 == obj2 ||
                !(obj1 == null || obj2 == null) &&
                        equals(obj1.getResourceAdapters(), obj2.getResourceAdapters()) &&
                        equals(obj1.getManagedResources(), obj2.getManagedResources());
    }

    public static boolean equals(final ResourceAdapterConfiguration adapter1, final ResourceAdapterConfiguration adapter2){
        if(adapter1 == null) return adapter2 == null;
        else
            return adapter2 != null &&
                    Objects.equals(adapter1.getAdapterName(), adapter2.getAdapterName()) &&
                    equals(adapter1.getParameters(), adapter2.getParameters());
    }

    public static boolean equals(final ManagedResourceConfiguration resource1, final ManagedResourceConfiguration resource2){
        if(resource1 == null) return resource2 == null;
        else return resource2 != null &&
                Objects.equals(resource1.getConnectionString(),  resource2.getConnectionString()) &&
                Objects.equals(resource1.getConnectionType(), resource2.getConnectionType()) &&
                Objects.equals(resource1.getElements(ManagedResourceConfiguration.AttributeConfiguration.class), resource2.getElements(ManagedResourceConfiguration.AttributeConfiguration.class)) &&
                Objects.equals(resource1.getElements(ManagedResourceConfiguration.EventConfiguration.class), resource2.getElements(ManagedResourceConfiguration.EventConfiguration.class)) &&
                Objects.equals(resource1.getElements(ManagedResourceConfiguration.OperationConfiguration.class), resource2.getElements(ManagedResourceConfiguration.OperationConfiguration.class)) &&
                equals(resource1.getParameters(), resource2.getParameters());
    }
}
