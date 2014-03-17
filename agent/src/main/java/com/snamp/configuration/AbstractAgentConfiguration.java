package com.snamp.configuration;

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
    public abstract AbstractAgentConfiguration clone();

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
    public static final void copy(final AgentConfiguration input, final AgentConfiguration output){
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
}
