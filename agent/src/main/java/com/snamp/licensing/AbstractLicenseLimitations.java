package com.snamp.licensing;

import com.snamp.*;

import javax.xml.bind.annotation.adapters.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Represents an abstract class for all licensed objects.
 * @author roman
 */
public abstract class AbstractLicenseLimitations implements LicenseLimitations {
    /**
     * Initializes a new licensed object.
     */
    protected AbstractLicenseLimitations(){
    }

    /**
     * Represents XML adapter for the limitation type.
     * @param <T> Type of the limitation parameter.
     * @param <L> Type of the limitation implementer.
     */
    protected static abstract class LimitationAdapter<T, L extends Limitation<T>> extends XmlAdapter<T, L>{

    }

    protected static <T extends AbstractLicenseLimitations> T current(final Class<T> limitationsHolder, final Activator<T> fallback){
        return LicenseReader.getLimitations(limitationsHolder, fallback);
    }

    /**
     * Represents limitation based on the expected value.
     * @param <A> Type of the actual value.
     * @param <E> Type of the expected value.
     */
    protected static abstract class Requirement<A, E> implements Limitation<A>{
        /**
         * Represents expected value.
         */
        protected final E requiredValue;

        protected Requirement(final E requiredValue){
            this.requiredValue = requiredValue;
        }
    }

    /**
     * Represents XML adapter for the expected value.
     * @param <A> Type of the actual value.
     * @param <E> Type of the expected value.
     * @param <L> Type of the limitation based on the expected value.
     */
    protected static abstract class RequirementParser<A, E, L extends Requirement<A, E>> extends XmlAdapter<E, L>{

        /**
         * Converts the limitation to the expected value.
         * @param limitation The limitation to convert.
         * @return The expected value.
         */
        @Override
        public final E marshal(final L limitation) {
            return limitation.requiredValue;
        }
    }

    protected static abstract class MaxValueLimitation<T> extends Requirement<Comparable<T>, T> {
        protected MaxValueLimitation(final T expectedValue){
            super(expectedValue);
        }

        @Override
        public final boolean validate(final Comparable<T> actualValue) {
            return actualValue.compareTo(requiredValue) <= 0;
        }
    }

    protected static abstract class MinValueLimitation<T> extends Requirement<Comparable<T>, T> {
        protected MinValueLimitation(final T expectedValue){
            super(expectedValue);
        }

        @Override
        public final boolean validate(final Comparable<T> actualValue){
            return actualValue.compareTo(requiredValue) >= 0;
        }
    }

    protected static abstract class ExactLimitation<T> extends Requirement<T, T> {
        protected ExactLimitation(final T expectedValue){
            super(expectedValue);
        }

        @Override
        public final boolean validate(final T actualValue){
            return Objects.equals(actualValue, requiredValue);
        }
    }

    protected static abstract class NotExactLimitation<T> extends Requirement<T, T> {
        protected NotExactLimitation(final T expectedValue){
            super(expectedValue);
        }

        @Override
        public final boolean validate(final T actualValue){
            return !Objects.equals(actualValue, requiredValue);
        }
    }

    protected static abstract class RangeLimitation<T extends Comparable<T>> extends Requirement<T, Range<T>> {
        private final Range.InclusionTestType testType;

        protected RangeLimitation(final Range<T> range, final Range.InclusionTestType testType){
            super(range);
            this.testType = testType;
        }

        protected RangeLimitation(final T lowerBound, final T upperBound, final Range.InclusionTestType testType){
            this(new Range<>(lowerBound, upperBound), testType);
        }

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         *
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        @Override
        public final boolean validate(final T actualValue) {
            return requiredValue.contains(actualValue, testType);
        }
    }

    protected static abstract class VersionLimitation extends Requirement<String, String> {
        protected VersionLimitation(final String expectedVersion){
            super(expectedVersion);
        }

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         *
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        @Override
        public final boolean validate(final String actualValue) {
            switch (requiredValue){
                case "*": return true;
                default: return requiredValue.compareTo(actualValue) >= 0;
            }
        }
    }

    /**
     * Throws an exception if the specified parameter value breaks the license limitation.
     * @param limitation
     * @param actualValue
     * @param <T>
     * @throws LicensingException
     */
    protected static final <T> void verify(final Limitation<T> limitation, final T actualValue) throws LicensingException{
        if(limitation != null && !limitation.validate(actualValue))
            throw limitation.createException();
    }

    private static interface FieldConverter<T>{
        public T convert(final Field f);
    }

    private static boolean isLimitationHolder(final Field f){
        return f != null &&
                Limitation.class.isAssignableFrom(f.getType()) &&
                f.isAnnotationPresent(XmlJavaTypeAdapter.class);
    }

    private final <T> List<T> getLimitationHolders(final FieldConverter<T> converter){
        final Field[] declaredFields = getClass().getDeclaredFields();
        final List<T> result = new ArrayList<>(declaredFields.length);
        for(final Field f: declaredFields)
            if(isLimitationHolder(f))
                result.add(converter.convert(f));
        return result;
    }

    /**
     * Throws an exception if the specified parameter value breaks the license limitation.
     * @param limitationName The limitation name.
     * @param actualValue The actual value of the restricted parameter.
     * @param <T> Type of the value of the restricted parameter.
     * @throws LicensingException The exception that is thrown if parameter value breaks the license limitation.
     */
    public final <T> void verify(final String limitationName, final T actualValue) throws LicensingException {
        verify(getLimitationCore(limitationName), actualValue);

    }

    /**
     * Returns an iterator through all available limitations.
     * @return An iterator through all available limitations.
     */
    @Override
    public final Iterator<String> iterator() {
        return getLimitationHolders(new FieldConverter<String>() {
            @Override
            public String convert(final Field f) {
                return f.getName();
            }
        }).iterator();
    }

    private final <T> Limitation<T> getLimitationCore(final String limitationName) {
        try {
            final Field limitationHolder = getClass().getDeclaredField(limitationName);
            limitationHolder.setAccessible(true);
            return isLimitationHolder(limitationHolder) ? (Limitation<T>)limitationHolder.get(this) : null;
        }
        catch (final ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Returns the restricted parameter by its name.
     *
     * @param limitationName The name of the restricted parameter.
     * @return An instance of the limitation descriptor.
     */
    @Override
    public final Limitation<?> getLimitation(final String limitationName) {
        return getLimitationCore(limitationName);
    }
}
