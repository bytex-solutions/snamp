package com.itworks.snamp.connectors;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.internal.annotations.ThreadSafe;

/**
 * Represents utility class that represents managed entity values and its type.
 * <p>
 *     This class can be used in the following cases:
 *     <li>
 *         <ul>When you want to store attribute value and its type as single object.</ul>
 *         <ul>Notification attachment value when static attachment type is not known.</ul>
 *     </li>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ManagedEntityValue<T extends ManagedEntityType> {
    /**
     * Represents value of the managed entity.
     */
    public final Object rawValue;

    /**
     * Represents type of the managed entity.
     */
    public final T type;

    /**
     * Initializes a new managed entity value and its type.
     * @param entityValue The value of the managed entity.
     * @param entityType The type of the managed entity.
     * @throws IllegalArgumentException entityType is {@literal null}.
     */
    public ManagedEntityValue(final Object entityValue, final T entityType){
        if(entityType == null) throw new IllegalArgumentException("entityType is null.");
        rawValue = entityValue;
        type = entityType;
    }

    /**
     * Determines whether the stored value can be converted into the specified type.
     *
     * @param target The result of the conversion.
     * @param <G>    The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    @ThreadSafe
    public final <G> boolean canConvertTo(final TypeToken<G> target) {
        return target != null &&
                (TypeLiterals.isInstance(rawValue, target) || type.getProjection(target) != null);
    }

    /**
     * Converts the stored value to the specified type.
     *
     * @param target The type of the conversion result. Cannot be {@literal null}.
     * @param <G>    Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    @ThreadSafe
    public final <G> G convertTo(final TypeToken<G> target) throws IllegalArgumentException {
        if(target == null) throw new IllegalArgumentException("target is null.");
        else if(TypeLiterals.isInstance(rawValue, target)) return TypeLiterals.cast(rawValue, target);
        else {
            final TypeConverter<G> converter = type.getProjection(target);
            if(converter == null) throw new IllegalArgumentException(String.format("Type %s is not supported", target));
            return converter.convertFrom(rawValue);
        }
    }

    /**
     * Determines whether the stored type is compliant with the specified entity type.
     * @param entityType The type to check.
     * @return {@literal true}, if the stored type is compliant with the specified entity type; otherwise, {@literal false}.
     */
    @ThreadSafe
    public final boolean isTypeOf(final Class<? extends ManagedEntityType> entityType){
        return entityType.isInstance(type);
    }

    /**
     * Changes the type of the stored type.
     * @param entityTypeDef A new type for upper type casting.
     * @param <G> A new type used for upper type casting.
     * @return A new instance of this class. Field {@link #rawValue} will not be changed.
     */
    @ThreadSafe
    public final <G extends ManagedEntityType> ManagedEntityValue<G> cast(final Class<G> entityTypeDef){
        return new ManagedEntityValue<>(rawValue, entityTypeDef.cast(type));
    }
}
