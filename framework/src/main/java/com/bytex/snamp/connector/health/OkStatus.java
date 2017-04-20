package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import java.util.Locale;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Indicates that everything is OK.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class OkStatus extends HealthStatus {
    public static final int CODE = 0;
    private static final long serialVersionUID = 5391122005596632004L;

    private OkStatus(final String resourceName) {
        super(resourceName, CODE);
    }

    /**
     * Represents OK status associated with the specified resource name.
     * @param resourceName Resource name.
     * @return A new instance of OK status.
     */
    public static OkStatus of(final String resourceName) {
        return new OkStatus(nullToEmpty(resourceName));
    }

    /**
     * Represents OK status
     * @return
     */
    public static OkStatus of(){
        return of(null);
    }

    @Override
    public final boolean isCritical() {
        return false;
    }

    @Override
    public final int compareTo(@Nonnull final HealthStatus other) {
        return other instanceof OkStatus ? 0 : -1;
    }

    @Override
    public String toString(final Locale locale) {
        return "Everything is fine";
    }
}
