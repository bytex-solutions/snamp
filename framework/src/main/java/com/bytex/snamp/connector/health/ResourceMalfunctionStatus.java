package com.bytex.snamp.connector.health;

import java.time.Instant;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Something wrong with managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ResourceMalfunctionStatus extends MalfunctionStatus {
    private static final long serialVersionUID = -1766747580186741189L;

    protected ResourceMalfunctionStatus(final Instant timeStamp) {
        super(timeStamp);
    }
}
