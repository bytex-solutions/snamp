package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributeTrackerService extends AbstractPrincipalBoundedService<Dashboard> {
    public AttributeTrackerService() {
        super(Dashboard.class);
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
