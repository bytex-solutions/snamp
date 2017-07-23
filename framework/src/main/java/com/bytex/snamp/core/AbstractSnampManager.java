package com.bytex.snamp.core;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceSelector;
import com.bytex.snamp.gateway.Gateway;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.gateway.GatewaySelector;
import com.bytex.snamp.supervision.Supervisor;
import com.bytex.snamp.supervision.SupervisorActivator;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.SupervisorSelector;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * Represents partial implementation of {@link SnampManager} service.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractSnampManager extends AbstractAggregator implements SnampManager {
    private final class InternalSnampComponentDescriptor extends HashMap<String, String> implements SnampComponentDescriptor{
        private static final long serialVersionUID = 5684854305916946882L;

        private InternalSnampComponentDescriptor(final long bid){
            super(1);
            put(BUNDLE_ID_PROPERTY, Long.toString(bid));
        }

        private long getBundleID(){
            return getValueAsLong(this, BUNDLE_ID_PROPERTY, Long::parseLong).orElse(0L);
        }

        private BundleContext getItselfContext() {
            return getBundleContextOfObject(AbstractSnampManager.this);
        }

        /**
         * Gets state of the component.
         *
         * @return The state of the component.
         * @see org.osgi.framework.Bundle#ACTIVE
         * @see org.osgi.framework.Bundle#INSTALLED
         * @see org.osgi.framework.Bundle#UNINSTALLED
         * @see org.osgi.framework.Bundle#RESOLVED
         * @see org.osgi.framework.Bundle#STARTING
         * @see org.osgi.framework.Bundle#STOPPING
         */
        @Override
        public int getState() {
            return getItselfContext().getBundle(getBundleID()).getState();
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return getItselfContext().
                    getBundle(getBundleID()).
                    getHeaders(loc != null ? loc.toString() : null).
                    get(Constants.BUNDLE_NAME);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return getItselfContext().
                    getBundle(getBundleID()).
                    getVersion();
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @return {@literal true}, if the specified service is invoked successfully.
         */
        @Override
        public <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Acceptor<S, E> serviceInvoker) throws E {
            final BundleContext context = getItselfContext();
            final Bundle bnd = context.getBundle(getBundleID());
            boolean result = false;
            final ServiceReference<?>[] refs = bnd.getRegisteredServices();
            for (final ServiceReference<?> candidate : refs != null ? refs : emptyArray(ServiceReference[].class))
                if (isInstanceOf(candidate, serviceType))
                    try {
                        serviceInvoker.accept(serviceType.cast(context.getService(candidate)));
                    } finally {
                        context.ungetService(candidate);
                        result = true;
                    }
            return result;
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String toString(final Locale locale) {
            return getItselfContext().
                    getBundle(getBundleID()).
                    getHeaders(locale != null ? locale.toString() : null).
                    get(Constants.BUNDLE_DESCRIPTION);
        }

        @Override
        public String toString() {
            final Bundle bnd = getItselfContext().getBundle(getBundleID());
            return bnd != null ?
                    String.format("Bundle id: %s. Symbolic name: %s", getBundleID(), bnd.getSymbolicName()):
                    String.format("Bundle id: %s", getBundleID());
        }
    }

    //do not change to private class due to bug with lambda expressions in Java
    public static abstract class TypedComponentDescriptor extends HashMap<String, String> implements SnampComponentDescriptor{
        private static final long serialVersionUID = 4377144410734844053L;
        private final String componentTypeHolder;

        private TypedComponentDescriptor(final String componentTypeHolder, @Nonnull final String systemName) {
            super(2);
            put(this.componentTypeHolder = componentTypeHolder, systemName);
        }

        public final String getType(){
            return getOrDefault(componentTypeHolder, "");
        }

        final BundleContext getItselfContext(){
            return getBundleContextOfObject(this);
        }

        abstract ServiceSelector filterBuilder();

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         */
        @Override
        public final  <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Acceptor<S, E> serviceInvoker) throws E {
            final ServiceSelector filter = filterBuilder().setServiceType(serviceType);
            ServiceReference<S> ref = null;
            try {
                ref = filter.getServiceReference(getItselfContext(), serviceType).orElse(null);
                if (ref == null) return false;
                serviceInvoker.accept(getItselfContext().getService(ref));
            } finally {
                if (ref != null) getItselfContext().ungetService(ref);
            }
            return true;
        }

        /**
         * Returns system name of the connector.
         * @return The system name of the connector.
         */
        @Override
        public String toString() {
            return getType();
        }
    }

    public static abstract class SupervisorDescriptor extends TypedComponentDescriptor{
        private static final long serialVersionUID = -4684730181011108891L;

        protected SupervisorDescriptor(final String systemName){
            super(SUPERVISOR_TYPE_PROPERTY, systemName);
        }

        @Override
        SupervisorSelector filterBuilder() {
            return SupervisorClient.selector();
        }

        /**
         * Gets state of the component.
         *
         * @return The state of the component.
         * @see org.osgi.framework.Bundle#ACTIVE
         * @see org.osgi.framework.Bundle#INSTALLED
         * @see org.osgi.framework.Bundle#UNINSTALLED
         * @see org.osgi.framework.Bundle#RESOLVED
         * @see org.osgi.framework.Bundle#STARTING
         * @see org.osgi.framework.Bundle#STOPPING
         */
        @Override
        public int getState() {
            return SupervisorClient.getState(getItselfContext(), getType());
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return SupervisorClient.getDisplayName(getItselfContext(), getType(), loc);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return SupervisorClient.getVersion(getItselfContext(), getType());
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String toString(final Locale locale) {
            return SupervisorClient.getDescription(getItselfContext(), getType(), locale);
        }
    }

    /**
     * Represents superclass for gateway descriptor.
     * Now public because in case that is protected we received this is defined in an inaccessible class or interface
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static abstract class GatewayDescriptor extends TypedComponentDescriptor{
        private static final long serialVersionUID = 5641114150847940779L;

        protected GatewayDescriptor(final String systemName){
            super(GATEWAY_TYPE_PROPERTY, systemName);
        }

        @Override
        GatewaySelector filterBuilder() {
            return GatewayClient.selector().setGatewayType(getType());
        }

        /**
         * Gets state of the component.
         *
         * @return The state of the component.
         * @see org.osgi.framework.Bundle#ACTIVE
         * @see org.osgi.framework.Bundle#INSTALLED
         * @see org.osgi.framework.Bundle#UNINSTALLED
         * @see org.osgi.framework.Bundle#RESOLVED
         * @see org.osgi.framework.Bundle#STARTING
         * @see org.osgi.framework.Bundle#STOPPING
         */
        @Override
        public int getState() {
            return GatewayClient.getState(getItselfContext(), getType());
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return GatewayClient.getDisplayName(getItselfContext(), getType(), loc);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return GatewayClient.getVersion(getItselfContext(), getType());
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String toString(final Locale locale) {
            return GatewayClient.getDescription(getItselfContext(), getType(), locale);
        }
    }

    /**
     * Represents superclass for managed resource connector descriptor.
     * Now public because in case that is protected we received this is defined in an inaccessible class or interface
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static abstract class ResourceConnectorDescriptor extends TypedComponentDescriptor {
        private static final long serialVersionUID = -5406342058157943559L;

        protected ResourceConnectorDescriptor(final String connectorName){
            super(CONNECTOR_TYPE_PROPERTY, connectorName);
        }

        @Override
        ManagedResourceSelector filterBuilder() {
            return ManagedResourceConnectorClient.selector().setConnectorType(getType());
        }

        /**
         * Gets state of the component.
         *
         * @return The state of the component.
         * @see org.osgi.framework.Bundle#ACTIVE
         * @see org.osgi.framework.Bundle#INSTALLED
         * @see org.osgi.framework.Bundle#UNINSTALLED
         * @see org.osgi.framework.Bundle#RESOLVED
         * @see org.osgi.framework.Bundle#STARTING
         * @see org.osgi.framework.Bundle#STOPPING
         */
        @Override
        public int getState() {
            return ManagedResourceConnectorClient.getState(getItselfContext(), getType());
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ManagedResourceConnectorClient.getDisplayName(getItselfContext(), getType(), loc);
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String toString(final Locale locale) {
            return ManagedResourceConnectorClient.getDescription(getItselfContext(), getType(), locale);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ManagedResourceConnectorClient.getVersion(getItselfContext(), getType());
        }
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * Creates a new instance of the connector descriptor.
     * @param systemName The name of the connector.
     * @return A new instance of the connector descriptor.
     */
    @Nonnull
    protected abstract ResourceConnectorDescriptor createResourceConnectorDescriptor(final String systemName);

    /**
     * Returns a read-only collection of installed resource connector.
     *
     * @return A read-only collection of installed resource connector.
     */
    @Override
    public final Collection<? extends ResourceConnectorDescriptor> getInstalledResourceConnectors() {
        final Collection<String> systemNames = ManagedResourceActivator.getInstalledResourceConnectors(getBundleContext());
        return systemNames.stream().map(this::createResourceConnectorDescriptor).collect(Collectors.toList());
    }

    /**
     * Creates a new instance of the gateway descriptor.
     * @param gatewayType Type of gateway.
     * @return A new instance of the gateway descriptor.
     */
    @Nonnull
    protected abstract GatewayDescriptor createGatewayDescriptor(final String gatewayType);

    /**
     * Returns a read-only collection of installed gateways.
     *
     * @return A read-only collection of installed gateways.
     */
    @Override
    public final Collection<? extends GatewayDescriptor> getInstalledGateways() {
        final Collection<String> systemNames = GatewayActivator.getInstalledGateways(getBundleContext());
        return systemNames.stream().map(this::createGatewayDescriptor).collect(Collectors.toList());
    }

    /**
     * Creates a new instance of the supervisor descriptor.
     * @param supervisorType Type of supervisor.
     * @return A new instance of the supervisor descriptor.
     */
    @Nonnull
    protected abstract SupervisorDescriptor createSupervisorDescriptor(final String supervisorType);

    /**
     * Returns a read-only collection of installed supervisors.
     *
     * @return A read-only collection of installed supervisors.
     * @since 2.0
     */
    @Override
    public final Collection<? extends SupervisorDescriptor> getInstalledSupervisors() {
        final Collection<String> systemNames = SupervisorActivator.getInstalledSupervisors(getBundleContext());
        return systemNames.stream().map(this::createSupervisorDescriptor).collect(Collectors.toList());
    }

    /**
     * Determines whether the specified bundle is a part of SNAMP.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle is a part of SNAMP; otherwise, {@literal false}.
     */
    private static boolean isSnampComponent(final Bundle bnd){
        if(Gateway.isGatewayBundle(bnd) || Supervisor.isSupervisorBundle(bnd) ||
                ManagedResourceConnector.isResourceConnectorBundle(bnd)) return false;
        final String importPackages = bnd.getHeaders().get(Constants.IMPORT_PACKAGE);
        if(importPackages == null) return false;
        final String snampPackageNameRoot = Aggregator.class.getPackage().getName();
        return importPackages.contains(snampPackageNameRoot);
    }

    /**
     * Returns a read-only collection of installed additional SNAMP components.
     *
     * @return A read-only collection of installed additional SNAMP components.
     */
    @Override
    public final Collection<? extends SnampComponentDescriptor> getInstalledComponents() {
        final BundleContext context = getBundleContext();
        return Arrays.stream(context.getBundles())
                .filter(AbstractSnampManager::isSnampComponent)
                .map(bnd -> new InternalSnampComponentDescriptor(bnd.getBundleId()))
                .collect(Collectors.toList());
    }
}
