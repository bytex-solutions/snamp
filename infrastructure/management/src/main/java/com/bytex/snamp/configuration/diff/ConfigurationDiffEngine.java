package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a methods for computing diffs between target and baseline configuration,
 * patching and merging.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ConfigurationDiffEngine {
    private ConfigurationDiffEngine(){

    }

    private static Stream<ConfigurationPatch> computeAdaptersGap(final Map<String, ? extends ResourceAdapterConfiguration> target,
                                           final Map<String, ? extends ResourceAdapterConfiguration> baseline) {
        //compute gaps for adapters that should be deleted from baseline config
        Stream<ConfigurationPatch> result = baseline.entrySet()
                .stream()
                .filter(adapterInstance -> !target.containsKey(adapterInstance.getKey()))
                .map(adapterInstance -> new RemoveResourceAdapterPatchImpl(adapterInstance.getKey(), adapterInstance.getValue()));
        result = Stream.concat(result, target.entrySet().stream().map(adapterInstance -> {
                    //compute gaps between two resource adapters
                    if (baseline.containsKey(adapterInstance.getKey())) {
                        final ResourceAdapterConfiguration targetConfig = adapterInstance.getValue();
                        return targetConfig.equals(baseline.get(adapterInstance.getKey())) ? null : new UpdateResourceAdapterInstancePatchImpl(adapterInstance.getKey(), targetConfig);
                    }
                    //compute gaps for adapters that should be added to the baseline config
                    else
                        return new AddResourceAdapterPatchIml(adapterInstance.getKey(), adapterInstance.getValue());
                }).filter(Objects::nonNull)
        );
        return result;
    }

    private static Stream<ConfigurationPatch> computeResourcesGap(final Map<String, ? extends ManagedResourceConfiguration> target,
                                            final Map<String, ? extends ManagedResourceConfiguration> baseline) {
        //compute gaps for resources that should be deleted from baseline config
        Stream<ConfigurationPatch> result = baseline.entrySet()
                .stream()
                .filter(resource -> !target.containsKey(resource.getKey()))
                .map(resource -> new RemoveManagedResourcePatchImpl(resource.getKey(), resource.getValue()));

        result = Stream.concat(result, target.entrySet().stream().map(resource -> {
                    if (baseline.containsKey(resource.getKey())) {
                        final ManagedResourceConfiguration targetConfig = resource.getValue();
                        return targetConfig.equals(baseline.get(resource.getKey())) ? null : new UpdateManagedResourcePatchImpl(resource.getKey(), targetConfig);
                    } else return new AddManagedResourcePatchImpl(resource.getKey(), resource.getValue());
                }).filter(Objects::nonNull)
        );
        return result;
    }

    private static Stream<ConfigurationPatch> computeThreadPoolGap(final Map<String, ? extends ThreadPoolConfiguration> target,
                                                                   final Map<String, ? extends ThreadPoolConfiguration> baseline){
        Stream<ConfigurationPatch> result = baseline.entrySet()
                .stream()
                .filter(tp -> !target.containsKey(tp.getKey()))
                .map()
    }

    /**
     * Computes the difference between the target configuration and baseline configuration.
     * and target configuration.
     * @param target The source configuration to be compared with the baseline configuration. Cannot be {@literal null}.
     * @param baseline The baseline configuration to modify. Cannot be {@literal null}.
     * @return A stream of gaps.
     */
    public static Stream<ConfigurationPatch> computeGap(final AgentConfiguration target,
                                                        final AgentConfiguration baseline){
        Stream<ConfigurationPatch> result = computeAdaptersGap(
                target.getEntities(ResourceAdapterConfiguration.class),
                baseline.getEntities(ResourceAdapterConfiguration.class));
        result = Stream.concat(result, computeResourcesGap(target.getEntities(ManagedResourceConfiguration.class),
                baseline.getEntities(ManagedResourceConfiguration.class)));
        result = Stream.concat(result, computeThreadPoolGap(target.getEntities(ThreadPoolConfiguration.class),
                baseline.getEntities(ThreadPoolConfiguration.class)));
        return result;
    }

    /**
     * Upgrades the baseline configuration to target configuration.
     * @param target The source configuration to be compared with the baseline configuration. Cannot be {@literal null}.
     * @param baseline The baseline configuration to modify. Cannot be {@literal null}.
     */
    public static void merge(final AgentConfiguration target,
                             final AgentConfiguration baseline) {
        computeGap(target, baseline).forEach(diff -> diff.applyTo(baseline));
    }
}
