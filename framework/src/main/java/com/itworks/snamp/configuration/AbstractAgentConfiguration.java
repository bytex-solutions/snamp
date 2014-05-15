package com.itworks.snamp.configuration;

import org.apache.commons.collections4.Factory;

import java.util.*;

/**
 * Represents a base class for custom agent configuration holders.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAgentConfiguration implements AgentConfiguration {
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
        getAgentHostingConfig().setAdapterName("");
        getAgentHostingConfig().getHostingParams().clear();
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

    private static void copy(final HostingConfiguration input, final HostingConfiguration output){
        output.setAdapterName(input.getAdapterName());
        final Map<String, String> hostingParams = output.getHostingParams();
        hostingParams.clear();
        hostingParams.putAll(input.getHostingParams());
    }

    private static void copyAttributes(final Map<String, ManagedResourceConfiguration.AttributeConfiguration> input, final Map<String, ManagedResourceConfiguration.AttributeConfiguration> output, final Factory<ManagedResourceConfiguration.AttributeConfiguration> attributeFactory){
        if(input != null && output != null)
            for(final String attributeId: input.keySet()){
                final ManagedResourceConfiguration.AttributeConfiguration inputAttr = input.get(attributeId);
                final ManagedResourceConfiguration.AttributeConfiguration outputAttr = attributeFactory.create();
                outputAttr.setAttributeName(inputAttr.getAttributeName());
                outputAttr.setReadWriteTimeout(inputAttr.getReadWriteTimeout());
                final Map<String, String> additionalElements = outputAttr.getParameters();
                additionalElements.clear();
                additionalElements.putAll(inputAttr.getParameters());
                output.put(attributeId, outputAttr);
            }
    }

    private static void copyTargets(final ManagedResourceConfiguration input, final ManagedResourceConfiguration output){
        output.setConnectionString(input.getConnectionString());
        output.setConnectionType(input.getConnectionType());
        output.setNamespace(input.getNamespace());
        //import additional elements
        final Map<String, String> additionalElements = output.getParameters();
        additionalElements.clear();
        additionalElements.putAll(input.getParameters());
        //import managementAttributes
        copyAttributes(input.getElements(ManagedResourceConfiguration.AttributeConfiguration.class),
                output.getElements(ManagedResourceConfiguration.AttributeConfiguration.class),
                new Factory<ManagedResourceConfiguration.AttributeConfiguration>() {
            @Override
            public ManagedResourceConfiguration.AttributeConfiguration create() {
                return output.newElement(ManagedResourceConfiguration.AttributeConfiguration.class);
            }
        });
    }

    private static void copy(final Map<String, ManagedResourceConfiguration> input, final Map<String, ManagedResourceConfiguration> output, final Factory<ManagedResourceConfiguration> configFactory){
        output.clear();
        for(final String targetName: input.keySet()){
            final ManagedResourceConfiguration outputConfig = configFactory.create();
            copyTargets(input.get(targetName), outputConfig);
            output.put(targetName, outputConfig);
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
        copy(input.getAgentHostingConfig(), output.getAgentHostingConfig());
        //import management targets
        copy(input.getManagedResources(), output.getManagedResources(), new Factory<ManagedResourceConfiguration>() {
            @Override
            public final ManagedResourceConfiguration create() {
                return output.newManagedResourceConfiguration();
            }
        });
    }

    private static boolean paramsAreEqual(final Map<String, String> params1, final Map<String, String> params2){
        if(params1 == params2) return true;
        else if(params1 == null || params2 == null) return false;
        else if(params1.size() == params2.size()){
            for(final String key1: params1.keySet())
                if(!Objects.equals(params1.get(key1), params2.get(key1))) return false;
            return true;
        }
        else return false;
    }

    public static boolean equals(final HostingConfiguration obj1, final HostingConfiguration obj2){
        return obj1 == obj2 ||
                !(obj1 == null || obj2 == null) &&
                        Objects.equals(obj1.getAdapterName(), obj2.getAdapterName()) &&
                        paramsAreEqual(obj1.getHostingParams(), obj2.getHostingParams());
    }

    public static boolean equals(final ManagedResourceConfiguration target1, final ManagedResourceConfiguration target2){
        return target1 == target2 ||
                !(target1 == null || target2 == null) &&
                        Objects.equals(target1.getNamespace(), target2.getNamespace()) &&
                        paramsAreEqual(target1.getParameters(), target2.getParameters()) &&
                        Objects.equals(target1.getConnectionString(), target2.getConnectionString()) &&
                        Objects.equals(target1.getConnectionType(), target2.getConnectionType()) &&
                        attributesAreEqual(target1.getElements(ManagedResourceConfiguration.AttributeConfiguration.class), target2.getElements(ManagedResourceConfiguration.AttributeConfiguration.class)) &&
                        eventsAreEqual(target1.getElements(ManagedResourceConfiguration.EventConfiguration.class), target2.getElements(ManagedResourceConfiguration.EventConfiguration.class));
    }

    public static boolean equals(final ManagedResourceConfiguration.EventConfiguration ev1, final ManagedResourceConfiguration.EventConfiguration ev2){
        return ev1 == ev2 ||
                !(ev1 == null || ev2 == null) &&
                        Objects.equals(ev1.getCategory(), ev2.getCategory()) &&
                        paramsAreEqual(ev1.getParameters(), ev2.getParameters());
    }

    private static boolean eventsAreEqual(final Map<String, ManagedResourceConfiguration.EventConfiguration> evs1, final Map<String, ManagedResourceConfiguration.EventConfiguration> evs2) {
        if(evs1 == evs2) return true;
        else if(evs1 == null || evs2 == null) return false;
        else if(evs1.size() == evs2.size()){
            for(final String key1: evs1.keySet())
                if(!equals(evs1.get(key1), evs2.get(key1))) return false;
            return true;
        }
        else return false;
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
                        paramsAreEqual(attr1.getParameters(), attr2.getParameters());
    }

    private static boolean attributesAreEqual(final Map<String, ManagedResourceConfiguration.AttributeConfiguration> attrs1, final Map<String, ManagedResourceConfiguration.AttributeConfiguration> attrs2) {
        if(attrs1 == attrs2) return true;
        else if(attrs1 == null || attrs2 == null) return attrs2 == null;
        else if(attrs1.size() == attrs2.size()){
            for(final String key1: attrs1.keySet())
                if(!equals(attrs1.get(key1), attrs2.get(key1))) return false;
            return true;
        }
        else return false;
    }

    private static boolean targetsAreEqual(final Map<String, ManagedResourceConfiguration> obj1, final Map<String, ManagedResourceConfiguration> obj2){
        if(obj1 == obj2) return true;
        else if(obj1 == null || obj2 == null) return false;
        else if(obj1.size() == obj2.size()){
            for(final String key1: obj1.keySet())
                if(!equals(obj1.get(key1), obj2.get(key1))) return false;
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
                        equals(obj1.getAgentHostingConfig(), obj2.getAgentHostingConfig()) &&
                        targetsAreEqual(obj1.getManagedResources(), obj2.getManagedResources());
    }
}
