package com.bytex.snamp.connector.supervision;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import java.util.List;
import java.util.Locale;

/**
 * Represents client for {@link Supervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SupervisorClient {

    public static SupervisorFilterBuilder filterBuilder(){
        final SupervisorFilterBuilder filter = new SupervisorFilterBuilder();
        filter.setServiceType(Supervisor.class);
        return filter;
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
