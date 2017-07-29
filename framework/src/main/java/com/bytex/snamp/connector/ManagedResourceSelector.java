package com.bytex.snamp.connector;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.DefaultServiceSelector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bytex.snamp.connector.ManagedResourceConnector.CATEGORY_PROPERTY;
import static com.bytex.snamp.connector.ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder of OSGi filter used to query instances of {@link ManagedResourceConnector}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ManagedResourceSelector extends DefaultServiceSelector {
    private static final String GROUP_NAME_PROPERTY = "group";
    private static final String CATEGORY = "resourceConnector";
    private static final String NAME_PROPERTY = "resourceName";
    private static final String CONNECTION_STRING_PROPERTY = "connectionString";
    private static final long serialVersionUID = 1348996073725440159L;

    ManagedResourceSelector() {
        put(CATEGORY_PROPERTY, CATEGORY);
    }

    ManagedResourceSelector(final ManagedResourceConfiguration configuration) {
        super(configuration);
        setConnectorType(configuration.getType())
                .setConnectionString(configuration.getConnectionString())
                .setGroupName(configuration.getGroupName())
                .put(CATEGORY_PROPERTY, CATEGORY);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        clear();
    }

    public ManagedResourceSelector setResourceName(final String value) {
        if (isNullOrEmpty(value))
            remove(NAME_PROPERTY);
        else
            put(NAME_PROPERTY, value);
        return this;
    }

    public ManagedResourceSelector setConnectorType(final String value) {
        if (isNullOrEmpty(value))
            remove(TYPE_CAPABILITY_ATTRIBUTE);
        else
            put(TYPE_CAPABILITY_ATTRIBUTE, value);
        return this;
    }

    public ManagedResourceSelector setConnectionString(final String value) {
        if (isNullOrEmpty(value))
            remove(CONNECTION_STRING_PROPERTY);
        else
            put(CONNECTION_STRING_PROPERTY, value);
        return this;
    }

    public ManagedResourceSelector setGroupName(final String value) {
        if (isNullOrEmpty(value))
            remove(GROUP_NAME_PROPERTY);
        else
            put(GROUP_NAME_PROPERTY, value);
        return this;
    }

    private ServiceReference<ManagedResourceConnector>[] getServiceReferences(final BundleContext context){
        return getServiceReferences(context, ManagedResourceConnector.class);
    }

    public Set<String> getResources(final BundleContext context) {
        return Arrays.stream(getServiceReferences(context))
                .map(ManagedResourceSelector::getManagedResourceName)
                .filter(name -> !isNullOrEmpty(name))
                .collect(Collectors.toSet());
    }

    public Set<String> getGroups(final BundleContext context) {
        return Arrays.stream(getServiceReferences(context))
                .map(ManagedResourceSelector::getGroupName)
                .filter(groupName -> !isNullOrEmpty(groupName))
                .collect(Collectors.toSet());
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return getReferencePropertyAsString(connectorRef, NAME_PROPERTY).orElse("");
    }

    static String getManagedResourceName(final Map<String, ?> identity) {
        return MapUtils.getValue(identity, NAME_PROPERTY, Objects::toString).orElse("");
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return getReferencePropertyAsString(connectorRef, CONNECTION_STRING_PROPERTY).orElse("");
    }

    static String getGroupName(final ServiceReference<ManagedResourceConnector> connectorRef){
        return getReferencePropertyAsString(connectorRef, GROUP_NAME_PROPERTY).orElse("");
    }
}
