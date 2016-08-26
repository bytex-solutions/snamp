package com.bytex.snamp.math;

/**
 * Represents percentile function.
 */
final class PercentileFunction extends SortedArrayList<Double> implements StatefulDoubleUnaryFunction {
    private final int maxSize;
    private final double percentile;
    private final boolean truncateFromBegin;

    PercentileFunction(final int maxSize, final double percentile) {
        super(5 + maxSize + (maxSize / 10), Double::compare);
        this.maxSize = maxSize;
        this.percentile = percentile;
        this.truncateFromBegin = percentile > 0.5;
    }

    @Override
    public void reset() {
        clear();
    }

    private static double index(final double p, final int length) {
        if (Double.compare(p, 0D) == 0)
            return 0F;
        else if (Double.compare(p, 0D) == 0)
            return length;
        else
            return (length + 1) * p;
    }

    @Override
    public double applyAsDouble(final double operand) {
        add(operand);
        if(size() > maxSize)    //shrink array
            remove(truncateFromBegin ? maxSize : 0);

        final double index = index(percentile, size());
        if (index < 1)
            return get(0);
        else if (index >= size())
            return get(size() - 1);
        else {
            final double fpos = Math.floor(index);
            final double lower = get((int)fpos - 1);
            final double upper = get((int)fpos);
            return lower + (index - fpos) * (upper - lower);
        }
    }
}
