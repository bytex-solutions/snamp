package com.snamp.adapters;

import com.snamp.MethodThreadSafety;
import com.snamp.ThreadSafety;
import com.snamp.connectors.*;

import java.lang.ref.*;
import java.util.*;

/**
 * Represents a map of exposed attributes to the adapter.
 * @author roman
 */
public abstract class ConnectedAttributes extends HashMap<String, AttributeMetadata> {
    private final Reference<ManagementConnector> connector;

    protected ConnectedAttributes(final ManagementConnector connector){
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        this.connector = new WeakReference<ManagementConnector>(connector);
    }

    final ManagementConnector getConnector(){
        return connector.get();
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract String makeAttributeId(final String prefix, final String postfix);
}
