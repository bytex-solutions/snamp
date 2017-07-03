package com.bytex.snamp.examples.spring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TripInfo {
    private final String departure, destination;

    @JsonCreator
    public TripInfo(@JsonProperty("departure") final String departure,
                    @JsonProperty("destination") final String destination){
        this.departure = departure;
        this.destination = destination;
    }

    @JsonProperty("departure")
    public String getDeparture(){
        return departure;
    }

    @JsonProperty("destination")
    public String getDestination(){
        return destination;
    }
}
