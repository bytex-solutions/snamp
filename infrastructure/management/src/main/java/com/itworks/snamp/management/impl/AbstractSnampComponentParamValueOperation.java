package com.itworks.snamp.management.impl;

import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.*;
import java.util.*;

/**
 * The type Abstract snamp component param value operation.
 * @author Evgeniy Kirichenko
 */
abstract class AbstractSnampComponentParamValueOperation extends OpenMBean.OpenOperation<String [], ArrayType<String []>>
    implements CommonOpenTypesSupport{

    /**
     * The constant PARAM_NAME_PARAM.
     */
    protected static final OpenMBeanParameterInfo PARAM_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "parameterName",
            "The name of the parameter which values should be suggested",
            SimpleType.STRING
    );

    /**
     * The constant CONNECTION_STRING_PARAM.
     */
    protected static final OpenMBeanParameterInfo CONNECTION_STRING_PARAM = new OpenMBeanParameterInfoSupport(
            "connectionStringData",
            "Additional parameters for filtering suggested values",
            SIMPLE_MAP_TYPE
    );

    /**
     * The Snamp manager.
     */
    protected final AbstractSnampManager snampManager;

    /**
     * Instantiates a new Abstract snamp component param value operation.
     *
     * @param manager the manager
     * @param operationName the operation name
     * @param parameters the parameters
     * @throws OpenDataException the open data exception
     */
    protected AbstractSnampComponentParamValueOperation(final AbstractSnampManager manager,
                                                        final String operationName,
                                                        final OpenMBeanParameterInfo... parameters) throws OpenDataException {
        super(operationName, new ArrayType<String[]>(SimpleType.STRING, false), parameters);
        this.snampManager = Objects.requireNonNull(manager);
    }

    /**
     * Get snamp component suggested value.
     *
     * @param snampComponentDescriptor the snamp component descriptor
     * @param parameterName the parameter name
     * @param locale the locale
     * @param configurationEntity the configuration entity
     * @param tabularData the tabular data
     * @return the string [ ]
     * @throws Exception the exception
     */
    protected final String[] getSnampComponentSuggestedValue(final SnampComponentDescriptor snampComponentDescriptor,
                                                             final String parameterName, final String locale,
                                                             final Class<? extends AgentConfiguration.ConfigurationEntity> configurationEntity,
                                                             final Map<String, String> tabularData) throws Exception {

        final List<String> result = new LinkedList<>();
        snampComponentDescriptor.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, Exception>() {
            @Override
            public void accept(final ConfigurationEntityDescriptionProvider input) throws Exception {
                final ConfigurationEntityDescription<?> description = input.getDescription(configurationEntity);
                if (description != null) {
                    final ConfigurationEntityDescription.ParameterDescription pd = description.getParameterDescriptor(parameterName);
                    if (pd instanceof SelectableAdapterParameterDescriptor)
                        Collections.addAll(result, ((SelectableAdapterParameterDescriptor) pd).suggestValues(tabularData,
                                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));

                }
            }
        });
        return ArrayUtils.toArray(result, String.class);
    }
}
