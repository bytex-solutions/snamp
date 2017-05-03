package com.bytex.snamp.moa;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.io.SerializableSnapshotSupport;
import com.bytex.snamp.io.SerializedState;

import java.io.Serializable;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.DoubleConsumer;
import java.util.function.IntToDoubleFunction;

/**
 * Provides reservoir of {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DoubleReservoir extends ThreadSafeObject implements DoubleConsumer, IntToDoubleFunction, Reservoir, SerializableSnapshotSupport<DoubleReservoir> {
    private static final class DoubleReservoirSnapshot extends SerializedState<DoubleReservoir>{
        private static final long serialVersionUID = -6080572664395210068L;
        private final double[] values;
        private final int actualSize;

        private DoubleReservoirSnapshot(final DoubleReservoir reservoir){
            this.values = reservoir.values;
            this.actualSize = reservoir.actualSize;
        }

        @Override
        public DoubleReservoir get() {
            return new DoubleReservoir(this);
        }
    }

    private static final long serialVersionUID = -2597353518482200745L;
    private final double[] values;
    private int actualSize;

    private DoubleReservoir(final DoubleReservoirSnapshot snapshot){
        super(SingleResourceGroup.class);
        values = snapshot.values;
        actualSize = snapshot.actualSize;
    }

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

    @Override
    public DoubleReservoirSnapshot takeSnapshot() {
        return new DoubleReservoirSnapshot(this);
    }

    @Override
    public Object writeReplace() {
        return takeSnapshot();
    }

    private SafeCloseable acquireWriteLock(){
        return writeLock.acquireLock(SingleResourceGroup.INSTANCE);
    }

    private SafeCloseable acquireReadLock(){
        return readLock.acquireLock(SingleResourceGroup.INSTANCE);
    }

    public double getMin() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            switch (actualSize) {
                case 0:
                    return Double.NaN;
                default:
                    return values[0];
            }
        }
    }

    public double getMax() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            switch (actualSize) {
                case 0:
                    return Double.NaN;
                default:
                    return values[actualSize - 1];
            }
        }
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
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
    public int getSize() {
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
    public int getCapacity() {
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
    //this is more efficient because insertion is O(log n) in contrast with O(1) insertion and O(n log n) quick sorting when reading reservoir
    private int computeIndex(final double item) {
        if (actualSize == 0)
            return 0;
        int low = 0;
        int high = actualSize - 1;
        while (low <= high) {
            final int midIndex = (high + low) >>> 1;   //(high + low) / 2
            switch (Double.compare(item, values[midIndex])) {
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
    public void add(final double value){
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
        add(value.doubleValue());
    }

    /**
     * Gets a value in this reservoir.
     * @param index Index of the value.
     * @return Value inside of this reservoir.
     * @throws IndexOutOfBoundsException Incorrect index.
     */
    public double get(final int index) {
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
    public double applyAsDouble(final int index) {
        return get(index);
    }

    /**
     * Adds a new value to this reservoir.
     * @param value A value to add.
     */
    public void accept(final double value) {
        add(value);
    }

    private double sumImpl(){
        double sum = 0D;
        for(int i = 0; i < actualSize; i++)
            sum += values[i];
        return sum;
    }

    /**
     * Computes sum of all elements in this reservoir.
     * @return Sum of all elements in this reservoir.
     */
    public double getSum() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            return sumImpl();
        }
    }

    private double getMeanImpl(){
        return sumImpl() / actualSize;
    }

    @Override
    public double getMean(){
        try(final SafeCloseable ignored = acquireReadLock()){
            return getMeanImpl();
        }
    }

    private static float getIndexForQuantile(final float q, final int length) {
        if (Double.compare(q, 0D) == 0)
            return 0F;
        else if (Double.compare(q, 1D) == 0)
            return length;
        else
            return (length + 1) * q;
    }

    private double getQuantileImpl(final float quantile){
        final double index = getIndexForQuantile(quantile, actualSize);
        if (index < 1)
            return values[0];
        else if (index >= actualSize)
            return values[actualSize - 1];
        else {
            final int fpos = (int) index;   //no need to use Math.floor because index is always positive value
            final double lower = values[(int)fpos - 1];
            final double upper = values[(int)fpos];
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
        return find(value.doubleValue());
    }

    /**
     * Finds location of the value in this reservoir.
     *
     * @param value The value to find.
     * @return The location of the value in this reservoir
     */
    public OptionalInt find(final double value){
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
        return greaterThanOrEqualValues(value.doubleValue());
    }

    /**
     * Computes a percent of values that are greater than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are greater that or equal to the specified value.
     */
    public double greaterThanOrEqualValues(final double value) {
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
        return lessThanOrEqualValues(value.doubleValue());
    }

    /**
     * Computes a percent of values that are less than or equal to the specified value.
     *
     * @param value A value to compute.
     * @return A percent of values that are less that or equal to the specified value.
     */
    public double lessThanOrEqualValues(final double value) {
        try (final SafeCloseable ignored = acquireReadLock()) {
            final int index = computeIndex(value);
            return index >= actualSize ? 1D : (double) index / actualSize;
        }
    }

    @Override
    public Serializable toArray() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            return values.clone();
        }
    }
}
