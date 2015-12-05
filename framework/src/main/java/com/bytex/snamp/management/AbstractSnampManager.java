package com.bytex.snamp.management;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.core.SupportService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.osgi.framework.*;

import java.util.*;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * Represents partial implementation of {@link com.bytex.snamp.management.SnampManager} service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampManager extends AbstractFrameworkService implements SnampManager {
    private final class InternalSnampComponentDescriptor extends HashMap<String, String> implements SnampComponentDescriptor{
        private static final long serialVersionUID = 5684854305916946882L;

        private InternalSnampComponentDescriptor(final long bid){
            super(1);
            put(BUNDLE_ID_PROPERTY, Long.toString(bid));
        }

        private long getBundleID(){
            return Long.parseLong(get(BUNDLE_ID_PROPERTY));
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
         * @see com.bytex.snamp.management.Maintainable
         */
        @Override
        public <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Consumer<S, E> serviceInvoker) throws E {
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
        private static final long serialVersionUID = 5641114150847940779L;

        protected ResourceAdapterDescriptor(final String systemName){
            super(1);
            put(ADAPTER_SYSTEM_NAME_PROPERTY, systemName);
        }

        protected final String getSystemName(){
            return get(ADAPTER_SYSTEM_NAME_PROPERTY);
        }

        private BundleContext getItselfContext(){
            return getBundleContextOfObject(this);
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
         * @see com.bytex.snamp.management.Maintainable
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
        private static final long serialVersionUID = -5406342058157943559L;

        protected ResourceConnectorDescriptor(final String connectorName){
            super(1);
            put(CONNECTOR_SYSTEM_NAME_PROPERTY, connectorName);
        }

        protected final String getSystemName(){
            return get(CONNECTOR_SYSTEM_NAME_PROPERTY);
        }

        private BundleContext getItselfContext(){
            return getBundleContextOfObject(this);
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
         * @see com.bytex.snamp.management.Maintainable
         */
        @Override
        public final  <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Consumer<S, E> serviceInvoker) throws E {
            ServiceReference<S> ref = null;
            try {
                ref = ManagedResourceConnectorClient.getServiceReference(getItselfContext(), getSystemName(), null, serviceType);
                if (ref == null) return false;
                else serviceInvoker.accept(getItselfContext().getService(ref));
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
    public final Collection<? extends ResourceConnectorDescriptor> getInstalledResourceConnectors() {
        final Collection<String> systemNames = ManagedResourceActivator.getInstalledResourceConnectors(getBundleContextOfObject(this));
        final Collection<ResourceConnectorDescriptor> result = new LinkedList<>();
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
    public final Collection<? extends ResourceAdapterDescriptor> getInstalledResourceAdapters() {
        final Collection<String> systemNames = ResourceAdapterActivator.getInstalledResourceAdapters(getBundleContextOfObject(this));
        final Collection<ResourceAdapterDescriptor> result = new LinkedList<>();
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
    public final Collection<InternalSnampComponentDescriptor> getInstalledComponents() {
        final BundleContext context = getBundleContextOfObject(this);
        final Collection<InternalSnampComponentDescriptor> result = new LinkedList<>();
        for(final Bundle bnd: context.getBundles())
            if(isSnampComponent(bnd))
                result.add(new InternalSnampComponentDescriptor(bnd.getBundleId()));
        return result;
    }

    public final ResourceConnectorDescriptor getResourceConnector(final String connectorName) {
        return Iterables.find(getInstalledResourceConnectors(), new Predicate<SnampComponentDescriptor>() {
            @Override
            public boolean apply(final SnampComponentDescriptor connector) {
                return Objects.equals(connectorName, connector.get(SnampComponentDescriptor.CONNECTOR_SYSTEM_NAME_PROPERTY));
            }
        });
    }

    public final ResourceAdapterDescriptor getResourceAdapter(final String adapterName) {
        return Iterables.find(getInstalledResourceAdapters(), new Predicate<SnampComponentDescriptor>() {
            @Override
            public boolean apply(final SnampComponentDescriptor connector) {
                return Objects.equals(adapterName, connector.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY));
            }
        });
    }
}
