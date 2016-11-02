package com.bytex.snamp.scripting.groovy;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenDataScriptHelpers {
    private static final String ITEM_DESCR = "description";
    private static final String ITEM_TYPE = "type";
    private static final String ITEM_INDEXED = "indexed";

    private OpenDataScriptHelpers(){
        throw new InstantiationError();
    }

    static String getDescription(final Map item,
                                         final String fallback) {
        return item.containsKey(ITEM_DESCR) ?
                Objects.toString(item.get(ITEM_DESCR)) : fallback;
    }

    static OpenType<?> getType(final Map item) throws OpenDataException {
        final Object result = item.get(ITEM_TYPE);
        if (result instanceof OpenType<?>)
            return (OpenType<?>) result;
        else throw new OpenDataException("Item type is not declared");
    }

    static boolean isIndexed(final Map column) {
        if (column.containsKey(ITEM_INDEXED)) {
            final Object indexed = column.get(ITEM_INDEXED);
            if (indexed instanceof Boolean)
                return (Boolean) indexed;
            else if (indexed instanceof String)
                return ((String) indexed).length() > 0;
            else return indexed != null;
        } else return false;
    }
}
