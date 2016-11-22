package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.EventConfiguration;

import java.util.Map;

/**
 * EventDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class EventDTOEntity extends AbstractDTOEntity implements EventConfiguration {
    EventDTOEntity() {}
    EventDTOEntity(final Map<String, String> map) {
        super(map);
    }
}