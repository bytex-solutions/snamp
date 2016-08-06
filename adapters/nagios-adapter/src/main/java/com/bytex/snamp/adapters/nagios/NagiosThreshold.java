package com.bytex.snamp.adapters.nagios;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents threshold in Nagios format.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NagiosThreshold implements Predicate<Number> {
    //https://nagios-plugins.org/doc/guidelines.html#THRESHOLDFORMAT
    private static final Pattern THRESHOLD_FORMAT = Pattern.compile("(?<inverse>@)?(?<lower>~|[0-9]+)((?<delim>:)(?<upper>[0-9]*))?");

    private final Predicate<BigDecimal> rangeChecker;
    private final String thresholdValue;

    public NagiosThreshold(final String threshold) throws IllegalArgumentException {
        final Matcher result = THRESHOLD_FORMAT.matcher(this.thresholdValue = threshold);
        if (result.matches()) {
            final boolean inverse = !isNullOrEmpty(result.group("inverse"));
            final String lowerBound = result.group("lower");
            final boolean isNegativeInfinity = Objects.equals("~", lowerBound);
            final String upperBound = result.group("upper");
            final boolean isPositiveInfinity = !isNullOrEmpty(result.group("delim")) &&
                    isNullOrEmpty(upperBound);
            final Predicate<BigDecimal> predicate;
            if (isPositiveInfinity)
                predicate = Range.atLeast(new BigDecimal(lowerBound))::contains;
            else if (isNegativeInfinity)
                predicate = Range.atMost(new BigDecimal(upperBound))::contains;
            else if (!isNullOrEmpty(upperBound))
                predicate = Range.closed(new BigDecimal(lowerBound), new BigDecimal(upperBound))::contains;
            else predicate = Range.closed(BigDecimal.ZERO, new BigDecimal(lowerBound))::contains;
            this.rangeChecker = inverse ?
                    predicate.negate() :
                    predicate;
        } else throw new IllegalArgumentException(String.format("'%s' is not a threshold", threshold));
    }

    public NagiosThreshold(final Number value, final DecimalFormat format){
        this(format.format(value));
    }

    public boolean check(final Number value) {
        if (value instanceof BigDecimal)
            return rangeChecker.test((BigDecimal) value);
        else if (value instanceof BigInteger)
            return rangeChecker.test(new BigDecimal((BigInteger) value));
        else return rangeChecker.test(new BigDecimal(value.doubleValue()));
    }

    public boolean check(final String value, final DecimalFormat format) throws ParseException {
        return check(format.parse(value));
    }

    @Override
    public boolean test(final Number value) {
        return check(value);
    }

    /**
     * Gets threshold in Nagios format.
     * @return Nagios threshold.
     */
    public String getValue(){
        return thresholdValue;
    }

    /**
     * Returns a string that simplifies debugging of this class.
     * @return A string that simplifies debugging of this class.
     */
    @Override
    public String toString() {
        return rangeChecker.toString();
    }
}
