package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.core.Communicator;

import java.util.function.Consumer;

/**
 * Represents
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractPrincipalBoundedService<M> extends AbstractWebConsoleService implements Consumer<Communicator.IncomingMessage> {

    /**
     * Performs this operation on the given argument.
     *
     * @param incomingMessage the input argument
     */
    @Override
    public void accept(final Communicator.IncomingMessage incomingMessage) {

    }
}
