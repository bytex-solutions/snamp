package com.itworks.snamp.adapters.runtime;

import com.google.common.base.Joiner;
import com.itworks.snamp.adapters.FeatureAccessor;
import com.itworks.snamp.jmx.DescriptorUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents information about binding of the feature.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class FeatureBinding extends HashMap<String, Object> {
    private final String resourceName;
    private final String userDefinedName;

    private FeatureBinding(final String declaredResource,
                           final String featureName,
                           final Map<String, ?> bindingDetails){
        super(bindingDetails);
        this.resourceName = Objects.requireNonNull(declaredResource);
        this.userDefinedName = Objects.requireNonNull(featureName);
    }

    FeatureBinding(final String declaredResource,
                   final FeatureAccessor<?, ?> accessor) {
        this(declaredResource, accessor.get().getName(), DescriptorUtils.toMap(accessor.get().getDescriptor()));
    }

    FeatureBinding(final String declaredResource,
                   final String featureName){
        this(declaredResource, featureName, Collections.<String, Object>emptyMap());
    }

    private static String toString(final char... chars){
        return new String(chars);
    }

    public final String getBindingDetailsAsString(final char keyValueSeparator,
                                                  final char pairSeparator) {
        return Joiner.on(pairSeparator)
                .withKeyValueSeparator(toString(keyValueSeparator))
                .useForNull("undefined")
                .join(this);
    }

    /**
     * Converts binding details to plain string.
     * @return A string that represents binding details.
     */
    public final String getBindingDetailsAsString(){
        return getBindingDetailsAsString(';', '=');
    }

    /**
     * Gets name of the feature.
     * @return Name of the feature.
     */
    public final String getName(){
        return userDefinedName;
    }

    /**
     * Gets name of the resource which declares this feature.
     * @return Name of the resource.
     */
    public final String getDeclaredResource(){
        return resourceName;
    }
}
