package com.bytex.snamp.instrumentation;

/**
 * Represents well-known measurements.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum StandardMeasurements {
    /**
     * Represents amount of available RAM in bytes.
     */
    FREE_RAM("freeRAM"),

    /**
     * Represents amount of used RAM in bytes.
     */
    USED_RAM("usedRAM"),

    /**
     * Represents utilization of CPU in percents.
     */
    CPU_LOAD("cpuLoad"),
    ;
    private final String measurementName;

    StandardMeasurements(final String name){
        measurementName = name;
    }

    public final String getMeasurementName(){
        return measurementName;
    }

    public static IntegerMeasurement freeRam(final long freeRamInBytes){
        final IntegerMeasurement measurement = new IntegerMeasurement();
        measurement.setValue(freeRamInBytes);
        measurement.setName(FREE_RAM);
        return measurement;
    }

    public static IntegerMeasurement usedRAM(final long usedRamInBytes){
        final IntegerMeasurement measurement = new IntegerMeasurement();
        measurement.setValue(usedRamInBytes);
        measurement.setName(USED_RAM);
        return measurement;
    }

    public static FloatingPointMeasurement cpuLoad(final double load){
        final FloatingPointMeasurement measurement = new FloatingPointMeasurement();
        measurement.setName(CPU_LOAD);
        measurement.setValue(load);
        return measurement;
    }
}
