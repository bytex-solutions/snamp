package com.bytex.snamp.core;

import org.osgi.framework.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents service selector.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class DefaultServiceSelector extends HashMap<String, String> implements ServiceSelector, Constants {
    private static final long serialVersionUID = -4019896459706940608L;

    public DefaultServiceSelector() {

    }

    protected DefaultServiceSelector(final Map<String, String> properties) {
        super(properties);
    }

    /**
     * Resets this builder.
     */
    @Override
    public void reset() {
        clear();
    }

    public String toString(final String filter) {
        if (isEmpty() && isNullOrEmpty(filter))
            return "";
        final StringBuilder result = new StringBuilder("(&").append(filter);
        forEach((key, value) -> result.append('(').append(key).append('=').append(value).append(')'));
        return result.append(')').toString();
    }

    public final DefaultServiceSelector property(final String name, final String value){
        put(name, value);
        return this;
    }

    /**
     * Constructs OSGi filter.
     *
     * @return OSGi filter.
     */
    @Override
    public Filter get() {
        return callUnchecked(() -> get(""));
    }

    /**
     * Constructs OSGi filter.
     *
     * @param filter Additional filter to be appended to this filter.
     * @return OSGi filter.
     * @throws InvalidSyntaxException Unable to compile additional filter.
     */
    public Filter get(final String filter) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter(toString(filter));
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    @Nonnull
    public final DefaultServiceSelector setServiceType(@Nonnull final Class<?> serviceType) {
        put(OBJECTCLASS, serviceType.getName());
        return this;
    }

    protected static Optional<String> getReferencePropertyAsString(final ServiceReference<?> connectorRef,
                                                                 final String propertyName) {
        return Optional.ofNullable(connectorRef)
                .map(ref -> ref.getProperty(propertyName))
                .map(Objects::toString);
    }
}
