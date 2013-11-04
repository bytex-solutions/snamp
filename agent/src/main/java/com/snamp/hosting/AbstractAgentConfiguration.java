package com.snamp.hosting;

import com.snamp.Activator;

import java.util.*;

/**
 * Represents a base class for building agent configuration.
 * @author roman
 */
abstract class AbstractAgentConfiguration implements AgentConfiguration {
    /**
     * Initializes a new empty agent configuration.
     */
    protected AbstractAgentConfiguration(){

    }

    /**
     * Clones this instance of agent configuration.
     * @return
     */
    @Override
    public abstract AbstractAgentConfiguration clone();

    private static final void copy(final HostingConfiguration input, final HostingConfiguration output){
        output.setAdapterName(input.getAdapterName());
        final Map<String, String> hostingParams = output.getHostingParams();
        hostingParams.clear();
        hostingParams.putAll(input.getHostingParams());
    }

    private static final void copyAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> input, final Map<String, ManagementTargetConfiguration.AttributeConfiguration> output, final Activator<ManagementTargetConfiguration.AttributeConfiguration> attributeFactory){
        for(final String attributeId: input.keySet()){
            final ManagementTargetConfiguration.AttributeConfiguration inputAttr = input.get(attributeId);
            final ManagementTargetConfiguration.AttributeConfiguration outputAttr = attributeFactory.newInstance();
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
        copyAttributes(input.getAttributes(), output.getAttributes(), new Activator<ManagementTargetConfiguration.AttributeConfiguration>() {
            @Override
            public ManagementTargetConfiguration.AttributeConfiguration newInstance() {
                return output.newAttributeConfiguration();
            }
        });
    }

    private static final void copy(final Map<String, ManagementTargetConfiguration> input, final Map<String, ManagementTargetConfiguration> output, final Activator<ManagementTargetConfiguration> configFactory){
        output.clear();
        for(final String targetName: input.keySet()){
            final ManagementTargetConfiguration outputConfig = configFactory.newInstance();
            copyTargets(input.get(targetName), outputConfig);
            output.put(targetName, outputConfig);
        }
    }

    /**
     * Imports the state of specified object into this object.
     *
     * @param input
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
        copy(input.getTargets(), output.getTargets(), new Activator<ManagementTargetConfiguration>() {
            @Override
            public final ManagementTargetConfiguration newInstance() {
                return output.newManagementTargetConfiguration();
            }
        });
    }
}
