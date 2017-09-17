package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.core.ScriptletCompiler;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 *
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class AttributeCheckerFactory implements ScriptletCompiler<AttributeChecker> {
    private final LazyReference<ObjectMapper> mapper = LazyReference.soft();
    private final LazyReference<GroovyAttributeCheckerFactory> groovyAttributeCheckerFactory = LazyReference.soft();

    private GroovyAttributeCheckerFactory createGroovyCheckerFactory() {
        return new GroovyAttributeCheckerFactory(getClass().getClassLoader());
    }

    private GroovyAttributeChecker createGroovyChecker(final String scriptBody) {
        return groovyAttributeCheckerFactory.get(this::createGroovyCheckerFactory).create(scriptBody);
    }

    private ColoredAttributeChecker createColoredChecker(final String scriptBody) throws IOException {
        return ColoredAttributeChecker.parse(scriptBody, mapper.get((Supplier<ObjectMapper>) ObjectMapper::new));
    }

    @Override
    public AttributeChecker compile(final ScriptletConfiguration checker) throws InvalidAttributeCheckerException {
        final String scriptBody, language = checker.getLanguage();
        try {
            scriptBody = checker.resolveScriptBody();
        } catch (final IOException e) {
            throw new InvalidAttributeCheckerException(language, e);
        }
        final Function<? super Exception, InvalidAttributeCheckerException> exceptionFactory = e -> new InvalidAttributeCheckerException(language, scriptBody, e);
        switch (checker.getLanguage()) {
            case ScriptletConfiguration.GROOVY_LANGUAGE:
                return callAndWrapException(() -> createGroovyChecker(scriptBody), exceptionFactory);
            case ColoredAttributeChecker.LANGUAGE_NAME:
                return callAndWrapException(() -> createColoredChecker(scriptBody), exceptionFactory);
            case "":
                return AttributeChecker.OK;
            default:
                throw new InvalidAttributeCheckerException(language);
        }
    }
}
