package com.bytex.snamp.connector.health;

import java.time.Instant;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Something wrong with managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ResourceMalfunctionStatus extends MalfunctionStatus {
    private static final long serialVersionUID = -1766747580186741189L;
    private final String resourceName;

    protected ResourceMalfunctionStatus(final String resourceName,
                              final Instant timeStamp) {
        super(timeStamp);
        if(isNullOrEmpty(resourceName))
            throw new IllegalArgumentException("Resource name is not specified");
        this.resourceName = resourceName;
    }

    final boolean equalsHelper(final ResourceMalfunctionStatus other){
        return other.getResourceName().equals(resourceName) &&
                other.getTimeStamp().equals(getTimeStamp()) &&
                other.getLevel().equals(getLevel());
    }

    /**
     * Gets name of the problematic resource.
     * @return Name of the problematic resource.
     */
    public final String getResourceName(){
        return resourceName;
    }
}
