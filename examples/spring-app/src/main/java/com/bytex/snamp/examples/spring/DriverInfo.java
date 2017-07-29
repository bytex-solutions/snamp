package com.bytex.snamp.examples.spring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about driver.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class DriverInfo {
    private final String carNumber;
    private final String driverFirstName, driverSecondName;

    @JsonCreator
    public DriverInfo(@JsonProperty("carNumber") final String carNumber,
                      @JsonProperty("driverFirstName") final String driverFirstName,
                      @JsonProperty("driverSecondName") final String driverSecondName){
        this.carNumber = carNumber;
        this.driverFirstName = driverFirstName;
        this.driverSecondName = driverSecondName;
    }

    @JsonProperty("carNumber")
    public String getCarNumber() {
        return carNumber;
    }

    @JsonProperty("driverFirstName")
    public String getDriverFirstName() {
        return driverFirstName;
    }

    @JsonProperty("driverSecondName")
    public String getDriverSecondName() {
        return driverSecondName;
    }
}
