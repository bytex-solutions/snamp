package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.time.Duration;

/**
 * AttributeDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("attribute")
public final class AttributeDataObject extends AbstractFeatureDataObject<AttributeConfiguration> {
    private Duration duration;

    @SpecialUse
    public AttributeDataObject() {}

    public AttributeDataObject(final AttributeConfiguration configuration) {
        super(configuration);
        this.duration = configuration.getReadWriteTimeout();
    }

    @Override
    public void exportTo(final AttributeConfiguration entity) {
        super.exportTo(entity);
        entity.setReadWriteTimeout(duration);
    }

    public Duration getReadWriteTimeout() {
        return duration;
    }

    public void setReadWriteTimeout(final Duration value) {
        duration = value;
    }
}