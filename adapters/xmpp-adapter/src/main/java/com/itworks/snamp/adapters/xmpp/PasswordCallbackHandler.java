package com.itworks.snamp.adapters.xmpp;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class PasswordCallbackHandler implements CallbackHandler {
    private final char[] password;

    PasswordCallbackHandler(final String password){
        this.password = password.toCharArray();
    }

    private void setPassword(final PasswordCallback callback){
        callback.setPassword(password);
    }

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(final Callback callback: callbacks)
            if(callback instanceof PasswordCallback)
                setPassword((PasswordCallback)callback);
    }

    protected void finalize() throws Throwable {
        Arrays.fill(password, '\0');
        super.finalize();
    }
}
