package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.connector.supervision.NoRootCause;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OkStatusDetails extends AbstractStatusDetails {
    static final OkStatusDetails INSTANCE = new OkStatusDetails();

    private OkStatusDetails(){
        super("");
    }

    @Override
    public HealthStatus getStatus() {
        return HealthStatus.OK;
    }

    @Override
    public NoRootCause getRootCause() {
        return NoRootCause.INSTANCE;
    }
}
