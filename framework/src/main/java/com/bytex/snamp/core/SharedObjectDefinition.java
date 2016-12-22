package com.bytex.snamp.core;

import java.util.Objects;

/**
 * Describes cluster-wide shared object.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public class SharedObjectDefinition<S extends SharedObject> {
    private final boolean persistent;
    private final Class<S> objectType;

    protected SharedObjectDefinition(final Class<S> ot, final boolean persistent){
        objectType = Objects.requireNonNull(ot);
        this.persistent = persistent;
    }

    protected SharedObjectDefinition(final SharedObjectDefinition<S> definition){
        objectType = definition.getType();
        persistent = definition.isPersistent();
    }

    /**
     * Gets type of shared object.
     * @return Type of shared object.
     */
    public final Class<S> getType(){
        return objectType;
    }

    /**
     * Determines whether shared object MUST be persistent.
     * @return {@literal true}, if requested shared object must be persistent; otherwise, {@literal false}.
     */
    public final boolean isPersistent(){
        return persistent;
    }

    public final boolean isInstance(final SharedObject object){
        return persistent == object.isPersistent() && objectType.isInstance(object);
    }

    public final S cast(final SharedObject obj) {
        return isInstance(obj) ? objectType.cast(obj) : null;
    }

    private boolean equals(final SharedObjectDefinition<?> other){
        return getClass().equals(other.getClass()) &&
                getType().equals(other.getType()) &&
                isPersistent() == other.isPersistent();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof SharedObjectDefinition<?> && equals((SharedObjectDefinition<?>) other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, persistent);
    }

    @Override
    public String toString() {
        return "SharedObjectDefinition{" +
                "persistent=" + persistent +
                ", objectType=" + objectType +
                '}';
    }
}
