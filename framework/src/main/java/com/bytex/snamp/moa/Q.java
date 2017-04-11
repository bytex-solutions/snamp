package com.bytex.snamp.moa;

/**
 * Provides utility methods for computing different metrics provided by Queue Theory.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Q {
    private static final int MAX_ARG = 170;//170! is the maximum factorial value for DOUBLE data type
    private static final double[] FACTORIAL = new double[MAX_ARG + 1];  //cached array of all factorial products

    static {
        double acc;
        FACTORIAL[0] = acc = 1D;
        for (int i = 1; i < FACTORIAL.length; i++)
            FACTORIAL[i] = (acc *= i);
    }

    private Q(){
        throw new InstantiationError();
    }

    /**
     * Computes factorial of the specified argument.
     * @param x Argument of the factorial.
     * @return Factorial product.
     */
    public static double factorial(final int x) {
        if (x > MAX_ARG)     
            return Double.POSITIVE_INFINITY;
        else if (x < 0)
            return 1D;
        else
            return FACTORIAL[x];
    }

    public static double getAvailability(final double rps, final double responseTimeInSeconds, final int channels) {
        if (channels == 0)
            return 0;
        else if (responseTimeInSeconds == 0D || rps == 0D)
            return 1D;
        else {
            //http://latex.codecogs.com/gif.latex?\rho=\lambda\times&space;t
            final double intensity = rps * responseTimeInSeconds; //workload intensity
            //http://latex.codecogs.com/gif.latex?p_{0}=\frac{1}{\sum_{i=0}^{k}\frac{\rho^{i}}{i!}}
            double denialProbability = 1D;
            for (int i = 1; i <= channels; i++)
                denialProbability += (Math.pow(intensity, i) / factorial(i));
            denialProbability = 1D / denialProbability;
            //http://latex.codecogs.com/gif.latex?P=1-\frac{\rho^{k}}{k!}\rho_{0}
            return denialProbability == 0D ?
                    1D :         //little optimization
                    (1D - (Math.pow(intensity, channels) / channels) * denialProbability);  //availability
        }
    }
}
