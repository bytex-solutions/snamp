package com.bytex.snamp.moa;

import com.bytex.snamp.concurrent.Timeout;

import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Average extends Timeout {
    private static final long serialVersionUID = -7033204722759946995L;

    public Average(final Duration ttl) {
        super(ttl);
    }


}
