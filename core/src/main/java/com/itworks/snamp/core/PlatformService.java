package com.itworks.snamp.core;

import com.itworks.snamp.Aggregator;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

import java.util.logging.Logger;

/**
 * Represents a root interface for all SNAMP platform service.<br/>
 * <p>
 *     This interface uses as an indicator for architectural component of SNAMP.
 *     You should not implement this interface directly
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
public interface PlatformService extends Aggregator {

}
