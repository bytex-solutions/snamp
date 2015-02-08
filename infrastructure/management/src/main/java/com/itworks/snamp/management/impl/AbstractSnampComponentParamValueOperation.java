package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.Box;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.management.jmx.OpenMBean;
import com.sun.xml.internal.fastinfoset.util.FixedEntryStringIntMap;

import javax.management.openmbean.*;
import java.util.*;

/**
 * Created by temni on 2/8/2015.
 */
abstract class AbstractSnampComponentParamValueOperation extends OpenMBean.OpenOperation<String [], ArrayType<String []>> {

    protected static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    protected static final OpenMBeanParameterInfo PARAM_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "parameterName",
            "The name of the parameter which values should be suggested",
            SimpleType.STRING
    );

    protected static final TabularType CONNECTION_PARAMS_SCHEMA;

    static {
        try {
            CONNECTION_PARAMS_SCHEMA = new TabularType("com.itworks.management.ConnectionParams",
                    "Configuration entity schema",
                    new CompositeType("com.itworks.management.ConnectionParam",
                            "Additional parameters for filtering suggested values",
                            new String[]{"key", "value"},
                            new String[]{"Parameter key", "Parameter value"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"key"}
            );
        } catch (OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected static final OpenMBeanParameterInfo CONNECTION_STRING_PARAM = new OpenMBeanParameterInfoSupport(
            "connectionStringData",
            "Additional parameters for filtering suggested values",
            CONNECTION_PARAMS_SCHEMA
    );

    protected static Map<String, String> transformTabularDataToMap(final TabularData data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> result = new HashMap<>();
            for (Object value : data.values()) {
                if (!(value instanceof CompositeData)) continue;
                final CompositeData cd = (CompositeData) value;
                result.put((String) cd.get("key"), (String) cd.get("value"));
            }
            return result;
        }
    }


    protected AbstractSnampComponentParamValueOperation(String operationName, ArrayType<String[]> returnType, OpenMBeanParameterInfo... parameters) {
        super(operationName, returnType, parameters);
    }

    protected static SnampComponentDescriptor getResourceAdapter(final SnampManager snampManager,
                                                               final String adapterName){
        for(final SnampComponentDescriptor adapter: snampManager.getInstalledResourceAdapters())
            if(Objects.equals(adapterName, adapter.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY)))
                return adapter;
        return null;
    }

    protected static SnampComponentDescriptor getResourceConnector(final SnampManager snampManager,
                                                                 final String connectorName){
        for(final SnampComponentDescriptor connector: snampManager.getInstalledResourceConnectors())
            if(Objects.equals(connectorName, connector.get(SnampComponentDescriptor.CONNECTOR_SYSTEM_NAME_PROPERTY)))
                return connector;
        return null;
    }

    protected String[] getSnampCompenentSuggestedValue(final SnampComponentDescriptor snampComponentDescriptor,
                                                       final String parameterName, final String locale,
                                                       final Class<? extends AgentConfiguration.ConfigurationEntity> configurationEntity,
                                                       final Map<String, String> tabularData) throws Exception {

        final Box<List<String>> result = new Box<>();
        snampComponentDescriptor.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, Exception>() {
            @Override
            public void accept(final ConfigurationEntityDescriptionProvider input) throws Exception {
                final ConfigurationEntityDescription<?> description = input.getDescription(configurationEntity);
                if (description != null) {
                    final ConfigurationEntityDescription.ParameterDescription pd = description.getParameterDescriptor(parameterName);
                    if (pd == null || !(pd instanceof SelectableAdapterParameterDescriptor))
                        result.set(Collections.<String>emptyList());
                    else {
                        result.set(ImmutableList.copyOf(((SelectableAdapterParameterDescriptor) pd).suggestValues(tabularData,
                                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale))));
                    }
                }
            }
        });
        final List<String> resultList = result.get();
        return resultList.toArray(new String[resultList.size()]);
    }
}
