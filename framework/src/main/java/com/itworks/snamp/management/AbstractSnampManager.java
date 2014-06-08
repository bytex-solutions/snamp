package com.itworks.snamp.management;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.Aggregator;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.internal.Utils;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.FunctorException;
import org.osgi.framework.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Represents partial implementation of {@link com.itworks.snamp.management.SnampManager} service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampManager extends AbstractAggregator implements SnampManager {
    private final class InternalSnampComponentDescriptor implements SnampComponentDescriptor{
        private final long bundleID;

        public InternalSnampComponentDescriptor(final long bid){
            this.bundleID = bid;
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
            return getItselfContext().getBundle(bundleID).getState();
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
                    getBundle(bundleID).
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
                    getBundle(bundleID).
                    getVersion();
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @throws org.apache.commons.collections4.FunctorException An exception occurred during processing.
         * @see org.apache.commons.collections4.FunctorException#getCause()
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public <S extends ManagementService> void invokeManagementService(final Class<S> serviceType, final Closure<S> serviceInvoker) throws FunctorException {
            final BundleContext context = getItselfContext();
            final Bundle bnd = context.getBundle(bundleID);
            for(final ServiceReference<?> candidate: bnd.getRegisteredServices())
                if(Utils.isInstanceOf(candidate, serviceType))
                    try{
                        serviceInvoker.execute(serviceType.cast(context.getService(candidate)));
                    }
                    finally {
                        context.ungetService(candidate);
                    }
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
                    getBundle(bundleID).
                    getHeaders(locale != null ? locale.toString() : null).
                    get(Constants.BUNDLE_DESCRIPTION);
        }

        @Override
        public String toString() {
            final Bundle bnd = getItselfContext().getBundle(bundleID);
            return bnd != null ?
                    String.format("Bundle id: %s. Symbolic name: %s", bundleID, bnd.getSymbolicName()):
                    String.format("Bundle id: %s", bundleID);
        }
    }

    /**
     * Represents superclass for resource adapter descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ResourceAdapterDescriptor implements SnampComponentDescriptor{
        /**
         * Represents system name of the adapter.
         */
        protected final String systemName;

        protected ResourceAdapterDescriptor(final String systemName){
            this.systemName = systemName;
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
            return ResourceAdapterClient.getState(getItselfContext(), systemName);
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ResourceAdapterClient.getDisplayName(getItselfContext(), systemName, loc);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ResourceAdapterClient.getVersion(getItselfContext(), systemName);
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @throws org.apache.commons.collections4.FunctorException An exception occurred during processing.
         * @see org.apache.commons.collections4.FunctorException#getCause()
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public final  <S extends ManagementService> void invokeManagementService(final Class<S> serviceType, final Closure<S> serviceInvoker) throws FunctorException {
            ServiceReference<S> ref = null;
            try {
                ref = ResourceAdapterClient.getServiceReference(getItselfContext(), systemName, null, serviceType);
                serviceInvoker.execute(getItselfContext().getService(ref));
            }
            catch (final InvalidSyntaxException e) {
                throw new FunctorException(e);
            }
            finally {
                if(ref != null) getItselfContext().ungetService(ref);
            }
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
            return ResourceAdapterClient.getDescription(getItselfContext(), systemName, locale);
        }

        /**
         * Returns system name of the adapter.
         * @return The system name of the adapter.
         */
        @Override
        public String toString() {
            return systemName;
        }
    }

    /**
     * Represents superclass for managed resource connector descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ResourceConnectorDescriptor implements SnampComponentDescriptor {
        /**
         * Represents name of the managed resource connector.
         */
        protected final String systemName;

        protected ResourceConnectorDescriptor(final String connectorName){
            this.systemName = connectorName;
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
            return ManagedResourceConnectorClient.getState(getItselfContext(), systemName);
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ManagedResourceConnectorClient.getDisplayName(getItselfContext(), systemName, loc);
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
            return ManagedResourceConnectorClient.getDescription(getItselfContext(), systemName, locale);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ManagedResourceConnectorClient.getVersion(getItselfContext(), systemName);
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @throws org.apache.commons.collections4.FunctorException An exception occurred during processing.
         * @see org.apache.commons.collections4.FunctorException#getCause()
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public final  <S extends ManagementService> void invokeManagementService(final Class<S> serviceType, final Closure<S> serviceInvoker) throws FunctorException {
            ServiceReference<S> ref = null;
            try {
                ref = ManagedResourceConnectorClient.getServiceReference(getItselfContext(), systemName, null, serviceType);
                serviceInvoker.execute(getItselfContext().getService(ref));
            }
            catch (final InvalidSyntaxException e) {
                throw new FunctorException(e);
            }
            finally {
                if(ref != null) getItselfContext().ungetService(ref);
            }
        }

        /**
         * Returns system name of the connector.
         * @return The system name of the connector.
         */
        @Override
        public String toString() {
            return systemName;
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
        final Collection<String> systemNames = AbstractManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this));
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
        final Collection<String> systemNames = AbstractResourceAdapterActivator.getInstalledResourceAdapters(Utils.getBundleContextByObject(this));
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
        if(AbstractResourceAdapterActivator.isResourceAdapterBundle(bnd) ||
                AbstractManagedResourceActivator.isResourceConnectorBundle(bnd)) return false;
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
}
