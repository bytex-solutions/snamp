package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.FactoryMap;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.io.SerializableMap;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


abstract class SerializableFactoryMap<K, V extends Modifiable & Stateful> extends ModifiableMap<K, V> implements FactoryMap<K, V>, SerializableMap<K, V> {
    private static final long serialVersionUID = -5518666612904348037L;

    final <ERROR extends Exception> Set<K> modifiedEntries(final EntryReader<? super K, ? super V, ERROR> reader) throws ERROR {
        if(isEmpty())
            return Collections.emptySet();
        final Set<K> modified = new HashSet<>(size());
        for (final Entry<K, V> e : entrySet()) {
            final V entity = e.getValue();
            final K name = e.getKey();
            if (entity.isModified()) {
                modified.add(name);
                if (!reader.accept(name, entity)) return modified;
            }
        }
        return Collections.unmodifiableSet(modified);
    }

    final <S> void load(final Map<K, S> map, final BiConsumer<? super V, ? super S> loader) {
        if (map == null)
            return;
        clear();
        map.forEach((id, entityToImport) -> loader.accept(getOrAdd(id), entityToImport));
    }

    @Override
    public final boolean isModified() {
        if (super.isModified()) return true;
        else for (final Modifiable entity : values())
            if (entity.isModified()) return true;
        return false;
    }

    private static void reset(final Stateful r){
        r.reset();
    }

    @Override
    public final void reset() {
        super.reset();
        values().forEach(SerializableFactoryMap::reset);//BANANA: Bug in JDK, can't replace with Stateful::reset
    }

    @Nonnull
    abstract V createValue();

    @Override
    public final boolean addAndConsume(final K entityID, final Consumer<? super V> handler) {
        if (containsKey(entityID)) {
            handler.accept(get(entityID));
            return false;
        } else {
            final V entity = createValue();
            put(entityID, entity);
            handler.accept(entity);
            return true;
        }
    }

    @Override
    public final <I> boolean addAndConsume(final I input, final K entityID, final BiConsumer<? super I, ? super V> handler) {
        return addAndConsume(entityID, entity -> handler.accept(input, entity));
    }

    @Override
    @Nonnull
    public final V getOrAdd(final K entityID) {
        final V result;
        if (containsKey(entityID))
            result = get(entityID);
        else
            put(entityID, result = createValue());
        return result;
    }
}
