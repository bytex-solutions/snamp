package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.FeatureRepository;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents replica of attribute repository.
 * @param <M> Type of attributes in repository.
 * @param <S> Type of attribute snapshot.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public abstract class AttributeRepositoryReplica<M extends MBeanAttributeInfo, S extends Serializable> implements Externalizable {
    private static final long serialVersionUID = -6501762720773353861L;
    private final Map<String, S> attributes;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public AttributeRepositoryReplica() {
        attributes = new HashMap<>();
    }

    private static String getStorageKey(final MBeanAttributeInfo attribute){
        return AttributeDescriptor.getName(attribute);
    }

    /**
     * Populate replica using attributes from repository.
     * @param repository Repository of attributes.
     * @deprecated Use {@linkplain #init(FeatureRepository)} instead.
     */
    @Deprecated
    public final void init(final AbstractFeatureRepository<M> repository){
        repository.forEach(this::add);
    }

    /**
     * Restore repository from replica.
     * @param repository Repository of attributes.
     * @deprecated Use {@linkplain #restore(FeatureRepository)} instead.
     */
    @Deprecated
    public final void restore(final AbstractFeatureRepository<M> repository) {
        repository.forEach(this::restore);
    }

    public final void init(final FeatureRepository<M> repository){
        repository.forEachFeature(this::add);
    }

    public final void restore(final FeatureRepository<M> repository){
        repository.forEachFeature(this::restore);
    }

    /**
     * Adds attribute to the replica.
     * @param attribute Attribute to be replicated.
     * @return {@literal true}, if the specified attribute supports replication; otherwise, {@literal false}.
     */
    public final boolean add(@Nonnull final M attribute) {
        final Optional<? extends S> snapshot = takeSnapshot(attribute);
        if (snapshot.isPresent()) {
            attributes.put(getStorageKey(attribute), snapshot.get());
            return true;
        } else
            return false;
    }

    public final boolean restore(@Nonnull final M attribute) {
        final S snapshot = attributes.get(getStorageKey(attribute));
        if (snapshot == null)
            return false;
        else {
            loadFromSnapshot(attribute, snapshot);
            return true;
        }
    }

    protected abstract Optional<? extends S> takeSnapshot(final M attribute);

    protected abstract void loadFromSnapshot(final M attribute, final S snapshot);

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.write(attributes.size());
        for (final Map.Entry<String, S> attribute : attributes.entrySet()) {
            out.writeUTF(attribute.getKey());
            out.writeObject(attribute.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        for (int size = in.readInt(); size > 0; size--) {
            final String attributeName = in.readUTF();
            final Object attributeState = in.readObject();
            if (attributeState instanceof Serializable)
                attributes.put(attributeName, (S) attributeState);
        }
    }
}
