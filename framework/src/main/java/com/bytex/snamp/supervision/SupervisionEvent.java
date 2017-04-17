package com.bytex.snamp.supervision;

import javax.annotation.Nonnull;
import java.util.EventObject;

/**
 * Represents resource discovery event.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class SupervisionEvent extends EventObject {
    private static final long serialVersionUID = -4934599821637719691L;
    private final String groupName;

    SupervisionEvent(@Nonnull final Object source,
                     @Nonnull final String groupName) {
        super(source);
        this.groupName = groupName;
    }

    public final String getGroupName(){
        return groupName;
    }
}
