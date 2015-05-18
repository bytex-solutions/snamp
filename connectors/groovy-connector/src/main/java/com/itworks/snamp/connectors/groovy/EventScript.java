package com.itworks.snamp.connectors.groovy;

import java.lang.ref.WeakReference;

/**
 * Represents an abstract class for event emitter script.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class EventScript extends ManagementScript implements NotificationEmitter {
    private WeakReference<NotificationEmitter> emitter;

    @Override
    public final void emitNotification(final String message) {
        final NotificationEmitter emitter = getEmitter();
        if(emitter != null)
            emitter.emitNotification(message, null);
    }

    private NotificationEmitter getEmitter(){
        final WeakReference<NotificationEmitter> emitter = this.emitter;
        return emitter != null ? emitter.get() : null;
    }

    @Override
    public final void emitNotification(final String message, final Object userData) {
        final NotificationEmitter emitter = getEmitter();
        if(emitter != null)
            emitter.emitNotification(message, userData);
    }

    public final void setEmitter(final NotificationEmitter value){
        this.emitter = new WeakReference<>(value);
    }

    @Override
    public void close() throws Exception {
        final NotificationEmitter emitter = getEmitter();
        try{
            if(emitter != null)
                emitter.close();
        }
        finally {
            this.emitter = null;
        }
    }
}
