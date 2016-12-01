package com.bytex.snamp.instrumentation;

import java.util.concurrent.TimeUnit;

/**
 * Represents well-known measurements.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StandardMeasurements {
    /**
     * Represents amount of available RAM, in bytes.
     */
    public static final String FREE_RAM = "freeRAM";

    /**
     * Represents amount of used RAM, in bytes.
     */
    public static final String USED_RAM = "usedRAM";

    /**
     * Represents amount of available disk space, in bytes
     */
    public static final String FREE_DISK_SPACE = "freeDiskSpace";

    public static final String USED_DISK_SPACE = "usedDiskSpace";

    public static final String RESPONSE_TIME = "responseTime";

    /**
     * Represents number of requests per second.
     */
    public static final String RPS = "rps";

    /**
     * Represents utilization of CPU, in percents.
     */
    public static final String CPU_LOAD = "cpuLoad";

    private StandardMeasurements(){
        throw new InstantiationError();
    }

    private static IntegerMeasurement createIntegerMeasurement(final long value, final String name){
        final IntegerMeasurement measurement = new IntegerMeasurement(value);
        measurement.setName(name);
        return measurement;
    }

    private static FloatingPointMeasurement createFloatingPointMeasurement(final double value, final String name){
        final FloatingPointMeasurement measurement = new FloatingPointMeasurement(value);
        measurement.setName(name);
        return measurement;
    }

    private static TimeMeasurement createTimeMeasurement(final long duration, final TimeUnit unit, final String name){
        final TimeMeasurement measurement = new TimeMeasurement(duration, unit);
        measurement.setName(name);
        return measurement;
    }

    public static IntegerMeasurement freeRam(final long freeRamInBytes){
        return createIntegerMeasurement(freeRamInBytes, FREE_RAM);
    }

    public static IntegerMeasurement usedRAM(final long usedRamInBytes){
        return createIntegerMeasurement(usedRamInBytes, USED_RAM);
    }

    public static FloatingPointMeasurement cpuLoad(final double load) {
        return createFloatingPointMeasurement(load, CPU_LOAD);
    }

    public static IntegerMeasurement freeDiskSpace(final long space){
        return createIntegerMeasurement(space, FREE_DISK_SPACE);
    }

    public static IntegerMeasurement usedDiskSpace(final long space){
        return createIntegerMeasurement(space, USED_DISK_SPACE);
    }

    public static TimeMeasurement responseTime(final long duration, final TimeUnit unit){
        return createTimeMeasurement(duration, unit, RESPONSE_TIME);
    }

    public static IntegerMeasurement rps(final long requests){
        return createIntegerMeasurement(requests, RPS);
    }
}
