package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Provides a methods for computing diffs between target and baseline configuration,
 * patching and merging.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ConfigurationDiffEngine {
    private ConfigurationDiffEngine(){

    }

    private static Stream<ConfigurationPatch> computeGatewaysGap(final Map<String, ? extends GatewayConfiguration> target,
                                                                 final Map<String, ? extends GatewayConfiguration> baseline) {
        //compute gaps for gateway that should be deleted from baseline config
        Stream<ConfigurationPatch> result = baseline.entrySet()
                .stream()
                .filter(gatewayInstance -> !target.containsKey(gatewayInstance.getKey()))
                .map(gatewayInstance -> new RemoveGatewayPatchImpl(gatewayInstance.getKey(), gatewayInstance.getValue()));
        result = Stream.concat(result, target.entrySet().stream().map(gatewayInstance -> {
                    //compute gaps between two gateways
                    if (baseline.containsKey(gatewayInstance.getKey())) {
                        final GatewayConfiguration targetConfig = gatewayInstance.getValue();
                        return targetConfig.equals(baseline.get(gatewayInstance.getKey())) ? null : new UpdateGatewayInstancePatchImpl(gatewayInstance.getKey(), targetConfig);
                    }
                    //compute gaps for gateway that should be added to the baseline config
                    else
                        return new AddGatewayPatchIml(gatewayInstance.getKey(), gatewayInstance.getValue());
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
                                                                   final Map<String, ? extends ThreadPoolConfiguration> baseline) {
        Stream<ConfigurationPatch> result = baseline.entrySet()
                .stream()
                .filter(tp -> !target.containsKey(tp.getKey()))
                .map(threadPool -> new RemoveThreadPoolPatchImpl(threadPool.getKey(), threadPool.getValue()));

        result = Stream.concat(result, target.entrySet().stream().map(threadPool -> {
                    if (baseline.containsKey(threadPool.getKey())) {
                        final ThreadPoolConfiguration targetConfig = threadPool.getValue();
                        return targetConfig.equals(baseline.get(threadPool.getKey())) ? null : new UpdateThreadPoolPatchImpl(threadPool.getKey(), targetConfig);
                    } else return new AddThreadPoolPatchImpl(threadPool.getKey(), threadPool.getValue());
                }).filter(Objects::nonNull)
        );
        return result;
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
        Stream<ConfigurationPatch> result = computeGatewaysGap(
                target.getEntities(GatewayConfiguration.class),
                baseline.getEntities(GatewayConfiguration.class));
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
