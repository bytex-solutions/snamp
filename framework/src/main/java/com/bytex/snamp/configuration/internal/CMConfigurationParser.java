package com.bytex.snamp.configuration.internal;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.function.Function;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * Provides parsing of SNAMP configuration from data provided by {@link org.osgi.service.cm.Configuration}.
 * <p>
 *     This interface is intended to use from your code directly. Any future release of SNAMP may change
 *     configuration storage provided and this interface will be deprecated.
 * @param <E> Type of configuration section.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public interface CMConfigurationParser<E extends EntityConfiguration> {
    /**
     * Converts {@link Configuration} into SNAMP-specific configuration section.
     * @param config Configuration to convert.
     * @return Converted SNAMP configuration section.
     * @throws IOException Unable to parse persistent configuration.
     */
    E parse(final Configuration config) throws IOException;

    void serialize(final E input, final Configuration output) throws IOException;

    static <I extends CMConfigurationParser<? extends EntityConfiguration>, O> O withParser(final BundleContext context,
                                                                                            final Class<I> parserType,
                                                                                            final Function<? super I, ? extends O> parserHandler) {
        final ServiceHolder<ConfigurationManager> manager = ServiceHolder.tryCreate(context, ConfigurationManager.class);
        if (manager == null)
            throw new ConfigurationParserException(parserType);
        else try {
            final I parser = manager.get().queryObject(parserType);
            if (parser == null)
                throw new ConfigurationParserException(parserType);
            else
                return parserHandler.apply(parser);
        } finally {
            manager.release(context);
        }
    }
}
