package com.bytex.snamp.management.rest.model.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * TypedDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class TypedDTOEntity extends AbstractDTOEntity {

    private String type;

    TypedDTOEntity() {};

    TypedDTOEntity(final Map<String, String> parameters, final String type) {
        super(parameters);
        this.type = type;
    }

    /**
     * Gets type of the entity.
     * @return Type of the entity.
     */
    @JsonProperty
    public String getType() {
        return this.type;
    }

    /**
     * Sets type of the entity.
     * @param value Type of the entity.
     */
    public void setType(final String value) {
        this.type = value;
    }
}
