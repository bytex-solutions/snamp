package com.bytex.snamp.web.serviceModel;

/**
 * Represents abstract computing service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ComputingService<I, O, USERDATA> extends AbstractPrincipalBoundedService<USERDATA> {

    protected ComputingService(final Class<USERDATA> userDataType) {
        super(userDataType);
    }

    /**
     * Performs idempotent computation.
     * @param input Input argument to process.
     * @return Computation result.
     */
    public abstract O compute(final I input);
}
