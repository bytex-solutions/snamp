package com.bytex.snamp.cluster;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import org.osgi.framework.BundleContext;

import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents credentials for database.
 */
final class DatabaseCredentials {
    private static final String SNAMP_DEFAULT_USER_NAME = "snamp";
    private static final String SNAMP_USER_NAME = "orientdb.user.name";
    private static final String SNAMP_USER_PASSWORD = "orientdb.user.password";
    private final char[] password;
    private final String userName;

    DatabaseCredentials(final String userName, final String password) {
        this.password = Objects.requireNonNull(password).toCharArray();
        this.userName = Objects.requireNonNull(userName);
    }

    DatabaseCredentials(final OServerUserConfiguration userConfiguration){
        this(userConfiguration.name, userConfiguration.password);
    }

    static DatabaseCredentials resolveSnampUser(final BundleContext context, final OServerConfiguration configuration) {
        for (final OServerUserConfiguration userConfiguration : configuration.users)
            if (Objects.equals(userConfiguration.name, SNAMP_DEFAULT_USER_NAME))
                return new DatabaseCredentials(userConfiguration);
        //try to resolve SNAMP DB user via framework properties
        if (context != null) {
            String userName = context.getProperty(SNAMP_USER_NAME);
            if (isNullOrEmpty(userName))
                userName = SNAMP_DEFAULT_USER_NAME;
            String password = context.getProperty(SNAMP_USER_PASSWORD);
            return isNullOrEmpty(password) ? new DatabaseCredentials(userName, password) : null;
        }
        return null;
    }

    void login(final ODatabase<?> database){
        database.open(userName, new String(password));
    }

    void createUser(final ODatabase<?> database) {
        database.getMetadata().getSecurity().createUser(
                userName,
                new String(password),
                database.getMetadata().getSecurity().getRole(ORole.ADMIN)
        );
    }
}
