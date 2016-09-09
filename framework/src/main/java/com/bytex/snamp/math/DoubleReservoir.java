package com.bytex.snamp.math;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;

import java.util.Arrays;
import java.util.function.DoubleConsumer;
import java.util.function.IntToDoubleFunction;

/**
 * Provides reservoir of {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DoubleReservoir extends ThreadSafeObject implements DoubleConsumer, IntToDoubleFunction, Reservoir {
    private final double[] values;
    private int actualSize;

    /**
     * Initializes a new reservoir.
     * @param samplingSize A size of this reservoir.
     */
    public DoubleReservoir(final int samplingSize){
        super(SingleResourceGroup.class);
        if(samplingSize < 2)
            throw new IllegalArgumentException("Sampling size cannot be less than 2");
        this.values = new double[samplingSize];
        this.actualSize = 0;
    }

    private SafeCloseable acquireWriteLock(){
        return acquireWriteLock(SingleResourceGroup.INSTANCE);
    }

    private SafeCloseable acquireReadLock(){
        return acquireReadLock(SingleResourceGroup.INSTANCE);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public final void reset() {
        try(final SafeCloseable ignored = acquireWriteLock()){
            actualSize = 0;
            Arrays.fill(values, 0D);
        }
    }

    /**
     * Gets size of this reservoir.
     *
     * @return The size of this reservoir.
     */
    @Override
    public final int getSize() {
        try(final SafeCloseable ignored = acquireReadLock()){
            return actualSize;
        }
    }

    /**
     * Gets capacity of this reservoir.
     *
     * @return The capacity of this reservoir.
     */
    @Override
    public final int getCapacity() {
        return values.length;
    }

    private double getDeviationImpl(){
        if (actualSize <= 1) return 0;

        final double mean = getMean();
        double variance = 0;

        for (int i = 0; i < actualSize; i++) {
            final double diff = values[i] - mean;
            variance +=  diff*diff;
        }

        return Math.sqrt(variance);
    }

    /**
     * Gets standard deviation of the values in this reservoir.
     *
     * @return The standard deviation of the values in this reservoir.
     */
    @Override
    public double getDeviation() {
        try(final SafeCloseable ignored = acquireReadLock()){
            return getDeviationImpl();
        }
    }

    //we use binarySearch-derived algorithm for value insertion
    //this is more efficient because insertion is O(log n) in comparison with O(1) insertion and O(n log n) quick sorting when reading reservoir
    private int computeIndex(final double item) {
        if (actualSize == 0)
            return 0;
        int low = 0;
        int high = actualSize - 1;
        while (low <= high) {
            final int midIndex = (high + low) >>> 1;   //(high + low) / 2
            final double midValue = values[midIndex];
            final int comparisonResult = Double.compare(item, midValue);
            if (comparisonResult > 0)    //input > midValue
                low = midIndex + 1;
            else if (comparisonResult < 0)
                high = midIndex - 1;
            else
                break;
        }
        return Math.max(low, high);
    }

    /**
     * Adds a new value to this reservoir.
     * @param value A value to add.
     */
    public final void add(final double value){
        try (final SafeCloseable ignored = acquireWriteLock()) {
            int index = computeIndex(value);
            if (actualSize < values.length) {  //buffer is not fully occupied
                if (index < actualSize) //shift array to right and utilize more buffer space
                    System.arraycopy(values, index, values, index + 1, actualSize - index);
                actualSize += 1; //increase occupation factor
            } else   //buffer is fully occupied
                if(index == actualSize)
                    index -= 1;
            values[index] = value;
        }
    }

    /**
     * Adds a new value to this reservoir.
     *
     * @param value A value to add.
     */
    @Override
    public final void add(final Number value) {
        add(value.doubleValue());
    }

    /**
     * Gets a value in this reservoir.
     * @param index Index of the value.
     * @return Value inside of this reservoir.
     * @throws IndexOutOfBoundsException Incorrect index.
     */
    public final double get(final int index) {
        try (final SafeCloseable ignored = acquireReadLock()) {
            if (index < actualSize)
                return values[index];
            else
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Gets a value in this reservoir.
     * @param index Index of the value.
     * @return Value inside of this reservoir.
     * @throws IndexOutOfBoundsException Incorrect index.
     */
    @Override
    public final double applyAsDouble(final int index) {
        return get(index);
    }

    /**
     * Adds a new value to this reservoir.
     * @param value A value to add.
     */
    public final void accept(final double value) {
        add(value);
    }

    private double getMeanImpl(){
        double sum = 0D;
        for(int i = 0; i < actualSize; i++)
            sum += values[i];
        return sum / actualSize;
    }

    @Override
    public final double getMean(){
        try(final SafeCloseable ignored = acquireReadLock()){
            return getMeanImpl();
        }
    }

    private static double index(final double p, final int length) {
        if (Double.compare(p, 0D) == 0)
            return 0F;
        else if (Double.compare(p, 1D) == 0)
            return length;
        else
            return (length + 1) * p;
    }

    private double getQuantileImpl(final double quantile){
        final double index = index(quantile, actualSize);
        if (index < 1)
            return values[0];
        else if (index >= actualSize)
            return values[actualSize - 1];
        else {
            final double fpos = Math.floor(index);
            final double lower = values[(int)fpos - 1];
            final double upper = values[(int)fpos];
            return lower + (index - fpos) * (upper - lower);
        }
    }

    @Override
    public final double getQuantile(final double quantile) {
        try(final SafeCloseable ignored = acquireReadLock()){
            return getQuantileImpl(quantile);
        }
    }
}
