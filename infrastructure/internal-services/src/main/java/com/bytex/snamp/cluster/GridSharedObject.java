package com.bytex.snamp.cluster;

import com.bytex.snamp.core.SharedObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class GridSharedObject implements SharedObject {
    abstract void destroy();
}
