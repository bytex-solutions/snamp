package com.bytex.snamp.web.serviceModel;

import java.security.Principal;
import java.util.EventListener;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface WebConsoleSession extends EventListener {
    /**
     * Gets user data cached by this session.
     * @param userDataType Type of the cached user data.
     * @param <USERDATA> Type of the cached user data.
     * @return Optional cached user data.
     */
    <USERDATA> Optional<USERDATA> getUserData(final Class<USERDATA> userDataType);

    /**
     * Sends message to the Web client asynchronously.
     * @param message A message to send. Cannot be {@literal null}.
     */
    void sendMessage(final WebMessage message);

    /**
     * Gets principal associated with this session.
     * @return Principal associated with this session.
     */
    Principal getPrincipal();
}
