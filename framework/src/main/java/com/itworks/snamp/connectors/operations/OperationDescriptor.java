package com.itworks.snamp.connectors.operations;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.CompositeData;

import static com.itworks.snamp.connectors.operations.OperationSupport.*;
import static com.itworks.snamp.jmx.DescriptorUtils.*;

/**
 * Represents descriptor of the managed resource operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class OperationDescriptor extends ImmutableDescriptor {
    private static final long serialVersionUID = -6350507145892936614L;

    public OperationDescriptor(final String operationName,
                               final CompositeData options){

    }

    public static boolean isAsynchronous(final Descriptor descriptor){
        if(hasField(descriptor, ASYNC_FIELD)){
            final Object result = descriptor.getFieldValue(ASYNC_FIELD);
            if(result instanceof Boolean)
                return (Boolean)result;
            else if(result instanceof String)
                return Boolean.valueOf((String)result);
        }
        return false;
    }

    public static boolean isAsynchronous(final MBeanOperationInfo metadata){
        return isAsynchronous(metadata.getDescriptor());
    }

    public final boolean isAsynchronous(){
        return isAsynchronous(this);
    }
}
