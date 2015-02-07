package com.itworks.snamp.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.Aggregator;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.core.SupportService;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.*;

import java.util.*;

/**
 * Represents partial implementation of {@link com.itworks.snamp.management.SnampManager} service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampManager extends AbstractAggregator implements SnampManager {
    private final class InternalSnampComponentDescriptor extends HashMap<String, String> implements SnampComponentDescriptor{

        public InternalSnampComponentDescriptor(final long bid){
            super(1);
            put(BUNDLE_ID_PROPERTY, Long.toString(bid));
        }

        private long getBundleID(){
            return Long.valueOf(get(BUNDLE_ID_PROPERTY));
        }

        private BundleContext getItselfContext(){
            return Utils.getBundleContextByObject(AbstractSnampManager.this);
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
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Consumer<S, E> serviceInvoker) throws E {
            final BundleContext context = getItselfContext();
            final Bundle bnd = context.getBundle(getBundleID());
            boolean result = false;
            final ServiceReference<?>[] refs = bnd.getRegisteredServices();
            for (final ServiceReference<?> candidate : refs != null ? refs : new ServiceReference<?>[0])
                if (Utils.isInstanceOf(candidate, serviceType))
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
        public String getDescription(final Locale locale) {
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

    /**
     * Represents superclass for resource adapter descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ResourceAdapterDescriptor extends HashMap<String, String> implements SnampComponentDescriptor{

        protected ResourceAdapterDescriptor(final String systemName){
            super(1);
            put(ADAPTER_SYSTEM_NAME_PROPERTY, systemName);
        }

        protected final String getSystemName(){
            return get(ADAPTER_SYSTEM_NAME_PROPERTY);
        }

        /**
         * Represents the bundle context of the derived class.
         * <p>
         *     Use {@link com.itworks.snamp.internal.Utils#getBundleContextByObject(Object)} with
         *     {@literal this} parameter to implement this method.
         * </p>
         * @return The bundle context of the derived class.
         */
        protected abstract BundleContext getItselfContext();

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
            return ResourceAdapterClient.getState(getItselfContext(), getSystemName());
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ResourceAdapterClient.getDisplayName(getItselfContext(), getSystemName(), loc);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ResourceAdapterClient.getVersion(getItselfContext(), getSystemName());
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public final  <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Consumer<S, E> serviceInvoker) throws E {
            ServiceReference<S> ref = null;
            try {
                ref = ResourceAdapterClient.getServiceReference(getItselfContext(), getSystemName(), null, serviceType);
                if (ref == null) return false;
                serviceInvoker.accept(getItselfContext().getService(ref));
            } catch (final InvalidSyntaxException ignored) {
                return false;
            } finally {
                if (ref != null) getItselfContext().ungetService(ref);
            }
            return true;
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String getDescription(final Locale locale) {
            return ResourceAdapterClient.getDescription(getItselfContext(), getSystemName(), locale);
        }

        /**
         * Returns system name of the adapter.
         * @return The system name of the adapter.
         */
        @Override
        public String toString() {
            return getSystemName();
        }
    }

    /**
     * Represents superclass for managed resource connector descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ResourceConnectorDescriptor extends HashMap<String, String> implements SnampComponentDescriptor {

        protected ResourceConnectorDescriptor(final String connectorName){
            super(1);
            put(CONNECTOR_SYSTEM_NAME_PROPERTY, connectorName);
        }

        protected final String getSystemName(){
            return get(CONNECTOR_SYSTEM_NAME_PROPERTY);
        }

        /**
         * Represents the bundle context of the derived class.
         * <p>
         *     Use {@link com.itworks.snamp.internal.Utils#getBundleContextByObject(Object)} with
         *     {@literal this} parameter to implement this method.
         * </p>
         * @return The bundle context of the derived class.
         */
        protected abstract BundleContext getItselfContext();

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
            return ManagedResourceConnectorClient.getState(getItselfContext(), getSystemName());
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ManagedResourceConnectorClient.getDisplayName(getItselfContext(), getSystemName(), loc);
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String getDescription(final Locale locale) {
            return ManagedResourceConnectorClient.getDescription(getItselfContext(), getSystemName(), locale);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ManagedResourceConnectorClient.getVersion(getItselfContext(), getSystemName());
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public final  <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Consumer<S, E> serviceInvoker) throws E {
            ServiceReference<S> ref = null;
            try {
                ref = ManagedResourceConnectorClient.getServiceReference(getItselfContext(), getSystemName(), null, serviceType);
                if (ref == null) return false;
                serviceInvoker.accept(getItselfContext().getService(ref));
            } catch (final InvalidSyntaxException ignored) {
                return false;
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
            return getSystemName();
        }
    }

    /**
     * Creates a new instance of the connector descriptor.
     * @param systemName The name of the connector.
     * @return A new instance of the connector descriptor.
     */
    protected abstract ResourceConnectorDescriptor createResourceConnectorDescriptor(final String systemName);

    /**
     * Returns a read-only collection of installed resource connectors.
     *
     * @return A read-only collection of installed resource connectors.
     */
    @Override
    public final Collection<SnampComponentDescriptor> getInstalledResourceConnectors() {
        final Collection<String> systemNames = ManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this));
        final Collection<SnampComponentDescriptor> result = new ArrayList<>(systemNames.size());
        for(final String systemName: systemNames)
            result.add(createResourceConnectorDescriptor(systemName));
        return result;
    }

    /**
     * Creates a new instance of the resource adapter descriptor.
     * @param systemName The system name of the adapter.
     * @return A new instance of the resource adapter descriptor.
     */
    protected abstract ResourceAdapterDescriptor createResourceAdapterDescriptor(final String systemName);

    /**
     * Returns a read-only collection of installed resource adapters.
     *
     * @return A read-only collection of installed resource adapters.
     */
    @Override
    public final Collection<SnampComponentDescriptor> getInstalledResourceAdapters() {
        final Collection<String> systemNames = ResourceAdapterActivator.getInstalledResourceAdapters(Utils.getBundleContextByObject(this));
        final Collection<SnampComponentDescriptor> result = new ArrayList<>(systemNames.size());
        for(final String systemName: systemNames)
            result.add(createResourceAdapterDescriptor(systemName));
        return result;
    }

    /**
     * Determines whether the specified bundle is a part of SNAMP.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle is a part of SNAMP; otherwise, {@literal false}.
     */
    public static boolean isSnampComponent(final Bundle bnd){
        if(ResourceAdapterActivator.isResourceAdapterBundle(bnd) ||
                ManagedResourceActivator.isResourceConnectorBundle(bnd)) return false;
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
    public final Collection<SnampComponentDescriptor> getInstalledComponents() {
        final BundleContext context = Utils.getBundleContextByObject(this);
        final Collection<SnampComponentDescriptor> result = new ArrayList<>(10);
        for(final Bundle bnd: context.getBundles())
            if(isSnampComponent(bnd))
                result.add(new InternalSnampComponentDescriptor(bnd.getBundleId()));
        return result;
    }

    public final SnampComponentDescriptor getResourceConnector(final String connectorName) {
        return Iterables.find(getInstalledResourceConnectors(), new Predicate<SnampComponentDescriptor>() {
            @Override
            public boolean apply(final SnampComponentDescriptor connector) {
                return Objects.equals(connectorName, connector.get(SnampComponentDescriptor.CONNECTOR_SYSTEM_NAME_PROPERTY));
            }
        });
    }

    public final SnampComponentDescriptor getResourcAdapter(final String adapterName) {
        return Iterables.find(getInstalledResourceAdapters(), new Predicate<SnampComponentDescriptor>() {
            @Override
            public boolean apply(final SnampComponentDescriptor connector) {
                return Objects.equals(adapterName, connector.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY));
            }
        });
    }
}
