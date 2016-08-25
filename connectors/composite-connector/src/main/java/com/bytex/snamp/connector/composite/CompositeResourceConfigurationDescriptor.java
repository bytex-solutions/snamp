package com.bytex.snamp.connector.composite;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.configuration.*;
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
    private static final String SOURCE_PARAM = "source";

    private static final LazyValue<CompositeResourceConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(CompositeResourceConfigurationDescriptor::new);

    private static final class ResourceConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private ResourceConfigurationDescription(){
            super("ConnectorParameters", ManagedResourceConfiguration.class, SEPARATOR_PARAM);
        }
    }

    private static final class AttributeConfigurationDescription extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private AttributeConfigurationDescription(){
            super("AttributeParameters", AttributeConfiguration.class, SOURCE_PARAM);
        }
    }

    private static final class EventConfigurationDescription extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private EventConfigurationDescription(){
            super("EventParameters", EventConfiguration.class, SOURCE_PARAM);
        }
    }

    private static final class OperationConfigurationDescription extends ResourceBasedConfigurationEntityDescription<OperationConfiguration>{
        private OperationConfigurationDescription(){
            super("OperationParameters", OperationConfiguration.class, SOURCE_PARAM);
        }
    }

    private CompositeResourceConfigurationDescriptor() {
        super(new ResourceConfigurationDescription(),
                new AttributeConfigurationDescription(),
                new EventConfigurationDescription(),
                new OperationConfigurationDescription());
    }

    static CompositeResourceConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }

    String parseSeparator(final Map<String, String> parameters){
        return getValue(parameters, SEPARATOR_PARAM, Function.identity(), () -> ";");
    }

    static String parseSource(final Descriptor descriptor) throws AbsentCompositeConfigurationParameterException {
        return getFieldIfPresent(descriptor, SOURCE_PARAM, Objects::toString, AbsentCompositeConfigurationParameterException::new);
    }
}
