package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class AttributeCheckerFactory {
    private final LazySoftReference<ObjectMapper> mapper = new LazySoftReference<>();
    private final LazySoftReference<GroovyAttributeCheckerFactory> groovyAttributeCheckerFactory = new LazySoftReference<>();

    private GroovyAttributeChecker createGroovyChecker(final String scriptBody) throws IOException {
        final ClassLoader loader = getClass().getClassLoader();
        return groovyAttributeCheckerFactory.lazyGet(consumer -> consumer.accept(new GroovyAttributeCheckerFactory(loader))).create(scriptBody);
    }

    private ColoredAttributeChecker createColoredChecker(final String scriptBody) throws IOException {
        return ColoredAttributeChecker.parse(scriptBody, mapper.lazyGet((Supplier<ObjectMapper>) ObjectMapper::new));
    }

    public AttributeChecker createChecker(final ScriptletConfiguration checker) throws InvalidAttributeCheckerException {
        final String scriptBody;
        try {
            scriptBody = checker.resolveScriptBody();
        } catch (final IOException e) {
            throw new InvalidAttributeCheckerException(e);
        }
        final Function<? super Exception, InvalidAttributeCheckerException> exceptionFactory = e -> new InvalidAttributeCheckerException(scriptBody, e);
        switch (checker.getLanguage()) {
            case ScriptletConfiguration.GROOVY_LANGUAGE:
                return callAndWrapException(() -> createGroovyChecker(scriptBody), exceptionFactory);
            case ColoredAttributeChecker.LANGUAGE_NAME:
                return callAndWrapException(() -> createColoredChecker(scriptBody), exceptionFactory);
            default:
                throw new InvalidAttributeCheckerException(checker.getLanguage());
        }
    }
}