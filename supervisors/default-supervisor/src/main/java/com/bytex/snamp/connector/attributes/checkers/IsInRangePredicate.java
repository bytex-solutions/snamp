package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.SpecialUse;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.management.Attribute;
import java.util.OptionalDouble;

import static com.bytex.snamp.Convert.toDouble;

/**
 * Tests whether the attribute is in specified range.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("isInRange")
public final class IsInRangePredicate implements ColoredAttributePredicate {
    private double from;
    private boolean fromInclusive;
    private double to;
    private boolean toInclusive;

    public IsInRangePredicate(final double from, final boolean fromInclusive, final double to, final boolean toInclusive){
        this.from = from;
        this.fromInclusive = fromInclusive;
        this.to = to;
        this.toInclusive = toInclusive;
    }

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public IsInRangePredicate(){
        this(Double.NaN, false, Double.NaN, false);
    }

    private boolean test(final double actual) {
        return (fromInclusive ? Double.compare(actual, from) >= 0 : Double.compare(actual, from) > 0) &&
                (toInclusive ? Double.compare(actual, to) <= 0 : Double.compare(actual, to) < 0);
    }

    @Override
    public boolean test(final Attribute attribute) {
        final OptionalDouble actual = toDouble(attribute.getValue());
        return actual.isPresent() && test(actual.getAsDouble());
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
