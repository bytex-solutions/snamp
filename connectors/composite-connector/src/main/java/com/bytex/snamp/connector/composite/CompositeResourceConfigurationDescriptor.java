package com.bytex.snamp.connector.composite;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;

import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String SPLITTER_PARAM = "splitter";

    private static final LazyValue<CompositeResourceConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(CompositeResourceConfigurationDescriptor::new);

    private CompositeResourceConfigurationDescriptor(){

    }

    static CompositeResourceConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }

    String parseSplitter(final Map<String, String> parameters){
        return getValue(parameters, SPLITTER_PARAM, Function.identity(), () -> ";");
    }
}
