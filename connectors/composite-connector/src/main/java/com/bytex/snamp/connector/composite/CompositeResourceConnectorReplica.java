package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeRepositoryReplica;

import java.io.*;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class CompositeResourceConnectorReplica implements Externalizable {
    private static final long serialVersionUID = -5497518397507394190L;

    private static final class AttributeCompositionReplica extends AttributeRepositoryReplica<AbstractCompositeAttribute>{
        private static final long serialVersionUID = -4629170407063672873L;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public AttributeCompositionReplica() {
        }

        @Override
        protected Optional<? extends Serializable> takeSnapshot(final AbstractCompositeAttribute attribute) {
            return Convert.toType(attribute, DistributedAttribute.class)
                    .map(DistributedAttribute::takeSnapshot);
        }

        @Override
        protected void loadFromSnapshot(final AbstractCompositeAttribute attribute, final Serializable snapshot) {
            if (attribute instanceof DistributedAttribute)
                ((DistributedAttribute) attribute).loadFromSnapshot(snapshot);
        }
    }

    private final AttributeCompositionReplica attributes;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public CompositeResourceConnectorReplica() {
        attributes = new AttributeCompositionReplica();
    }

    void init(final AbstractAttributeRepository<AbstractCompositeAttribute> attributes){
        this.attributes.init(attributes);
    }

    void restore(final AbstractAttributeRepository<AbstractCompositeAttribute> attributes){
        this.attributes.restore(attributes);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        attributes.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        attributes.readExternal(in);
    }
}
