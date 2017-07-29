package com.bytex.snamp.configuration.internal;

import com.bytex.snamp.Internal;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.EntityConfiguration;

import java.io.IOException;
import java.util.Dictionary;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Internal
interface CMRootEntityParser<E extends EntityConfiguration> extends CMConfigurationParser<E> {
    String getFactoryPersistentID(final String entityType);

    @Override
    SingletonMap<String, ? extends E> parse(final Dictionary<String, ?> configuration) throws IOException;
}
