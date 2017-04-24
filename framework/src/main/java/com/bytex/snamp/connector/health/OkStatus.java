package com.bytex.snamp.connector.health;

import com.bytex.snamp.concurrent.LazyWeakReference;

import java.time.Instant;
import java.util.Locale;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OkStatus extends HealthStatus {
    private static final long serialVersionUID = 5391122005596632004L;

    public OkStatus(final Instant timeStamp) {
        super(0, timeStamp);
    }

    public OkStatus(){
        this(Instant.now());
    }

    @Override
    public boolean isCritical() {
        return false;
    }

    @Override
    public String toString(final Locale locale) {
        return "Everything is fine";
    }
}
