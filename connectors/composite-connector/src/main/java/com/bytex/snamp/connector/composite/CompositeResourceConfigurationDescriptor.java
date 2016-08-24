package com.bytex.snamp.connector.composite;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;

import javax.management.Descriptor;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String SEPARATOR_PARAM = "separator";
    private static final String CONNECTOR_TYPE_PARAM = "connectorType";

    private static final LazyValue<CompositeResourceConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(CompositeResourceConfigurationDescriptor::new);

    private static final class ResourceDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private ResourceDescription(){
            super("ConnectorParameters", ManagedResourceConfiguration.class, SEPARATOR_PARAM);
        }
    }

    private CompositeResourceConfigurationDescriptor(){
        super(new ResourceDescription());
    }

    static CompositeResourceConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }

    String parseSeparator(final Map<String, String> parameters){
        return getValue(parameters, SEPARATOR_PARAM, Function.identity(), () -> ";");
    }

    String parseConnectorType(final Descriptor descriptor) throws AbsentCompositeConfigurationParameterException {
        return getFieldIfPresent(descriptor, CONNECTOR_TYPE_PARAM, Objects::toString, AbsentCompositeConfigurationParameterException::new);
    }
}
