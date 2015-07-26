package com.bytex.snamp.connectors.modbus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a range of integer numbers.
 * This class cannot be inherited.
 */
final class IntegerRange {
    private static final Pattern FORMAT = Pattern.compile("(?<lower>[0-9]+)\\.\\.(?<upper>[0-9]+)");
    private final int lowerBound;
    private final int upperBound;

    IntegerRange(final int lowerBound, final int upperBound){
        checkRange(lowerBound, upperBound);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    IntegerRange(final String range) {
        final Matcher result = FORMAT.matcher(range);
        if (result.matches()) {
            lowerBound = Integer.parseInt(result.group("lower"));
            upperBound = Integer.parseInt(result.group("upper"));
            checkRange(lowerBound, upperBound);
        } else throw new IllegalArgumentException("Incorrect range: " + range);
    }

    public int size() {
        return upperBound - lowerBound + 1;
    }

    private static void checkRange(final int lowerBound, final int upperBound){
        if(upperBound < lowerBound || lowerBound < 0)
            throw new IndexOutOfBoundsException(String.format("Incorrect range: %s..%s", lowerBound, upperBound));
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return lowerBound + ".." + upperBound;
    }
}
