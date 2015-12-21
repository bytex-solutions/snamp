package com.bytex.snamp.adapters.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.jmx.WellKnownType;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.Servlet3Continuation;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAdapterHelpers {

    private HttpAdapterHelpers(){

    }

    //do not remove. It is necessary for Atmosphere and maven-bundle-plugin for correct import of Jetty package
    @SpecialUse
    private static Class<? extends Continuation> getJettyContinuationClass(){
        return Servlet3Continuation.class;
    }

    static String getJsonType(final WellKnownType type){
        switch (type){
            case STRING:
            case CHAR: return "String";
            case BIG_INT:
            case BIG_DECIMAL:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE: return "Number";
            case BOOL: return "Boolean";
            default:
                if(type.isArray() || type.isBuffer()) return "Array";
            case DICTIONARY:
            case TABLE:
                return "Object";
        }
    }
}
