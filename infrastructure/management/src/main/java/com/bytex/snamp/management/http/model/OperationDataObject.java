package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.OperationConfiguration;
import org.codehaus.jackson.annotate.JsonTypeName;

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

    @SpecialUse
    public OperationDataObject() {}

    public OperationDataObject(final OperationConfiguration configuration) {
        super(configuration);
        this.duration = configuration.getInvocationTimeout();
    }

    @Override
    public void exportTo(final OperationConfiguration entity) {
        super.exportTo(entity);
        entity.setInvocationTimeout(duration);
    }

    public Duration getInvocationTimeout() {
        return duration;
    }

    public void setInvocationTimeout(final Duration value) {
        duration = value;
    }
}
