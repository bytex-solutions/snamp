package com.bytex.snamp.core;

import com.bytex.snamp.Box;

import java.io.Serializable;

/**
 * Represents {@link Box} that can be shared across cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SharedBox extends Box<Serializable>, SharedObject {
}
