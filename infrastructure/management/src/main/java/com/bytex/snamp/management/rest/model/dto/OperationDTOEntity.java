package com.bytex.snamp.management.rest.model.dto;

import com.bytex.snamp.configuration.OperationConfiguration;

import java.time.Duration;
import java.util.Map;

/**
 * OperationDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class OperationDTOEntity extends AbstractDTOEntity implements OperationConfiguration {
    private Duration duration;

    OperationDTOEntity() {}
    OperationDTOEntity(final Map<String, String> map, Duration duration) {
        super(map);
        this.duration = duration;
    }

    @Override
    public Duration getInvocationTimeout() {
        return duration;
    }

    @Override
    public void setInvocationTimeout(Duration value) {
        duration = value;
    }
}
