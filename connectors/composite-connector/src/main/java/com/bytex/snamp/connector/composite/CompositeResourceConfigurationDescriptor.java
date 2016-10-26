package com.bytex.snamp.connector.composite;

import com.bytex.snamp.LazyValue;
import com.bytex.snamp.LazyValueFactory;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.FunctionParser;
import com.bytex.snamp.parser.ParseException;

import javax.management.Descriptor;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String SEPARATOR_PARAM = "separator";
    private static final String SOURCE_PARAM = "source";
    private static final String FORMULA_PARAM = "formula";
    private static final String RATE_FORMULA_PARAM = "rate()";
    private static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";

    private static final LazyValue<CompositeResourceConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(CompositeResourceConfigurationDescriptor::new);

    private static final class ResourceConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private ResourceConfigurationDescription(){
            super("ConnectorParameters", ManagedResourceConfiguration.class, SEPARATOR_PARAM);
        }
    }

    private static final class AttributeConfigurationDescription extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private AttributeConfigurationDescription(){
            super("AttributeParameters", AttributeConfiguration.class, SOURCE_PARAM, FORMULA_PARAM);
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

    Duration parseSyncPeriod(final Map<String, String> parameters){
        final long period = getValueAsInt(parameters, SYNC_PERIOD_PARAM, Integer::parseInt, () -> 5000);
        return Duration.ofMillis(period);
    }

    static String parseSource(final Descriptor descriptor) throws AbsentCompositeConfigurationParameterException {
        return getFieldIfPresent(descriptor, SOURCE_PARAM, Objects::toString, AbsentCompositeConfigurationParameterException::new);
    }

    static boolean isRateFormula(final AttributeDescriptor descriptor){
        return getField(descriptor, FORMULA_PARAM, RATE_FORMULA_PARAM::equals, () -> false);
    }

    static AggregationFunction<?> parseFormula(final AttributeDescriptor descriptor) throws ParseException {
        final String formula = getField(descriptor, FORMULA_PARAM, Objects::toString, () -> "");
        if(isNullOrEmpty(formula))
            return null;
        return FunctionParser.parse(formula);
    }
}
