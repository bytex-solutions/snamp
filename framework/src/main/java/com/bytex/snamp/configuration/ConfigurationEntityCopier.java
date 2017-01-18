package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface ConfigurationEntityCopier<T extends EntityConfiguration> {
    void copy(final T input, final T output);

    static <T extends EntityConfiguration> void copy(final Map<String, ? extends T> input,
                                                             final EntityMap<? extends T> output,
                                                             final ConfigurationEntityCopier<T> copier) {
        output.clear();
        for (final Map.Entry<String, ? extends T> entry : input.entrySet()) {
            final T source = entry.getValue();
            output.addAndConsume(source, entry.getKey(), copier::copy);
        }
    }
}
