package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SpecialUse;

import javax.annotation.Nonnull;
import javax.management.MBeanAttributeInfo;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents replica of attribute repository.
 * @param <M> Type of attributes in repository.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public abstract class AttributeRepositoryReplica<M extends MBeanAttributeInfo> implements Externalizable {
    private static final long serialVersionUID = -6501762720773353861L;
    private final Map<String, Serializable> attributes;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public AttributeRepositoryReplica() {
        attributes = new HashMap<>();
    }

    private static String getStorageKey(final MBeanAttributeInfo attribute){
        return AttributeDescriptor.getName(attribute);
    }

    /**
     * Initializes this replica from the attributes stored in repository.
     * @param repository Repository of attributes.
     */
    public final void init(@Nonnull final AbstractAttributeRepository<M> repository){
        repository.forEach(attribute -> takeSnapshot(attribute).ifPresent(snapshot -> attributes.put(getStorageKey(attribute), snapshot)));
    }

    /**
     * Restores the state of all attributes inside of repository using this replica.
     * @param repository Repository with attributes to be restored from replica.
     */
    public final void restore(@Nonnull final AbstractAttributeRepository<M> repository) {
        repository.forEach(attribute -> {
            final Serializable snapshot = attributes.get(getStorageKey(attribute));
            if (snapshot != null)
                loadFromSnapshot(attribute, snapshot);
        });
    }

    protected abstract Optional<? extends Serializable> takeSnapshot(final M attribute);

    protected abstract void loadFromSnapshot(final M attribute, final Serializable snapshot);

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.write(attributes.size());
        for (final Map.Entry<String, Serializable> attribute : attributes.entrySet()) {
            out.writeUTF(attribute.getKey());
            out.writeObject(attribute.getValue());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        for (int size = in.readInt(); size > 0; size--) {
            final String attributeName = in.readUTF();
            final Object attributeState = in.readObject();
            if (attributeState instanceof Serializable)
                attributes.put(attributeName, (Serializable) attributeState);
        }
    }
}
