package com.bytex.snamp.connector.health;

import com.bytex.snamp.concurrent.LazyWeakReference;

import java.util.Locale;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OkStatus extends HealthStatus {
    private static final LazyWeakReference<OkStatus> INSTANCE = new LazyWeakReference<>();
    private static final long serialVersionUID = 5391122005596632004L;

    private OkStatus() {
        super(0);
    }

    /**
     * Gets instance of {@link OkStatus}.
     *
     * @return Singleton instance.
     */
    public static OkStatus getInstance() {
        return INSTANCE.lazyGet(OkStatus::new);
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
