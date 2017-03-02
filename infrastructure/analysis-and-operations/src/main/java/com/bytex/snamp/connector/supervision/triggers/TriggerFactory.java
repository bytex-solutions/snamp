package com.bytex.snamp.connector.supervision.triggers;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;

import java.io.IOException;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class TriggerFactory {
    private final LazySoftReference<GroovyTriggerFactory> groovyTriggerFactory = new LazySoftReference<>();

    private GroovyTrigger createGroovyTrigger(final String scriptBody) throws IOException {
        final ClassLoader loader = getClass().getClassLoader();
        return groovyTriggerFactory.lazyGet(consumer -> consumer.accept(new GroovyTriggerFactory(loader))).create(scriptBody);
    }

    public HealthStatusTrigger createTrigger(final ScriptletConfiguration trigger) throws InvalidTriggerException {
        final String scriptBody;
        try {
            scriptBody = trigger.resolveScriptBody();
        } catch (final IOException e) {
            throw new InvalidTriggerException(e);
        }
        final Function<? super Exception, InvalidTriggerException> exceptionFactory = e -> new InvalidTriggerException(scriptBody, e);
        switch (trigger.getLanguage()) {
            case ScriptletConfiguration.GROOVY_LANGUAGE:
                return callAndWrapException(() -> createGroovyTrigger(scriptBody), exceptionFactory);
            default:
                return HealthStatusTrigger.NO_OP;
        }
    }
}
