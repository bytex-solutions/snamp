package com.bytex.snamp.gateway.http;

import com.bytex.snamp.jmx.WellKnownType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class HttpGatewayHelpers {

    private HttpGatewayHelpers(){

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
