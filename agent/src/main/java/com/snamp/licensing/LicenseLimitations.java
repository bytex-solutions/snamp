package com.snamp.licensing;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Represents an abstract class for all licensed objects.
 * @author roman
 */
public abstract class LicenseLimitations implements Iterable<String> {
    /**
     * Initializes a new licensed object.
     */
    protected LicenseLimitations(){
    }

    /**
     * Represents a limitation for the specified licensed object.
     * @param <T>
     */
    public static interface Limitation<T>{
        /**
         * Creates a new licensing exception.
         * @return A new instance of the licensing exception.
         */
        public LicensingException createException();

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        public boolean validate(final T actualValue);
    }

    /**
     * Represents XML adapter for the limitation type.
     * @param <T> Type of the limitation parameter.
     * @param <L> Type of the limitation implementer.
     */
    protected static abstract class LimitationAdapter<T, L extends Limitation<T>> extends XmlAdapter<T, L>{

    }

    /**
     * Represents limitation based on the expected value.
     * @param <A> Type of the actual value.
     * @param <E> Type of the expected value.
     */
    protected static abstract class Expectation<A, E> implements Limitation<A>{
        /**
         * Represents expected value.
         */
        protected final E expectedValue;

        protected Expectation(final E expectedValue){
            this.expectedValue = expectedValue;
        }
    }

    /**
     * Represents XML adapter for the expected value.
     * @param <A> Type of the actual value.
     * @param <E> Type of the expected value.
     * @param <L> Type of the limitation based on the expected value.
     */
    protected static abstract class ExpectationAdapter<A, E, L extends Expectation<A, E>> extends XmlAdapter<E, L>{

        /**
         * Converts the limitation to the expected value.
         * @param limitation The limitation to convert.
         * @return The expected value.
         */
        @Override
        public final E marshal(final L limitation) {
            return limitation.expectedValue;
        }
    }

    protected static abstract class MaxValueLimitation<T> extends Expectation<Comparable<T>, T>{
        protected MaxValueLimitation(final T expectedValue){
            super(expectedValue);
        }

        @Override
        public final boolean validate(final Comparable<T> actualValue) {
            return actualValue.compareTo(expectedValue) <= 0;
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
        try {
            final Field limitationHolder = getClass().getDeclaredField(limitationName);
            limitationHolder.setAccessible(true);
            if(isLimitationHolder(limitationHolder)){
                verify((Limitation<T>)limitationHolder.get(this), actualValue);
            }
        }
        catch (final ReflectiveOperationException e) {
            final Limitation<T> nullLim = null;
            verify(nullLim, actualValue);
        }

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
}
