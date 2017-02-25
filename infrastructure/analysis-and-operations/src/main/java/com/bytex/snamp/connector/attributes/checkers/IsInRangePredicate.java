package com.bytex.snamp.connector.attributes.checkers;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.management.Attribute;
import static com.bytex.snamp.Convert.toDouble;

/**
 * Tests whether the attribute is in specified range.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("isInRange")
public final class IsInRangePredicate implements ColoredAttributePredicate {
    private double from = Double.NaN;
    private boolean fromInclusive;
    private double to = Double.NaN;
    private boolean toInclusive;

    @Override
    public boolean test(final Attribute attribute) {
        final double actual = toDouble(attribute.getValue());
        boolean result = fromInclusive ? Double.compare(actual, from) >= 0 : Double.compare(actual, from) > 0;
        if (result)
            result = toInclusive ? Double.compare(actual, to) <= 0 : Double.compare(actual, to) < 0;
        return result;
    }

    @JsonProperty("rangeStart")
    public double getRangeStart(){
        return from;
    }

    public void setRangeStart(final double value){
        from = value;
    }

    @JsonProperty("isRangeStartInclusive")
    public boolean isRangeStartInclusive(){
        return fromInclusive;
    }

    public void setRangeStartInclusive(final boolean value){
        fromInclusive = value;
    }

    @JsonProperty("rangeEnd")
    public double getRangeEnd(){
        return to;
    }

    public void setRangeEnd(final double value){
        to = value;
    }

    @JsonProperty("isRangeEndInclusive")
    public boolean isRangeEndInclusive(){
        return toInclusive;
    }

    public void setRangeEndInclusive(final boolean value) {
        toInclusive = value;
    }
}
