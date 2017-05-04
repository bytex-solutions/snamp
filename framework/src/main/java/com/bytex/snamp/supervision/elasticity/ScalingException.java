package com.bytex.snamp.supervision.elasticity;

/**
 * Scaling process is failed.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public abstract class ScalingException extends Exception {
    protected ScalingException(final String message, final Exception cause){
        super(message, cause);
    }

    /**
     * Indicates that this exception is critical (or not) for elasticity management infrastructure.
     * @return {@literal true} to retry scaling later; {@literal false} to disable elasticity management.
     */
    public abstract boolean isRecoverable();
}
