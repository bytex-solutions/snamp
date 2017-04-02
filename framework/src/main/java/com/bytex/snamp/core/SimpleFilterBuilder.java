package com.bytex.snamp.core;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents abstract implementation of filter builder.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class SimpleFilterBuilder extends HashMap<String, String> implements FilterBuilder, Constants {
    private static final long serialVersionUID = -4019896459706940608L;

    public SimpleFilterBuilder() {

    }

    protected SimpleFilterBuilder(final Map<String, String> properties) {
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

    /**
     * Constructs OSGi filter.
     *
     * @return OSGi filter.
     */
    @Override
    public Filter get() {
        return get("");
    }

    /**
     * Constructs OSGi filter.
     *
     * @param filter Additional filter to be appended to this filter.
     * @return OSGi filter.
     */
    public Filter get(final String filter) {
        final String textFilter = toString(filter);
        return callUnchecked(() -> FrameworkUtil.createFilter(textFilter));
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    @Nonnull
    public SimpleFilterBuilder setServiceType(@Nonnull final Class<?> serviceType) {
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
