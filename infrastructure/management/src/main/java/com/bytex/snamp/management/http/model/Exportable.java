package com.bytex.snamp.management.http.model;

import com.bytex.snamp.FactoryMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface Exportable<E> {
    void exportTo(@Nonnull final E output);

    static <F, DTO extends Exportable<F>> Map<String, DTO> importEntities(final Map<String, ? extends F> entities,
                                                                          final Function<? super F, DTO> dataObjectFactory) {
        return entities.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> dataObjectFactory.apply(entry.getValue())));
    }

    static <F> void exportEntities(final Map<String, ? extends Exportable<F>> source,
                                                               final FactoryMap<String, ? extends F> destination) {
        source.forEach((name, featureObject) -> featureObject.exportTo(destination.getOrAdd(name)));
    }
}
