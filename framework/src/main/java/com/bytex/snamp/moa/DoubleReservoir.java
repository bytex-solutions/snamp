package com.bytex.snamp.moa;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.io.SerializableSnapshotSupport;
import com.bytex.snamp.io.SerializedState;

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

        private DoubleReservoirSnapshot(final DoubleReservoir reservoir) {
            this.values = reservoir.values.clone();
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
        try(final SafeCloseable ignored = acquireWriteLock()) {
            return new DoubleReservoirSnapshot(this);
        }
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
     * Gets casting vote weight depends on the actual size of this reservoir.
     *
     * @return Casting vote weight.
     */
    @Override
    public double getCastingVoteWeight() {
        return Math.floor(actualSize / 2D) + 1;
    }

    /**
     * Proceed voting.
     *
     * @return {@literal true}, if sum of all values in this reservoir is greater or equal than {@link #getCastingVoteWeight()}.
     */
    @Override
    public boolean vote() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            double sum = 0D;
            for(int i = 0; i < actualSize; i++)
                sum += values[i];
            return sum >= getCastingVoteWeight();
        }
    }

    /**
     * Extracts content of this reservoir as array.
     *
     * @return Generic copy of reservoir values.
     */
    @Override
    public double[] toArray() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            return values.clone();
        }
    }

    /**
     * Gets size of this reservoir.
     *
     * @return The size of this reservoir.
     */
    @Override
    public int getSize() {
        return actualSize;
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
     * Gets minimum value in this reservoir.
     * @return Minimum value in this reservoir; or {@link Double#NaN} if reservoir is empty.
     */
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
     * Gets minimum value in this reservoir.
     * @return Minimum value in this reservoir; or {@link Double#NaN} if reservoir is empty.
     */
    public double getMin() {
        try (final SafeCloseable ignored = acquireReadLock()) {
            switch (actualSize) {
                case 0:
                    return Double.NaN;
                default:
                    return values[0]; //values are sorted in ascending order. Therefore zero element is a minimal value.
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

    private double getMeanImpl(){
        double sum = 0D;
        for(int i = 0; i < actualSize; i++)
            sum += values[i];
        return sum / actualSize;
    }

    @Override
    public double getMean(){
        try(final SafeCloseable ignored = acquireReadLock()){
            return getMeanImpl();
        }
    }

    private static float index(final float p, final int length) {
        if (Float.compare(p, 0F) == 0)
            return 0F;
        else if (Float.compare(p, 0F) == 0)
            return length;
        else
            return (length + 1) * p;
    }

    private double getQuantileImpl(final float quantile){
        final float index = index(quantile, actualSize);
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
        try(final SafeCloseable ignored = acquireReadLock()){
            final double index = computeIndex(value);
            return index >= actualSize ? 0D : ((actualSize - index) / actualSize);
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
        try(final SafeCloseable ignored = acquireReadLock()){
            final double index = computeIndex(value);
            return index >= actualSize ? 1D : index / actualSize;
        }
    }
}
