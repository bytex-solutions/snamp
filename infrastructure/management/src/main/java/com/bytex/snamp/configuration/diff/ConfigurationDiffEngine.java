package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.*;

/**
 * Provides a methods for computing diffs between target and baseline configuration,
 * patching and merging.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationDiffEngine {
    private ConfigurationDiffEngine(){

    }

    private static void computeAdaptersGap(final Collection<ConfigurationPatch> output,
                                           final Map<String, ? extends ResourceAdapterConfiguration> target,
                                           final Map<String, ? extends ResourceAdapterConfiguration> baseline){
        //compute gaps for adapters that should be deleted from baseline config
        for(final Map.Entry<String, ? extends ResourceAdapterConfiguration> adapterInstance: baseline.entrySet())
            if(!target.containsKey(adapterInstance.getKey()))
                output.add(new RemoveResourceAdapterPatchImpl(adapterInstance.getKey(), adapterInstance.getValue()));

        for(final Map.Entry<String, ? extends ResourceAdapterConfiguration> adapterInstance: target.entrySet())
            //compute gaps between two resource adapters
            if(baseline.containsKey(adapterInstance.getKey())){
                final ResourceAdapterConfiguration targetConfig = adapterInstance.getValue();
                if(!AbstractAgentConfiguration.equals(targetConfig, baseline.get(adapterInstance.getKey())))
                    output.add(new UpdateResourceAdapterInstancePatchImpl(adapterInstance.getKey(), targetConfig));
            }
            //compute gaps for adapters that should be added to the baseline config
            else output.add(new AddResourceAdapterPatchIml(adapterInstance.getKey(), adapterInstance.getValue()));
    }

    private static void computeResourcesGap(final Collection<ConfigurationPatch> output,
                                            final Map<String, ? extends ManagedResourceConfiguration> target,
                                            final Map<String, ? extends ManagedResourceConfiguration> baseline) {
        //compute gaps for resources that should be deleted from baseline config
        for (final Map.Entry<String, ? extends ManagedResourceConfiguration> resource : baseline.entrySet())
            if (!target.containsKey(resource.getKey()))
                output.add(new RemoveManagedResourcePatchImpl(resource.getKey(), resource.getValue()));

        for(final Map.Entry<String, ? extends ManagedResourceConfiguration> resource : target.entrySet())
            if(baseline.containsKey(resource.getKey())){
                final ManagedResourceConfiguration targetConfig = resource.getValue();
                if(!AbstractAgentConfiguration.equals(targetConfig, baseline.get(resource.getKey())))
                    output.add(new UpdateManagedResourcePatchImpl(resource.getKey(), targetConfig));
            }
            else output.add(new AddManagedResourcePatchImpl(resource.getKey(), resource.getValue()));
    }

    /**
     * Computes the difference between the target configuration and baseline configuration.
     * and target configuration.
     * @param target The source configuration to be compared with the baseline configuration. Cannot be {@literal null}.
     * @param baseline The baseline configuration to modify. Cannot be {@literal null}.
     * @return A collection of gaps.
     */
    public static Collection<ConfigurationPatch> computeGap(final AgentConfiguration target,
                                                final AgentConfiguration baseline){
        final Collection<ConfigurationPatch> result = new LinkedList<>();
        computeAdaptersGap(result,
                target.getResourceAdapters(),
                baseline.getResourceAdapters());
        computeResourcesGap(result,
                target.getManagedResources(),
                baseline.getManagedResources());
        return result;
    }

    /**
     * Upgrades the baseline configuration to target configuration.
     * @param target The source configuration to be compared with the baseline configuration. Cannot be {@literal null}.
     * @param baseline The baseline configuration to modify. Cannot be {@literal null}.
     * @return A number of changes applied to the target configuration.
     */
    public static int merge(final AgentConfiguration target,
                             final AgentConfiguration baseline){
        final Collection<ConfigurationPatch> diffs = computeGap(target, baseline);
        for(final ConfigurationPatch diff: diffs)
            diff.applyTo(baseline);
        return diffs.size();
    }
}
