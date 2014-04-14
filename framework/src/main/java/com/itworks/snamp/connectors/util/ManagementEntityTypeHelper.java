package com.itworks.snamp.connectors.util;

import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.connectors.ManagementEntityTabularType;
import com.itworks.snamp.connectors.ManagementEntityType;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Represents additional helpers methods that simplifies communication
 * with {@link com.itworks.snamp.connectors.ManagementEntityType} instances. This class cannot be inherited or instantiated.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagementEntityTypeHelper {
    private ManagementEntityTypeHelper(){

    }

    /**
     * Determines whether the specified management entity supports all of the specified projections.
     * @param entityType An entity type descriptor to check.
     * @param projections An array of projections to check.
     * @return {@literal true}, if the specified management entity supports all of the specified projections; otherwise, {@literal false}.
     */
    public static boolean supportsAllProjections(final ManagementEntityType entityType, final Class<?>... projections){
        for(final Class<?> p: projections)
            if(!supportsProjection(entityType, p)) return false;
        return true;
    }

    /**
     * Determines whether the specified management entity supports one of the specified projections.
     * @param entityType An entity type descriptor to check.
     * @param projections An array of projections to check.
     * @return {@literal true}, if the specified management entity supports one of the specified projections; otherwise, {@literal false}.
     */
    public static boolean supportsAnyProjection(final ManagementEntityType entityType, final Class<?>... projections){
        for(final Class<?> p: projections)
            if(supportsProjection(entityType, p)) return true;
        return false;
    }

    /**
     * Determines whether the specified management entity supports the specified projection.
     * @param entityType An entity type descriptor to check.
     * @param projection A projection to check.
     * @return {@literal true}, if the specified projection is supported; otherwise, {@literal false}.
     */
    public static boolean supportsProjection(final ManagementEntityType entityType, final Class<?> projection){
        return entityType != null && projection != null && entityType.getProjection(projection) != null;
    }

    /**
     * Converts the native management entity value into well-known Java type.
     * @param <T> Method return type.
     * @param entityType An entity type descriptor. Cannot be {@literal null}.
     * @param value The management entity value to convert.
     * @param nativeType Well-known Java type. Cannot be {@literal null}.
     * @return Well-known representation of the management entity value.
     * @throws IllegalArgumentException {@code entityType} is {@literal null}; or the specified conversion is not supported.
     */
    public static <T> T convertFrom(final ManagementEntityType entityType, final Object value, final Class<T> nativeType) throws IllegalArgumentException{
        if(entityType == null) throw new IllegalArgumentException("entityType is null.");
        else if(nativeType.isInstance(value)) return nativeType.cast(value);
        final TypeConverter<T> converter = entityType.getProjection(nativeType);
        if(converter == null) throw new IllegalArgumentException(String.format("Projection %s is not supported of management entity %s", nativeType, entityType));
        return converter.convertFrom(value);
    }

    /**
     * Represents a functional interface that is used by this helper and invoked when the conversion to projection
     * is not supported.
     * @param <T> Type of the conversion result.
     */
    public static interface ConversionFallback<T> extends Callable<T>{
        /**
         * Invokes the fallback procedure.
         * @return Default result of the conversion procedure if the projection is not supported.
         */
        @Override
        T call();
    }

    /**
     * Converts the native management entity value into one of the supported well-known Java type.
     * @param entityType An entity type descriptor. Cannot be {@literal null}.
     * @param value The management entity value to convert.
     * @param baseType A base class for each well-known Java type in the list.
     * @param fallback A function that is called if conversion is not supported.
     * @param projections A list of well-known Java types.
     * @param <T> Method return type.
     * @return Well-known representation of the management entity value.
     * @throws IllegalArgumentException {@code entityType} is {@literal null}.
     */
    public static <T> T convertFrom(final ManagementEntityType entityType, final Object value, final Class<T> baseType, final ConversionFallback<T> fallback, final Class<? extends T>... projections){
        if(entityType == null) throw new IllegalArgumentException("entityType is null.");
        else if(projections == null || projections.length == 0) return convertFrom(entityType, value, baseType);
        else for(final Class<? extends T> proj: projections)
            if(proj.isInstance(value)) return proj.cast(value);
            else {
                final TypeConverter<? extends T> converter = entityType.getProjection(proj);
                if(converter == null) continue;
                else return converter.convertFrom(value);
            }
        return fallback.call();
    }

    /**
     * Converts the native management entity value into one of the supported well-known Java type.
     * @param entityType An entity type descriptor. Cannot be {@literal null}.
     * @param value The management entity value to convert.
     * @param baseType A base class for each well-known Java type in the list.
     * @param projections A list of well-known Java types.
     * @param <T> Method return type.
     * @return Well-known representation of the management entity value.
     * @throws IllegalArgumentException {@code entityType} is {@literal null}; or the specified conversion is not supported.
     */                                                      // assertEquals(response.getResponse().getErrorStatusText(),PDU.noError);
    public static <T> T convertFrom(final ManagementEntityType entityType, final Object value, final Class<T> baseType, final Class<? extends T>... projections) throws IllegalArgumentException{
        return convertFrom(entityType, value, baseType, new ConversionFallback<T>() {
            @Override
            public T call() {
                throw new IllegalArgumentException(String.format("Projections %s are not supported by %s management entity type.", Arrays.toString(projections), entityType));
            }
        }, projections);
    }

    /**
     * Converts the native management entity value into one of the supported well-known Java type.
     * @param entityType An entity type descriptor. Cannot be {@literal null}.
     * @param value The management entity value to convert.
     * @param baseType A base class for each well-known Java type in the list.
     * @param projections A list of well-known Java types.
     * @param <T> Method return type.
     * @return Well-known representation of the management entity value.
     * @throws IllegalArgumentException {@code entityType} is {@literal null}.
     */
    public static <T> T convertFrom(final ManagementEntityType entityType, final Object value, final T defaultValue, final Class<T> baseType, final Class<? extends T>... projections){
        return convertFrom(entityType, value, baseType, new ConversionFallback<T>() {
            @Override
            public T call() {
                return defaultValue;
            }
        }, projections);
    }

    /**
     * Returns a read-only collection of indexed columns inside of the tabular management entity.
     * @param entityType Tabular management entity type. Cannot be {@literal null}.
     * @return A read-only collection of indexed columns.
     */
    public static Collection<String> getIndexedColumns(final ManagementEntityTabularType entityType){
        final Collection<String> index = new ArrayList<>(entityType.getColumns().size());
        for(final String indexedColumn: entityType.getColumns())
            if(entityType.isIndexed(indexedColumn))
                index.add(indexedColumn);
        return Collections.unmodifiableCollection(index);
    }
}
