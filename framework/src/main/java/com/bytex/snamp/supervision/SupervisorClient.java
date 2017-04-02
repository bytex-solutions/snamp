package com.bytex.snamp.supervision;

import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.*;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.concurrent.SpinWait.spinUntilNull;

/**
 * Represents client for {@link Supervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SupervisorClient extends ServiceHolder<Supervisor> {
    /**
     * Initializes a new supervisor client.
     *
     * @param context    The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceRef The service reference to wrap. Cannot be {@literal null}.
     */
    public SupervisorClient(final BundleContext context, final ServiceReference<Supervisor> serviceRef) {
        super(context, serviceRef);
    }

    public static SupervisorClient tryCreate(final BundleContext context,
                                          final String groupName,
                                          final Duration instanceTimeout) throws TimeoutException, InterruptedException{
        final ServiceReference<Supervisor> ref = spinUntilNull(context, groupName, SupervisorClient::getSupervisorInstance, instanceTimeout);
        return new SupervisorClient(context, ref);
    }

    public static SupervisorClient tryCreate(final BundleContext context, final String groupName) {
        final ServiceReference<Supervisor> ref = getSupervisorInstance(context, groupName);
        return ref == null ? null : new SupervisorClient(context, ref);
    }

    /**
     * Constructs a new instance of the filter used to obtain service {@link Supervisor} from OSGi service registry.
     * @return A new instance of the filter builder.
     */
    public static SupervisorFilterBuilder filterBuilder(){
        final SupervisorFilterBuilder filter = new SupervisorFilterBuilder();
        filter.setServiceType(Supervisor.class);
        return filter;
    }

    private static ServiceReference<Supervisor> getSupervisorInstance(final BundleContext context,
                                                                final String groupName) {
        return filterBuilder().setGroupName(groupName).getServiceReference(context, Supervisor.class).orElse(null);
    }

    private static String getSupervisorBundleHeader(final BundleContext context,
                                                 final String supervisorType,
                                                 final String header,
                                                 final Locale loc){
        final List<Bundle> candidates = SupervisorActivator.getSupervisorBundles(context, supervisorType);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    /**
     * Gets bundle state of the specified supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String supervisorType){
        final List<Bundle> bnds = SupervisorActivator.getSupervisorBundles(context, supervisorType);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    /**
     * Gets localized display name of the supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @param loc The locale of the display name. May be {@literal null}.
     * @return The display name of the supervisor.
     */
    public static String getDisplayName(final BundleContext context, final String supervisorType, final Locale loc) {
        return getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_NAME, loc);
    }

    /**
     * Gets version of the specified supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @return The version of the supervisor.
     */
    public static Version getVersion(final BundleContext context, final String supervisorType){
        return new Version(getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_VERSION, null));
    }

    /**
     * Gets localized description of the supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the supervisor.
     */
    public static String getDescription(final BundleContext context, final String supervisorType, final Locale loc) {
        return getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_DESCRIPTION, loc);
    }
}
