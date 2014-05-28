package com.itworks.snamp.licensing;

import com.itworks.snamp.core.FrameworkService;
import org.apache.commons.collections4.Factory;
import org.apache.commons.lang3.Range;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Field;
import java.util.*;

import static com.itworks.snamp.core.AbstractServiceLibrary.RequiredServiceAccessor;

/**
 * Represents an abstract class for all licensed objects.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
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
        protected RangeLimitation(final Range<T> range){
            super(range);
        }

        @SuppressWarnings("UnusedDeclaration")
        protected RangeLimitation(final T lowerBound, final T upperBound){
            this(Range.between(lowerBound, upperBound));
        }

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         *
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        @Override
        public final boolean validate(final T actualValue) {
            return requiredValue.contains(actualValue);
        }
    }

    protected static abstract class VersionLimitation extends Requirement<Version, String> {
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
        public final boolean validate(final Version actualValue) {
            switch (requiredValue){
                case "*": return true;
                default:
                    return actualValue.compareTo(new Version(requiredValue.replaceAll("\\*", Integer.toString(Integer.MAX_VALUE)))) <= 0;
            }
        }
    }

    /**
     * Throws an exception if the specified parameter value breaks the license limitation.
     * @param limitation The limitation to verify.
     * @param actualValue The actual value to verify.
     * @param <T> Type of the limitation to verify.
     * @throws LicensingException Verification failed.
     */
    protected static <T> void verify(final Limitation<T> limitation, final T actualValue) throws LicensingException{
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

    private <T> List<T> getLimitationHolders(final FieldConverter<T> converter){
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
     * @throws LicensingException The exception that is thrown if parameter value breaks the license limitation.
     */
    public final void verify(final String limitationName, final Object actualValue) throws LicensingException {
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

    private Limitation getLimitationCore(final String limitationName) {
        try {
            final Field limitationHolder = getClass().getDeclaredField(limitationName);
            limitationHolder.setAccessible(true);
            return isLimitationHolder(limitationHolder) ? (Limitation<?>)limitationHolder.get(this) : null;
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

    private static Version getActualPluginVersion(final Class<? extends FrameworkService> pluginType){
        final Bundle owner = FrameworkUtil.getBundle(pluginType);
        return owner != null ? owner.getVersion() : Version.emptyVersion;
    }

    /**
     * Throws {@link LicensingException} if the actual plugin version is greater than the specified in
     * the license limitation.
     * @param expectedImplVersion The version defined in the license.
     * @param pluginType The plug-in to check.
     * @throws LicensingException The plugin has greater implementation version than the specified implementation version in the
     * license limitation.
     */
    protected static void verifyPluginVersion(final VersionLimitation expectedImplVersion, final Class<? extends FrameworkService> pluginType) throws LicensingException{
        verify(expectedImplVersion, getActualPluginVersion(pluginType));
    }

    /**
     * Returns the localized description of this object.
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this object.
     */
    @Override
    public String getDescription(final Locale locale) {
        final StringBuilder result = new StringBuilder();
        for(final String limName: this){
            result.append(String.format("%s - %s.", limName, Objects.toString(getLimitation(limName), "")));
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    /**
     * Returns the active license limitations.
     * @param limitationsType The limitations type. Cannot be {@literal null}.
     * @param licenseReader Dependency descriptor for {@link com.itworks.snamp.licensing.LicenseReader}. Cannot be {@literal null}.
     * @param fallbackFactory A factory that produces fully limited descriptor of the license. Cannot be {@literal null}.
     * @param <L> Type of the license limitations descriptor.
     * @return An instance of the license limitations descriptor.
     */
    protected static <L extends AbstractLicenseLimitations> L current(final Class<L> limitationsType,
                                                                      final RequiredServiceAccessor<LicenseReader> licenseReader,
                                                                      final Factory<L> fallbackFactory){
        if(limitationsType == null) throw new IllegalArgumentException("limitationsType is null.");
        else if(licenseReader == null) throw new IllegalArgumentException("licenseReader is null.");
        else if(fallbackFactory == null) throw new IllegalArgumentException("fallbackFactory is null.");
        else if(licenseReader.isResolved()) return licenseReader.getService().getLimitations(limitationsType, fallbackFactory);
        else return fallbackFactory.create();
    }
}
