package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.attributes.AttributeRepositoryReplica;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class SyntheticAttributeRepositoryReplica extends AttributeRepositoryReplica<SyntheticAttribute> {
    private static final long serialVersionUID = -6073058027337721252L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SyntheticAttributeRepositoryReplica() {
    }

    @Override
    protected Optional<? extends Serializable> takeSnapshot(final SyntheticAttribute attribute) {
        return Convert.toType(attribute, DistributedAttribute.class).map(DistributedAttribute::takeSnapshot);
    }

    @Override
    protected void loadFromSnapshot(final SyntheticAttribute attribute, final Serializable snapshot) {
        Convert.toType(attribute, DistributedAttribute.class).ifPresent(attr -> attr.loadFromSnapshot(snapshot));
    }
}
