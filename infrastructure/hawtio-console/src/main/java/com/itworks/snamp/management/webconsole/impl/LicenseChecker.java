package com.itworks.snamp.management.webconsole.impl;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.licensing.LicenseLogger;
import com.itworks.snamp.licensing.LicenseManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import java.util.Objects;

/**
 * Represents periodic license checker.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LicenseChecker extends Repeater {
    private final LicenseManager manager;

    LicenseChecker(final LicenseManager manager){
        super(TimeSpan.fromSeconds(5));
        this.manager = Objects.requireNonNull(manager);
    }

    /**
     * Provides some periodical action.
     */
    @Override
    protected void doAction() {
        final boolean isOK;
        try{
            isOK = manager.isOK();
        }
        catch (final Exception e){
            LicenseLogger.error("Unable to check license", e);
            return;
        }
        if(!isOK) {    //report about license violation
            final BundleContext context = Utils.getBundleContextByObject(this);
            final ServiceReferenceHolder<LogService> logServiceRef =
                    new ServiceReferenceHolder<>(context, LogService.class);
            try {
                logServiceRef.get().log(LogService.LOG_ERROR, "License conditions are violated");
            } finally {
                logServiceRef.release(context);
            }
        }
    }
}
