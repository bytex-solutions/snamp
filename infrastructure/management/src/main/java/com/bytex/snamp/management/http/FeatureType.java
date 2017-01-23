package com.bytex.snamp.management.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.management.http.model.*;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bytex.snamp.management.http.AbstractEntityConfigurationService.*;

/**
 * Represents feature type.
 * @since 2.0
 * @version 2.0
 */
public enum FeatureType {
    ATTRIBUTES {
        @Override
        Map<String, AttributeDataObject> getFeatures(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType) {
            return FeatureType.getFeatures(context, holderName, holderType, AttributeConfiguration.class, AttributeDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return FeatureType.getBindings(context, gatewayInstance, MBeanAttributeInfo.class, MBeanAttributeInfo::getName);
        }

        @Override
        Optional<AttributeDataObject> getFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.getFeature(context, holderName, holderType, featureName, AttributeConfiguration.class, AttributeDataObject::new);
        }

        @Override
        Response removeFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.removeFeature(context, holderName, holderType, featureName, AttributeConfiguration.class);
        }
    },
    EVENTS {
        @Override
        Map<String, EventDataObject> getFeatures(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType) {
            return FeatureType.getFeatures(context, holderName, holderType, EventConfiguration.class, EventDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return FeatureType.getBindingsFlat(context, gatewayInstance, MBeanNotificationInfo.class, MBeanNotificationInfo::getNotifTypes);
        }

        @Override
        Optional<EventDataObject> getFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.getFeature(context, holderName, holderType, featureName, EventConfiguration.class, EventDataObject::new);
        }

        @Override
        Response removeFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.removeFeature(context, holderName, holderType, featureName, EventConfiguration.class);
        }
    },
    OPERATIONS {
        @Override
        Map<String, OperationDataObject> getFeatures(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType) {
            return FeatureType.getFeatures(context, holderName, holderType, OperationConfiguration.class, OperationDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return FeatureType.getBindings(context, gatewayInstance, MBeanOperationInfo.class, MBeanFeatureInfo::getName);
        }

        @Override
        Optional<OperationDataObject> getFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.getFeature(context, holderName, holderType, featureName, OperationConfiguration.class, OperationDataObject::new);
        }

        @Override
        Response removeFeature(final BundleContext context, final String holderName, final Class<? extends ManagedResourceTemplate> holderType, final String featureName) {
            return FeatureType.removeFeature(context, holderName, holderType, featureName, OperationConfiguration.class);
        }
    };
    static final String ATTRIBUTES_TYPE = "attributes";
    static final String EVENTS_TYPE = "events";
    static final String OPERATIONS_TYPE = "operations";

    /**
     * Gets all features provided by managed resource or resource group.
     * @param context Context of the caller bundle.
     * @param holderName Instance of the managed resource or resource group.
     * @param holderType Expects {@link ManagedResourceConfiguration} or {@link ManagedResourceGroupConfiguration}.
     * @return A map of all features.
     */
    abstract Map<String, ? extends AbstractFeatureDataObject<?>> getFeatures(final BundleContext context,
                                                                             final String holderName,
                                                                             final Class<? extends ManagedResourceTemplate> holderType);

    abstract Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context,
                                                              final String gatewayInstance);

    abstract Optional<? extends AbstractFeatureDataObject<?>> getFeature(final BundleContext context, final String holderName,
                                                                                                                   final Class<? extends ManagedResourceTemplate> holderType,
                                                                                                                   final String featureName);

    abstract Response removeFeature(final BundleContext context,
                                          final String holderName,
                                          final Class<? extends ManagedResourceTemplate> holderType,
                                          final String featureName);

    private static <F extends FeatureConfiguration, D extends AbstractFeatureDataObject<F>> Map<String, D> getFeatures(final BundleContext context,
                                                                                                                       final String holderName,
                                                                                                                       final Class<? extends ManagedResourceTemplate> holderType,
                                                                                                                       final Class<F> featureType,
                                                                                                                       final Function<? super F, ? extends D> factory){
        return readOnlyActions(context, config -> {
            final ManagedResourceTemplate entity = config.getEntities(holderType).get(holderName);
            if (entity == null)
                throw AbstractEntityConfigurationService.notFound();
            return entity.getFeatures(featureType).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> factory.apply(entry.getValue())));
        });
    }

    private static <F extends MBeanFeatureInfo> Map<String, Map<String, FeatureBindingDataObject>> getBindingsFlat(final BundleContext context,
                                                                                                      final String gatewayInstance,
                                                                                                      final Class<F> featureType,
                                                                                                  final Function<? super F, String[]> nameMapper) {
        final Map<String, Map<String, FeatureBindingDataObject>> result = new HashMap<>();
        try {
            final GatewayClient client = new GatewayClient(context, gatewayInstance);
            client.forEachFeature(featureType, (resourceName, bindingInfo) -> {
                final Map<String, FeatureBindingDataObject> bindingsMap;
                if(result.containsKey(resourceName))
                    bindingsMap = result.get(resourceName);
                else
                    result.put(resourceName, bindingsMap = new HashMap<>());
                for(final String name: nameMapper.apply(bindingInfo.getMetadata()))
                    bindingsMap.put(name, new FeatureBindingDataObject(bindingInfo));
                return true;
            });
        } catch (final InstanceNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return result;
    }

    private static <F extends MBeanFeatureInfo> Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context,
                                                                                                               final String gatewayInstance,
                                                                                                               final Class<F> featureType,
                                                                                                               final Function<? super F, String> nameMapper) {
        return getBindingsFlat(context, gatewayInstance, featureType, metadata -> new String[]{nameMapper.apply(metadata)});
    }

    private static <F extends FeatureConfiguration, D extends AbstractFeatureDataObject<F>> Optional<D> getFeature(final BundleContext context,
                                                                                                                         final String holderName,
                                                                                                                         final Class<? extends ManagedResourceTemplate> holderType,
                                                                                                                  final String featureName,
                                                                                                                  final Class<F> featureType,
                                                                                                                  final Function<? super F, ? extends D> factory) {
        final Optional<F> feature = readOnlyActions(context, config -> {
            final ManagedResourceTemplate resource = config.getEntities(holderType).get(holderName);
            if (resource != null) {
                final Map<String, ? extends F> features = resource.getFeatures(featureType);
                return Optional.ofNullable(features.get(featureName));
            } else
                return Optional.empty();
        });
        return feature.map(factory);
    }

    private static Response removeFeature(final BundleContext context,
                                                                                                                   final String holderName,
                                                                                                                   final Class<? extends ManagedResourceTemplate> holderType,
                                                                                                                   final String featureName,
                                          final Class<? extends FeatureConfiguration> featureType) {
        return changingActions(context, config -> {
            final ManagedResourceTemplate resource = config.getEntities(holderType).get(holderName);
            if (resource == null || resource.getFeatures(featureType).remove(featureName) == null) {
                throw notFound();
            } else
                return true;
        });
    }

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public static FeatureType fromString(final String value){
        switch (value){
            case ATTRIBUTES_TYPE: return ATTRIBUTES;
            case EVENTS_TYPE: return EVENTS;
            case OPERATIONS_TYPE: return OPERATIONS;
            default: throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Incorrect feature type " + value).build());
        }
    }
}
