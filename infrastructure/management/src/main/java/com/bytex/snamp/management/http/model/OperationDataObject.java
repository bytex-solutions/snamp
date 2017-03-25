package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.DurationSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * OperationDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("operation")
public final class OperationDataObject extends AbstractFeatureDataObject<OperationConfiguration> {
    private Duration duration;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public OperationDataObject() {}

    public OperationDataObject(final OperationConfiguration configuration) {
        super(configuration);
        this.duration = configuration.getInvocationTimeout();
    }

    @Override
    public void exportTo(@Nonnull final OperationConfiguration entity) {
        super.exportTo(entity);
        entity.setInvocationTimeout(duration);
    }

    @JsonProperty("invocationTimeout")
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    public Duration getInvocationTimeout() {
        return duration;
    }

    public void setInvocationTimeout(final Duration value) {
        duration = value;
    }
}
