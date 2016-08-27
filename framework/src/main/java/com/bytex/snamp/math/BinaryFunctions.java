package com.bytex.snamp.math;

/**
 * Represents a set of binary functions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class BinaryFunctions {
    private BinaryFunctions(){
        throw new InstantiationError();
    }

    /**
     * Computes linear correlation between numbers in the stream.
     * @return Linear correlation function.
     */
    public static StatefulDoubleBinaryFunction correlation(){
        return new StatefulDoubleBinaryFunction() {
            private double sumX;
            private double sumY;
            private double prodX;
            private double prodY;
            private double prodXY;
            private long count;

            @Override
            public void reset() {
                count = 0;
                sumX = 0;
                sumY = 0;
                prodX = 0;
                prodY = 0;
                prodXY = 0;
            }

            @Override
            public double applyAsDouble(final double x, final double y) {
                count += 1;
                sumX += x;
                sumY += y;
                prodX += x * x;
                prodY += y * y;
                prodXY += x * y;
                if(count == Long.MAX_VALUE || Double.isInfinite(sumX) || Double.isInfinite(sumY) || Double.isInfinite(prodX) || Double.isInfinite(prodY) || Double.isInfinite(prodXY)){
                    count = 1;
                    sumX = x;
                    sumY = y;
                    prodX = x * x;
                    prodY = y * y;
                    prodXY = x * y;
                }
                // covariation
                final double cov = prodXY / count - sumX * sumY / (count * count);
                // standard error of x
                final double sigmaX = Math.sqrt(prodX / count -  sumX * sumX / (count * count));
                // standard error of y
                final double sigmaY = Math.sqrt(prodY / count -  sumY * sumY / (count * count));

                // correlation is just a normalized covariation
                return cov / sigmaX / sigmaY;
            }
        };
    }
}
