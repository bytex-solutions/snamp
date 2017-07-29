package com.bytex.snamp.security.web.impl;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * Represents JAAS callback handler that provides information about user name and password.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class NamePasswordHandler implements CallbackHandler {
    private final String userName;
    private final char[] password;

    /**
     * Instantiates a new Name password handler.
     *
     * @param user the user
     * @param pass the pass
     */
    NamePasswordHandler(final String user, final String pass){
        this.userName = user;
        this.password = pass.toCharArray();
    }

    @Override
    public void handle(final Callback[] suppliedCallback) throws IOException,UnsupportedCallbackException {
        for (final Callback callback : suppliedCallback) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(userName);
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(password);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
