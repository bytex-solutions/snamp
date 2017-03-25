package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.SimpleFilterBuilder;
import org.osgi.framework.ServiceReference;

import static com.bytex.snamp.connector.ManagedResourceConnector.CATEGORY_PROPERTY;
import static com.bytex.snamp.connector.ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder of OSGi filter used to query instances of {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ManagedResourceFilterBuilder extends SimpleFilterBuilder {
    private static final String CATEGORY = "resourceConnector";
    private static final String NAME_PROPERTY = "resourceName";
    private static final String CONNECTION_STRING_PROPERTY = "connectionString";
    private static final long serialVersionUID = 1348996073725440159L;

    ManagedResourceFilterBuilder() {
        put(CATEGORY_PROPERTY, CATEGORY);
    }

    ManagedResourceFilterBuilder(final ManagedResourceConfiguration configuration) {
        super(configuration);
        setConnectorType(configuration.getType())
                .setConnectionString(configuration.getConnectionString())
                .put(CATEGORY_PROPERTY, CATEGORY);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        clear();
    }

    public ManagedResourceFilterBuilder setResourceName(final String value) {
        if (isNullOrEmpty(value))
            remove(NAME_PROPERTY);
        else
            put(NAME_PROPERTY, value);
        return this;
    }

    public ManagedResourceFilterBuilder setConnectorType(final String value) {
        if (isNullOrEmpty(value))
            remove(TYPE_CAPABILITY_ATTRIBUTE);
        else
            put(TYPE_CAPABILITY_ATTRIBUTE, value);
        return this;
    }

    public ManagedResourceFilterBuilder setConnectionString(final String value) {
        if (isNullOrEmpty(value))
            remove(CONNECTION_STRING_PROPERTY);
        else
            put(CONNECTION_STRING_PROPERTY, value);
        return this;
    }

    public ManagedResourceFilterBuilder setGroupName(final String value) {
        if (isNullOrEmpty(value))
            remove(ManagedResourceConfiguration.GROUP_NAME_PROPERTY);
        else
            put(ManagedResourceConfiguration.GROUP_NAME_PROPERTY, value);
        return this;
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return getReferencePropertyAsString(connectorRef, NAME_PROPERTY).orElse("");
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return getReferencePropertyAsString(connectorRef, CONNECTION_STRING_PROPERTY).orElse("");
    }
}
