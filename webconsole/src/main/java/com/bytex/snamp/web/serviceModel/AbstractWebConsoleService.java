package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.internal.InheritanceNavigator;

import javax.ws.rs.Path;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractWebConsoleService implements WebConsoleService {
    private final WeakEventListenerList<WebEventListener, WebEvent> listeners = WeakEventListenerList.create(WebEventListener::accept);

    @Override
    public final String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public final void addWebEventListener(final WebEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeWebEventListener(final WebEventListener listener) {
        listeners.remove(listener);
    }

    protected final void fireWebEvent(final WebEvent event){
        listeners.fire(event);
    }

    /**
     * Indicates that this service exposes resource model.
     *
     * @return {@literal true}, if this service exposes resource model; otherwise, {@literal false}.
     */
    @Override
    public boolean isResourceModel() {
        for (final Class<?> clazz : InheritanceNavigator.of(getClass(), AbstractWebConsoleService.class))
            if (clazz.isAnnotationPresent(Path.class))
                return true;
        return false;
    }

    @Override
    public void close() throws IOException {
        listeners.clear();
    }
}
