package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.TypedEntityConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * TypedDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractTypedDataObject<E extends TypedEntityConfiguration> extends AbstractDataObject<E> {
    private String type;

    AbstractTypedDataObject() {}

    AbstractTypedDataObject(final E configuration) {
        super(configuration);
        type = configuration.getType();
    }

    @Override
    public void exportTo(final E entity) {
        super.exportTo(entity);
        entity.setType(type);
    }

    /**
     * Gets type of the entity.
     * @return Type of the entity.
     */
    @JsonProperty
    public final String getType() {
        return type;
    }

    /**
     * Sets type of the entity.
     * @param value Type of the entity.
     */
    public final void setType(final String value) {
        this.type = value;
    }
}
