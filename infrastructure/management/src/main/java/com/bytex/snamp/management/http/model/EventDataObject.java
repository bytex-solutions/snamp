package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EventConfiguration;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * EventDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("event")
public final class EventDataObject extends AbstractFeatureDataObject<EventConfiguration> {
    @SpecialUse
    public EventDataObject() {

    }

    public EventDataObject(final EventConfiguration map) {
        super(map);
    }
}