package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.DurationSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * AttributeDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("attribute")
public final class AttributeDataObject extends AbstractFeatureDataObject<AttributeConfiguration> {
    private Duration duration;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public AttributeDataObject() {}

    public AttributeDataObject(final AttributeConfiguration configuration) {
        super(configuration);
        this.duration = configuration.getReadWriteTimeout();
    }

    @Override
    public void exportTo(@Nonnull final AttributeConfiguration entity) {
        super.exportTo(entity);
        entity.setReadWriteTimeout(duration);
    }

    @JsonProperty("readWriteTimeout")
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getReadWriteTimeout() {
        return duration;
    }

    @JsonDeserialize(using = DurationDeserializer.class)
    public void setReadWriteTimeout(final Duration value) {
        duration = value;
    }
}