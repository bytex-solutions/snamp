package com.bytex.snamp.core;

import com.bytex.snamp.Convert;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes cluster-wide shared object.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public class SharedObjectType<S extends SharedObject> {
    /**
     * Represents definition of non-persistent cluster-wide generator of unique identifiers.
     */
    public static final SharedObjectType<SharedCounter> COUNTER = new SharedObjectType<>(SharedCounter.class, false);
    /**
     * Represents definition of non-durable communication service.
     */
    public static final SharedObjectType<Communicator> COMMUNICATOR = new SharedObjectType<>(Communicator.class, false);
    /**
     * Represents definition of distributed non-persistent scalar storage.
     */
    public static final SharedObjectType<SharedBox> BOX = new SharedObjectType<>(SharedBox.class, false);
    /**
     * Represents definition of distributed non-persistent key/value storage.
     */
    public static final SharedObjectType<KeyValueStorage> KV_STORAGE = new SharedObjectType<>(KeyValueStorage.class, false);
    /**
     * Represents definition of distributed persistent key/value storage.
     */
    public static final SharedObjectType<KeyValueStorage> PERSISTENT_KV_STORAGE = new SharedObjectType<>(KeyValueStorage.class, true);
    
    private final boolean persistent;
    private final Class<S> objectType;

    protected SharedObjectType(final Class<S> ot, final boolean persistent){
        objectType = Objects.requireNonNull(ot);
        this.persistent = persistent;
    }

    protected SharedObjectType(final SharedObjectType<S> definition){
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

    public final Optional<S> cast(final SharedObject obj) {
        return Convert.toType(obj, objectType);
    }

    private boolean equals(final SharedObjectType<?> other){
        return getClass().equals(other.getClass()) &&
                getType().equals(other.getType()) &&
                isPersistent() == other.isPersistent();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof SharedObjectType<?> && equals((SharedObjectType<?>) other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, persistent);
    }

    @Override
    public String toString() {
        return "SharedObjectType{" +
                "persistent=" + persistent +
                ", objectType=" + objectType +
                '}';
    }
}
