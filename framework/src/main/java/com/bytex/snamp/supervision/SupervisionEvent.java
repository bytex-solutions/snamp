package com.bytex.snamp.supervision;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.EventObject;

/**
 * Represents resource discovery event.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class SupervisionEvent extends EventObject {
    private static final long serialVersionUID = -4934599821637719691L;
    private final String groupName;
    private final Instant timeStamp;

    protected SupervisionEvent(@Nonnull final Object source,
                     @Nonnull final String groupName) {
        super(source);
        this.groupName = groupName;
        this.timeStamp = Instant.now();
    }

    public final Instant getTimeStamp(){
        return timeStamp;
    }

    public final String getGroupName(){
        return groupName;
    }
}
