package com.bytex.snamp.web.serviceModel;

import java.security.Principal;
import java.util.EventListener;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface WebEventListener extends EventListener, Consumer<WebEvent> {
    @Override
    void accept(final WebEvent event);

    /**
     * Gets principal associated with this listener.
     * @return Principal associated with this listener; or {@literal null} if this listener belongs to all users.
     */
    Principal getPrincipal();
}
