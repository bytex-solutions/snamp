package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.core.ScriptletCompiler;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Provides compilation of scaling policies into voters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScalingPolicyFactory implements ScriptletCompiler<ScalingPolicy> {
    private final LazySoftReference<ObjectMapper> mapper = new LazySoftReference<>();
    private final LazySoftReference<GroovyScalingPolicyFactory> groovyPolicyFactory = new LazySoftReference<>();

    private void createGroovyScalingPolicyFactory(final Consumer<GroovyScalingPolicyFactory> acceptor) throws IOException {
        acceptor.accept(new GroovyScalingPolicyFactory(getClass().getClassLoader()));
    }

    private GroovyScalingPolicy createGroovyPolicy(final String script) throws IOException {
        final ClassLoader loader = getClass().getClassLoader();
        return groovyPolicyFactory.lazyGet(this::createGroovyScalingPolicyFactory).create(script);
    }

    private MetricBasedScalingPolicy createMetricBasedPolicy(final String json) throws IOException {
        return MetricBasedScalingPolicy.parse(json, mapper.lazyGet((Supplier<ObjectMapper>) ObjectMapper::new));
    }

    private HealthStatusBasedScalingPolicy createStatusBasedPolicy(final String json) throws IOException{
        return HealthStatusBasedScalingPolicy.parse(json, mapper.lazyGet((Supplier<ObjectMapper>) ObjectMapper::new));
    }

    @Override
    public final ScalingPolicy compile(final ScriptletConfiguration scalingPolicy) throws InvalidScalingPolicyException {
        final String scriptBody, language = scalingPolicy.getLanguage();
        try {
            scriptBody = scalingPolicy.resolveScriptBody();
        } catch (final IOException e) {
            throw new InvalidScalingPolicyException(language, e);
        }
        final Function<? super Exception, InvalidScalingPolicyException> exceptionFactory = e -> new InvalidScalingPolicyException(language, scriptBody, e);
        switch (scalingPolicy.getLanguage()) {
            case ScriptletConfiguration.GROOVY_LANGUAGE:
                return callAndWrapException(() -> createGroovyPolicy(scriptBody), exceptionFactory);
            case MetricBasedScalingPolicy.LANGUAGE_NAME:
                return callAndWrapException(() -> createMetricBasedPolicy(scriptBody), exceptionFactory);
            case HealthStatusBasedScalingPolicy.LANGUAGE_NAME:
                return callAndWrapException(() -> createStatusBasedPolicy(scriptBody), exceptionFactory);
            case "":
                return ScalingPolicy.VOICELESS;
            default:
                throw new InvalidScalingPolicyException(language);
        }
    }
}
