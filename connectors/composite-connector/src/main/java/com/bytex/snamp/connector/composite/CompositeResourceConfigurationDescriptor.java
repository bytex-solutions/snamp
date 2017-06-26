package com.bytex.snamp.connector.composite;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.FunctionParser;
import com.bytex.snamp.parser.ParseException;
import com.google.common.base.Splitter;

import javax.management.Descriptor;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final Splitter PATH_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private static final Supplier<String> EMPTY_STRING = () -> "";
    private static final String SEPARATOR_PARAM = "separator";
    private static final String SOURCE_PARAM = "source";
    private static final String FORMULA_PARAM = "formula";
    private static final String RATE_FORMULA_PARAM = "rate()";
    private static final String GROOVY_FORMULA_PARAM = "groovy()";
    private static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";
    private static final String GROOVY_PATH_PARAM = "groovyPath";

    private static final LazySoftReference<CompositeResourceConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private static final class ResourceConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private ResourceConfigurationDescription(){
            super("ConnectorParameters", ManagedResourceConfiguration.class, SEPARATOR_PARAM, SYNC_PERIOD_PARAM, GROOVY_PATH_PARAM);
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
        return INSTANCE.lazyGet(CompositeResourceConfigurationDescriptor::new);
    }

    String parseSeparator(final Map<String, String> parameters){
        return getValue(parameters, SEPARATOR_PARAM, Function.identity()).orElse(";");
    }

    Duration parseSyncPeriod(final Map<String, String> parameters){
        final long period = getValueAsInt(parameters, SYNC_PERIOD_PARAM, Integer::parseInt).orElse(5000);
        return Duration.ofMillis(period);
    }

    static String parseSource(final Descriptor descriptor) throws AbsentCompositeConfigurationParameterException {
        return getFieldIfPresent(descriptor, SOURCE_PARAM, Objects::toString, AbsentCompositeConfigurationParameterException::new);
    }

    URL[] parseGroovyPath(final Map<String, String> parameters) {
        final String path = getValue(parameters, GROOVY_PATH_PARAM, Function.identity()).orElse("");
        return PATH_SPLITTER.splitToList(path).stream().map(p -> callUnchecked(() -> new URL(p))).toArray(URL[]::new);
    }

    static boolean isRateFormula(final AttributeDescriptor descriptor){
        return getField(descriptor, FORMULA_PARAM, RATE_FORMULA_PARAM::equals).orElse(false);
    }

    static boolean isGroovyFormula(final AttributeDescriptor descriptor){
        return getField(descriptor, FORMULA_PARAM, GROOVY_FORMULA_PARAM::equals).orElse(false);
    }

    static AggregationFunction<?> parseFormula(final AttributeDescriptor descriptor) throws ParseException {
        final String formula = getField(descriptor, FORMULA_PARAM, Objects::toString).orElse("");
        if(isNullOrEmpty(formula))
            return null;
        return FunctionParser.parse(formula);
    }
}
