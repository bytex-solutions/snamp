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
     * Clones this instance of agent configuration.
     * @return A new clone of this configuration.
     */
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

    private static final void copy(final HostingConfiguration input, final HostingConfiguration output){
        output.setAdapterName(input.getAdapterName());
        final Map<String, String> hostingParams = output.getHostingParams();
        hostingParams.clear();
        hostingParams.putAll(input.getHostingParams());
    }

    private static final void copyAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> input, final Map<String, ManagementTargetConfiguration.AttributeConfiguration> output, final Factory<ManagementTargetConfiguration.AttributeConfiguration> attributeFactory){
        for(final String attributeId: input.keySet()){
            final ManagementTargetConfiguration.AttributeConfiguration inputAttr = input.get(attributeId);
            final ManagementTargetConfiguration.AttributeConfiguration outputAttr = attributeFactory.create();
            outputAttr.setAttributeName(inputAttr.getAttributeName());
            outputAttr.setReadWriteTimeout(inputAttr.getReadWriteTimeout());
            final Map<String, String> additionalElements = outputAttr.getAdditionalElements();
            additionalElements.clear();
            additionalElements.putAll(inputAttr.getAdditionalElements());
            output.put(attributeId, outputAttr);
        }
    }

    private static final void copyTargets(final ManagementTargetConfiguration input, final ManagementTargetConfiguration output){
        output.setConnectionString(input.getConnectionString());
        output.setConnectionType(input.getConnectionType());
        output.setNamespace(input.getNamespace());
        //import additional elements
        final Map<String, String> additionalElements = output.getAdditionalElements();
        additionalElements.clear();
        additionalElements.putAll(input.getAdditionalElements());
        //import attributes
        copyAttributes(input.getAttributes(), output.getAttributes(), new Factory<ManagementTargetConfiguration.AttributeConfiguration>() {
            @Override
            public ManagementTargetConfiguration.AttributeConfiguration create() {
                return output.newAttributeConfiguration();
            }
        });
    }

    private static final void copy(final Map<String, ManagementTargetConfiguration> input, final Map<String, ManagementTargetConfiguration> output, final Factory<ManagementTargetConfiguration> configFactory){
        output.clear();
        for(final String targetName: input.keySet()){
            final ManagementTargetConfiguration outputConfig = configFactory.create();
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
        copy(input.getTargets(), output.getTargets(), new Factory<ManagementTargetConfiguration>() {
            @Override
            public final ManagementTargetConfiguration create() {
                return output.newManagementTargetConfiguration();
            }
        });
    }

    private static boolean paramsAreEqual(final Map<String, String> params1, final Map<String, String> params2){
        if(params1 == params2) return true;
        else if(params1 == null) return params2 == null;
        else if(params2 == null) return false;
        else if(params1.size() == params2.size()){
            for(final String key1: params1.keySet())
                if(Objects.equals(params1.get(key1), params2.get(key1))) continue;
                else return false;
            return true;
        }
        else return false;
    }

    public static boolean equals(final HostingConfiguration obj1, final HostingConfiguration obj2){
        if(obj1 == obj2) return true;
        else if(obj1 == null) return obj2 == null;
        else if(obj2 == null) return false;
        else return Objects.equals(obj1.getAdapterName(), obj2.getAdapterName()) &&
                    paramsAreEqual(obj1.getHostingParams(), obj2.getHostingParams());
    }

    public static boolean equals(final ManagementTargetConfiguration target1, final ManagementTargetConfiguration target2){
        if(target1 == target2) return true;
        else if(target1 == null) return target2 == null;
        else if(target2 == null) return false;
        else return Objects.equals(target1.getNamespace(), target2.getNamespace()) &&
                    paramsAreEqual(target1.getAdditionalElements(), target2.getAdditionalElements()) &&
                    Objects.equals(target1.getConnectionString(), target2.getConnectionString()) &&
                    Objects.equals(target1.getConnectionType(), target2.getConnectionType()) &&
                    attributesAreEqual(target1.getAttributes(), target2.getAttributes()) &&
                    eventsAreEqual(target1.getEvents(), target2.getEvents());
    }

    public static boolean equals(final ManagementTargetConfiguration.EventConfiguration ev1, final ManagementTargetConfiguration.EventConfiguration ev2){
        if(ev1 == ev2) return true;
        else if(ev1 == null) return ev2 == null;
        else if(ev2 == null) return false;
        else return Objects.equals(ev1.getCategory(), ev2.getCategory()) &&
                    paramsAreEqual(ev1.getAdditionalElements(), ev2.getAdditionalElements());
    }

    private static boolean eventsAreEqual(final Map<String, ManagementTargetConfiguration.EventConfiguration> evs1, final Map<String, ManagementTargetConfiguration.EventConfiguration> evs2) {
        if(evs1 == evs2) return true;
        else if(evs1 == null) return evs2 == null;
        else if(evs2 == null) return false;
        else if(evs1.size() == evs2.size()){
            for(final String key1: evs1.keySet())
                if(equals(evs1.get(key1), evs2.get(key1))) continue;
                else return false;
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
    public static boolean equals(final ManagementTargetConfiguration.AttributeConfiguration attr1, final ManagementTargetConfiguration.AttributeConfiguration attr2){
        if(attr1 == attr2) return true;
        else if(attr1 == null) return attr2 == null;
        else if(attr2 == null) return false;
        else return Objects.equals(attr1.getAttributeName(), attr2.getAttributeName()) &&
                    Objects.equals(attr1.getReadWriteTimeout(), attr2.getReadWriteTimeout()) &&
                    paramsAreEqual(attr1.getAdditionalElements(), attr2.getAdditionalElements());
    }

    private static boolean attributesAreEqual(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attrs1, final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attrs2) {
        if(attrs1 == attrs2) return true;
        else if(attrs1 == null) return attrs2 == null;
        else if(attrs2 == null) return false;
        else if(attrs1.size() == attrs2.size()){
            for(final String key1: attrs1.keySet())
                if(equals(attrs1.get(key1), attrs2.get(key1))) continue;
                else return false;
            return true;
        }
        else return false;
    }

    private static boolean targetsAreEqual(final Map<String, ManagementTargetConfiguration> obj1, final Map<String, ManagementTargetConfiguration> obj2){
        if(obj1 == obj2) return true;
        else if(obj1 == null) return obj2 == null;
        else if(obj2 == null) return false;
        else if(obj1.size() == obj2.size()){
            for(final String key1: obj1.keySet())
                if(equals(obj1.get(key1), obj2.get(key1))) continue;
                else return false;
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
        if(obj1 == obj2) return true;
        else if(obj1 == null) return obj2 == null;
        else if(obj2 == null) return false;
        else return equals(obj1.getAgentHostingConfig(), obj2.getAgentHostingConfig()) &&
                    targetsAreEqual(obj1.getTargets(), obj2.getTargets());
    }
}
