package com.bytex.snamp.configuration;

/**
 * Represents configuration of a root entity which can be identified by type.
 * @since 2.0
 * @version 2.1
 */
public interface TypedEntityConfiguration extends EntityConfiguration {
    /**
     * Gets type of the entity.
     * @return Type of the entity.
     */
    String getType();

    /**
     * Sets type of the entity.
     * @param value Type of the entity.
     */
    void setType(final String value);
}
