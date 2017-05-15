package com.bytex.snamp.management.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMapResolver;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.configuration.ManagedResourceTemplate;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.management.http.model.*;
import org.osgi.framework.BundleContext;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
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
        <T extends ManagedResourceTemplate> Map<String, AttributeDataObject> getFeatures(final BundleContext context, final String templateName, final EntityMapResolver<AgentConfiguration, T> templateResolver) {
            return getFeatures(context, templateName, templateResolver, EntityMapResolver.ATTRIBUTES, AttributeDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return getBindings(context, gatewayInstance, MBeanAttributeInfo.class, MBeanAttributeInfo::getName);
        }

        @Override
        <T extends ManagedResourceTemplate> Optional<AttributeDataObject> getFeature(final BundleContext context,
                                                 final String templateName,
                                                 final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                 final String attributeName) {
            return getFeature(context, templateName, templateResolver, template -> template.getAttributes().getIfPresent(attributeName).map(AttributeDataObject::new));
        }

        @Override
        <T extends ManagedResourceTemplate> Response removeFeature(final BundleContext context,
                               final String templateName,
                               final EntityMapResolver<AgentConfiguration, T> templateResolver,
                               final String featureName) {
            return removeFeature(context, templateName, templateResolver, featureName, EntityMapResolver.ATTRIBUTES);
        }
    },
    EVENTS {
        @Override
        <T extends ManagedResourceTemplate> Map<String, EventDataObject> getFeatures(final BundleContext context, final String holderName, final EntityMapResolver<AgentConfiguration, T> templateResolver) {
            return getFeatures(context, holderName, templateResolver, EntityMapResolver.EVENTS, EventDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return FeatureType.getBindingsFlat(context, gatewayInstance, MBeanNotificationInfo.class, MBeanNotificationInfo::getNotifTypes);
        }

        @Override
        <T extends ManagedResourceTemplate> Optional<EventDataObject> getFeature(final BundleContext context,
                                                 final String templateName,
                                                 final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                 final String eventName) {
            return getFeature(context, templateName, templateResolver, template -> template.getEvents().getIfPresent(eventName).map(EventDataObject::new));
        }

        @Override
        <T extends ManagedResourceTemplate> Response removeFeature(final BundleContext context,
                               final String templateName,
                               final EntityMapResolver<AgentConfiguration, T> templateResolver,
                               final String featureName) {
            return removeFeature(context, templateName, templateResolver, featureName, EntityMapResolver.EVENTS);
        }
    },
    OPERATIONS {
        @Override
        <T extends ManagedResourceTemplate> Map<String, OperationDataObject> getFeatures(final BundleContext context, final String holderName, final EntityMapResolver<AgentConfiguration, T> templateResolver) {
            return FeatureType.getFeatures(context, holderName, templateResolver, EntityMapResolver.OPERATIONS, OperationDataObject::new);
        }

        @Override
        Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance) {
            return FeatureType.getBindings(context, gatewayInstance, MBeanOperationInfo.class, MBeanFeatureInfo::getName);
        }

        @Override
        <T extends ManagedResourceTemplate> Optional<OperationDataObject> getFeature(final BundleContext context,
                                                 final String templateName,
                                                 final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                 final String operationName) {
            return getFeature(context, templateName, templateResolver, template -> template.getOperations().getIfPresent(operationName).map(OperationDataObject::new));
        }

        @Override
        <T extends ManagedResourceTemplate> Response removeFeature(final BundleContext context,
                               final String templateName,
                               final EntityMapResolver<AgentConfiguration, T> templateResolver,
                               final String featureName) {
            return removeFeature(context, templateName, templateResolver, featureName, EntityMapResolver.OPERATIONS);
        }
    };
    static final String ATTRIBUTES_TYPE = "attributes";
    static final String EVENTS_TYPE = "events";
    static final String OPERATIONS_TYPE = "operations";

    abstract <T extends ManagedResourceTemplate> Map<String, ? extends AbstractFeatureDataObject<?>> getFeatures(final BundleContext context,
                                                                             final String templateName,
                                                                             final EntityMapResolver<AgentConfiguration, T> holderType);

    abstract Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context, final String gatewayInstance);

    abstract <T extends ManagedResourceTemplate> Optional<? extends AbstractFeatureDataObject<?>> getFeature(final BundleContext context,
                                                                         final String templateName,
                                                                         final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                                         final String featureName);

    abstract <T extends ManagedResourceTemplate> Response removeFeature(final BundleContext context,
                                          final String templateName,
                                          final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                          final String featureName);

    static <T extends ManagedResourceTemplate, F extends FeatureConfiguration, D extends AbstractFeatureDataObject<F>> Map<String, D> getFeatures(final BundleContext context,
                                                                                                                       final String templateName,
                                                                                                                       final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                                                                                       final EntityMapResolver<? super T, F> featureResolver,
                                                                                                                       final Function<? super F, ? extends D> factory) {
        return readOnlyActions(context, config -> {
            final T template = templateResolver.apply(config)
                    .getIfPresent(templateName)
                    .orElseThrow(AbstractEntityConfigurationService::notFound);
            return featureResolver.apply(template)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> factory.apply(entry.getValue())));
        });
    }

    private static <F extends MBeanFeatureInfo> Map<String, Map<String, FeatureBindingDataObject>> getBindingsFlat(final BundleContext context,
                                                                                                      final String gatewayInstance,
                                                                                                      final Class<F> featureType,
                                                                                                  final Function<? super F, String[]> nameMapper) {
        final Map<String, Map<String, FeatureBindingDataObject>> result = new HashMap<>();
        try (final GatewayClient client = GatewayClient.tryCreate(context, gatewayInstance)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))) {
            client.forEachFeature(featureType, (resourceName, bindingInfo) -> {
                final Map<String, FeatureBindingDataObject> bindingsMap;
                if (result.containsKey(resourceName))
                    bindingsMap = result.get(resourceName);
                else
                    result.put(resourceName, bindingsMap = new HashMap<>());
                for (final String name : nameMapper.apply(bindingInfo.getMetadata()))
                    bindingsMap.put(name, new FeatureBindingDataObject(bindingInfo));
                return true;
            });
        }
        return result;
    }

    static <F extends MBeanFeatureInfo> Map<String, Map<String, FeatureBindingDataObject>> getBindings(final BundleContext context,
                                                                                                               final String gatewayInstance,
                                                                                                               final Class<F> featureType,
                                                                                                               final Function<? super F, String> nameMapper) {
        return getBindingsFlat(context, gatewayInstance, featureType, metadata -> new String[]{nameMapper.apply(metadata)});
    }

    static <T extends ManagedResourceTemplate, F extends FeatureConfiguration, D extends AbstractFeatureDataObject<F>> Optional<D> getFeature(final BundleContext context,
                                                                                                                         final String templateName,
                                                                                                                         final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                                                                                           final Function<? super T, Optional<D>> dtoFactory) {
        return readOnlyActions(context, config -> templateResolver.apply(config)
                .getIfPresent(templateName)
                .flatMap(dtoFactory));
    }

    static <T extends ManagedResourceTemplate> Response removeFeature(final BundleContext context,
                                            final String templateName,
                                  final EntityMapResolver<AgentConfiguration, T> templateResolver,
                                  final String featureName,
                                          final EntityMapResolver<? super T, ? extends FeatureConfiguration> featureResolver
                                          ) {
        return changingActions(context, config -> {
            final T resource = templateResolver.apply(config)
                    .getIfPresent(templateName)
                    .orElseThrow(AbstractManagementService::notFound);
            if (featureResolver.apply(resource).remove(featureName) == null)
                throw notFound();
            else
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
