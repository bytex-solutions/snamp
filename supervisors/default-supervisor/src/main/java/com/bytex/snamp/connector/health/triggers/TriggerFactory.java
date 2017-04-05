package com.bytex.snamp.connector.health.triggers;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.core.ScriptletCompiler;

import java.io.IOException;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Responsible for compiling health status triggers from different languages.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class TriggerFactory implements ScriptletCompiler<HealthStatusTrigger> {
    private final LazySoftReference<GroovyTriggerFactory> groovyTriggerFactory = new LazySoftReference<>();

    private GroovyTrigger createGroovyTrigger(final String scriptBody) throws IOException {
        final ClassLoader loader = getClass().getClassLoader();
        return groovyTriggerFactory.lazyGet(consumer -> consumer.accept(new GroovyTriggerFactory(loader))).create(scriptBody);
    }

    @Override
    public HealthStatusTrigger compile(final ScriptletConfiguration trigger) throws InvalidTriggerException {
        final String scriptBody, language = trigger.getLanguage();
        try {
            scriptBody = trigger.resolveScriptBody();
        } catch (final IOException e) {
            throw new InvalidTriggerException(language, e);
        }
        final Function<? super Exception, InvalidTriggerException> exceptionFactory = e -> new InvalidTriggerException(language, scriptBody, e);
        switch (trigger.getLanguage()) {
            case ScriptletConfiguration.GROOVY_LANGUAGE:
                return callAndWrapException(() -> createGroovyTrigger(scriptBody), exceptionFactory);
            case "":
                return HealthStatusTrigger.IDENTITY;
            default:
                throw new InvalidTriggerException(language);
        }
    }
}
