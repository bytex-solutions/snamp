package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.SnampComponentDescriptor;

import java.beans.ConstructorProperties;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnampComponentInfo {
    private final String displayName;
    private final String description;
    private final String version;
    private final int state;

    public SnampComponentInfo(final SnampComponentDescriptor descriptor) {
        this(descriptor.getName(null), descriptor.getDescription(null), Objects.toString(descriptor.getVersion(), "UNKNOWN"), descriptor.getState());
    }

    @ConstructorProperties({"displayName", "description", "version", "state"})
    public SnampComponentInfo(final String displayName,
                       final String description,
                       final String version,
                       final int state) {
        this.displayName = displayName;
        this.description = description;
        this.version = version;
        this.state = state;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public int getState() {
        return state;
    }
}
