package com.itworks.snamp.management.impl;

import com.itworks.snamp.Box;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.*;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Created by temni on 2/8/2015.
 */
final class SuggestAdapterParameterValuesOperation extends OpenMBean.OpenOperation<String [], ArrayType<String []>> {

    private static final String NAME = "suggestAdapterParameterValuesOperation";

    private static final OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );

    private static final OpenMBeanParameterInfo PARAM_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "parameterName",
            "The name of the managed resource adapter's parameter which values should be suggested",
            SimpleType.STRING
    );

    private static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    private static SnampComponentDescriptor getResourceAdapter(final SnampManager snampManager,
                                                               final String adapterName){
        for(final SnampComponentDescriptor adapter: snampManager.getInstalledResourceAdapters())
            if(Objects.equals(adapterName, adapter.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY)))
                return adapter;
        return null;
    }

    protected final AbstractSnampManager snampManager;

    SuggestAdapterParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(NAME, new ArrayType<String[]>(SimpleType.STRING, true), ADAPTER_NAME_PARAM, PARAM_NAME_PARAM, LOCALE_PARAM);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    @Override
    public String[] invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final SnampComponentDescriptor adapter = getResourceAdapter(snampManager, adapterName);
        if (adapter == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));

        final Box<String []> result = new Box<>(new String [100]);
        adapter.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, Exception>() {
            @Override
            public void accept(final ConfigurationEntityDescriptionProvider input) throws Exception {
                final ConfigurationEntityDescription<?> description = input.getDescription(AgentConfiguration.ResourceAdapterConfiguration.class);
                if (description != null) {
                    final ConfigurationEntityDescription.ParameterDescription pd = description.getParameterDescriptor(parameterName);
                    if (pd == null || !(pd instanceof SelectableAdapterParameterDescriptor))
                        result.set(new String[]{});
                    else {
                        result.set(((SelectableAdapterParameterDescriptor) pd).suggestValues(null,
                            locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
                    }
                }
            }
        });
        return result.get();

    }
}
