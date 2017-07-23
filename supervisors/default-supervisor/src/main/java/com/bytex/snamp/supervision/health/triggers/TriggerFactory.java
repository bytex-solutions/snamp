package com.bytex.snamp.supervision.health.triggers;

import com.bytex.snamp.concurrent.LazyReference;
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
    private final LazyReference<GroovyTriggerFactory> groovyTriggerFactory = LazyReference.soft();

    private GroovyTriggerFactory createGroovyTriggerFactory() {
        return new GroovyTriggerFactory(getClass().getClassLoader());
    }

    private GroovyTrigger createGroovyTrigger(final String scriptBody) {
        return groovyTriggerFactory.lazyGet(this::createGroovyTriggerFactory).create(scriptBody);
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
                return HealthStatusTrigger.NO_OP;//no operation
            default:
                throw new InvalidTriggerException(language);
        }
    }
}
