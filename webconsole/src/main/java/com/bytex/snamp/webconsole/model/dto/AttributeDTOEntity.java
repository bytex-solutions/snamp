package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.AttributeConfiguration;

import java.time.Duration;
import java.util.Map;

/**
 * AttributeDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class AttributeDTOEntity extends AbstractDTOEntity implements AttributeConfiguration {

    AttributeDTOEntity() {}
    AttributeDTOEntity(final Map<String, String> map, Duration duration) {
        super(map);
        this.duration = duration;
    }

    private Duration duration;
    @Override
    public Duration getReadWriteTimeout() {
        return duration;
    }

    @Override
    public void setReadWriteTimeout(Duration value) {
        duration = value;
    }
}