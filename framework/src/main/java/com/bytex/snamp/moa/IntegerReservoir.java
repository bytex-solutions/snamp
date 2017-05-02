package com.bytex.snamp.moa;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.io.SerializableSnapshotSupport;
import com.bytex.snamp.io.SerializedState;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.IntConsumer;

/**
 * Provides reservoir of {@code int} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class IntegerReservoir extends AbstractReservoir implements IntConsumer {
    private static final class IntegerReservoirSnapshot extends ReservoirSnapshot<IntegerReservoir> {
        private static final long serialVersionUID = -6080572664395210068L;
        private final int[] values;

        private IntegerReservoirSnapshot(final IntegerReservoir reservoir) {
            super(reservoir.actualSize);
            this.values = reservoir.values.clone();
        }

        @Override
        public IntegerReservoir get() {
            return new IntegerReservoir(this);
        }
    }
    private final int[] values;

    private IntegerReservoir(final IntegerReservoirSnapshot snapshot) {
        super(snapshot);
        values = snapshot.values;
    }

    /**
     * Initializes a new reservoir.
     * @param samplingSize A size of this reservoir.
     */
    public IntegerReservoir(final int samplingSize){
        if(samplingSize < 2)
            throw new IllegalArgumentException("Sampling size cannot be less than 2");
        this.values = new int[samplingSize];
    }

    @Override
    public IntegerReservoirSnapshot takeSnapshot() {
        try (final SafeCloseable ignored = acquireWriteLock()) {
            return new IntegerReservoirSnapshot(this);
        }
    }

    private int sumImpl() {
        int sum = 0;
        for (int i = 0; i < actualSize; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Computes sum of all elements in this reservoir.
     * @return Sum of all elements in this reservoir.
     */
    public int sum() {
        try (final SafeCloseable ignored = acquireWriteLock()) {
            return sumImpl();
        }
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        try (final SafeCloseable ignored = acquireWriteLock()) {
            super.reset();
            Arrays.fill(values, 0);
        }
    }

    /**
     * Extracts content of this reservoir as array.
     *
     * @return Generic copy of reservoir values.
     */
    @Override
    public int[] toArray() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            return values.clone();
        }
    }

    /**
     * Gets capacity of this reservoir.
     *
     * @return The capacity of this reservoir.
     */
    @Override
    public int getCapacity() {
        return values.length;
    }

    /**
     * Gets maximum value in this reservoir.
     * @return Maximum value in this reservoir; or empty value if reservoir is empty.
     */
    public OptionalInt getMax() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            switch (actualSize) {
                case 0:
                    return OptionalInt.empty();
                default:
                    return OptionalInt.of(values[actualSize - 1]);
            }
        }
    }

    /**
     * Gets minimum value in this reservoir.
     * @return Minimum value in this reservoir; or empty value if reservoir is empty.
     */
    public OptionalInt getMin() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            switch (actualSize) {
                case 0:
                    return OptionalInt.empty();
                default:
                    return OptionalInt.of(values[0]); //values are sorted in ascending order. Therefore zero element is a minimal value.
            }
        }
    }

    private double getDeviationImpl() {
        if (actualSize <= 1) return 0;

        final double mean = getMeanImpl();
        double variance = 0;

        for (int i = 0; i < actualSize; i++) {
            final double diff = values[i] - mean;
            variance += diff * diff;
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
    //this is more efficient because insertion is O(log n) in contrast with O(1) insertion and O(n log n) quick sorting when reading reservoir
    private int computeIndex(final int item) {
        if (actualSize == 0)
            return 0;
        int low = 0;
        int high = actualSize - 1;
        while (low <= high) {
            final int midIndex = (high + low) >>> 1;   //(high + low) / 2
            switch (Integer.compare(item, values[midIndex])) {
                case 1:
                    low = midIndex + 1;
                    continue;
                case -1:
                    high = midIndex - 1;
                    continue;
            }
            break;
        }
        return Integer.max(low, high);
    }

    /**
     * Adds a new value to this reservoir.
     * @param value A value to add.
     */
    public void add(final int value){
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
    public void add(final Number value) {
        add(value.intValue());
    }

    /**
     * Gets a value in this reservoir.
     * @param index Index of the value.
     * @return Value inside of this reservoir.
     * @throws IndexOutOfBoundsException Incorrect index.
     */
    public int get(final int index) {
        try (final SafeCloseable ignored = acquireReadLock()) {
            if (index < actualSize)
                return values[index];
            else
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Adds a new value to this reservoir.
     * @param value A value to add.
     */
    @Override
    public void accept(final int value) {
        add(value);
    }

    private double getMeanImpl() {
        final double sum = sumImpl();
        return sum / actualSize;
    }

    @Override
    public double getMean(){
        try(final SafeCloseable ignored = acquireReadLock()){
            return getMeanImpl();
        }
    }

    private double getQuantileImpl(final float quantile){
        final float index = getIndexForQuantile(quantile, actualSize);
        if (index < 1)
            return values[0];
        else if (index >= actualSize)
            return values[actualSize - 1];
        else {
            final int fpos = (int)index;    //do not use floor, because index is positive
            final double lower = values[fpos - 1];
            final double upper = values[fpos];
            return lower + (index - fpos) * (upper - lower);
        }
    }

    @Override
    public double getQuantile(final float quantile) {
        try(final SafeCloseable ignored = acquireReadLock()){
            return getQuantileImpl(quantile);
        }
    }

    /**
     * Finds location of the value in this reservoir.
     *
     * @param value The value to find.
     * @return The location of the value in this reservoir
     */
    @Override
    public OptionalInt find(final Number value) {
        return find(value.intValue());
    }

    /**
     * Finds location of the value in this reservoir.
     *
     * @param value The value to find.
     * @return The location of the value in this reservoir
     */
    public OptionalInt find(final int value){
        final int index;
        try(final SafeCloseable ignored = acquireReadLock()){
            index = Arrays.binarySearch(values, value);   //array is always sorted
        }
        return index < 0 ? OptionalInt.empty() : OptionalInt.of(index);
    }

    /**
     * Computes a percent of values that are greater than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are greater that or equal to the specified value.
     */
    @Override
    public double greaterThanOrEqualValues(final Number value) {
        return greaterThanOrEqualValues(value.intValue());
    }

    /**
     * Computes a percent of values that are greater than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are greater that or equal to the specified value.
     */
    public double greaterThanOrEqualValues(final int value) {
        try (final SafeCloseable ignored = acquireReadLock()) {
            final int index = computeIndex(value);
            return index >= actualSize ? 0D : ((actualSize - (double) index) / actualSize);
        }
    }

    /**
     * Computes a percent of values that are less than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are less that or equal to the specified value.
     */
    @Override
    public double lessThanOrEqualValues(final Number value) {
        return lessThanOrEqualValues(value.intValue());
    }

    /**
     * Computes a percent of values that are less than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are less that or equal to the specified value.
     */
    public double lessThanOrEqualValues(final int value) {
        try(final SafeCloseable ignored = acquireReadLock()){
            final double index = computeIndex(value);
            return index >= actualSize ? 1D : index / actualSize;
        }
    }
}
