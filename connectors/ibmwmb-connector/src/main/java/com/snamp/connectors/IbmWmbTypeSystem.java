package com.snamp.connectors;

/**
 * User: adonai
 * Date: 13.11.13
 * Time: 23:02
 */
public class IbmWmbTypeSystem extends WellKnownTypeSystem<EntityTypeInfoBuilder.AttributeTypeConverter> {
    /**
     * Initializes a new type system for the specified management entity.
     *
     * @param entityType Management entity type. Cannot be {@literal null}.
     * @throws IllegalArgumentException
     *          entityType is {@literal null}.
     */
    public IbmWmbTypeSystem(Class<AttributeTypeConverter> entityType) {
        super(entityType);
    }

    public  IbmWmbTypeSystem()
    {
        super(EntityTypeInfoBuilder.AttributeTypeConverter.class);
    }
}
