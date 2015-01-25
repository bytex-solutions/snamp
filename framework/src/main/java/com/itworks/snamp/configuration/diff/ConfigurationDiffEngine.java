package com.itworks.snamp.configuration.diff;

import com.itworks.snamp.configuration.AbstractAgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

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
                                           final Map<String, ResourceAdapterConfiguration> target,
                                           final Map<String, ResourceAdapterConfiguration> baseline){
        //compute gaps for adapters that should be deleted from baseline config
        for(final String adapterInstanceName: baseline.keySet())
            if(!target.containsKey(adapterInstanceName))
                output.add(new RemoveResourceResourceAdapterPatchImpl(adapterInstanceName, baseline.get(adapterInstanceName)));

        for(final String adapterInstanceName: target.keySet())
            //compute gaps between two resource adapters
            if(baseline.containsKey(adapterInstanceName)){
                final ResourceAdapterConfiguration targetConfig = target.get(adapterInstanceName);
                if(!AbstractAgentConfiguration.equals(targetConfig, baseline.get(adapterInstanceName)))
                    output.add(new UpdateResourceAdapterInstancePatchImpl(adapterInstanceName, targetConfig));
            }
            //compute gaps for adapters that should be added to the baseline config
            else output.add(new AddResourceResourceAdapterPatchIml(adapterInstanceName, target.get(adapterInstanceName)));
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
