package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.health.HealthCheckStatus;
import com.bytex.snamp.moa.watching.NoRootCause;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OkStatusDetails extends AbstractStatusDetails<NoRootCause> {
    static final OkStatusDetails INSTANCE = new OkStatusDetails();

    private OkStatusDetails(){
        super("", NoRootCause.INSTANCE);
    }

    @Override
    public HealthCheckStatus getStatus() {
        return HealthCheckStatus.OK;
    }
}
