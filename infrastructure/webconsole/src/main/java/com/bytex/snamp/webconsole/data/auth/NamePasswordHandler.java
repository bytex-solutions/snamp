package com.bytex.snamp.webconsole.data.auth;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * The type Name password handler.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class NamePasswordHandler implements CallbackHandler {

    private String userName;
    private String password;

    /**
     * Instantiates a new Name password handler.
     *
     * @param user the user
     * @param pass the pass
     */
    public NamePasswordHandler(String user , String pass){
        this.userName = user;
        this.password = pass;
    }

    public void handle(Callback[] suppliedCallback) throws IOException,UnsupportedCallbackException {

        for (int i = 0; i < suppliedCallback.length; i++) {
            if (suppliedCallback[i] instanceof NameCallback) {
                if (userName != null) {
                    ((NameCallback)suppliedCallback[i]).setName(userName);
                }
            }
            else if (suppliedCallback[i] instanceof PasswordCallback) {
                if (password != null) {
                    ((PasswordCallback)suppliedCallback[i]).setPassword(password.toCharArray());
                }
            }
            else {
                throw new UnsupportedCallbackException(suppliedCallback[i]);
            }
        }

    }
}
