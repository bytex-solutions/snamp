package com.bytex.snamp.webconsole;

import javax.security.auth.callback.*;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The type Name password handler.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
final class NamePasswordHandler implements CallbackHandler {
    private final String userName;
    private final String password;

    /**
     * Instantiates a new Name password handler.
     *
     * @param user the user
     * @param pass the pass
     */
    NamePasswordHandler(final String user, final String pass){
        this.userName = user;
        this.password = pass;
    }

    @Override
    public void handle(final Callback[] suppliedCallback) throws IOException,UnsupportedCallbackException {
        for (final Callback callback : suppliedCallback) {
            if (callback instanceof NameCallback && !isNullOrEmpty(userName)) {
                ((NameCallback) callback).setName(userName);
            } else if (callback instanceof PasswordCallback && !isNullOrEmpty(password)) {
                ((PasswordCallback) callback).setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
