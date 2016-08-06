package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.ReadAttributeLogicalOperation;
import com.bytex.snamp.adapters.modeling.WriteAttributeLogicalOperation;
import com.bytex.snamp.jmx.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SshHelpers {
    static final String ADAPTER_NAME = "ssh";
    private static final String LOGGER_NAME = AbstractResourceAdapter.getLoggerName(ADAPTER_NAME);
    static final Gson FORMATTER = JsonUtils.registerTypeAdapters(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private SshHelpers(){

    }

    private static Logger getLogger(){
        return Logger.getLogger(LOGGER_NAME);
    }

    static ReadAttributeLogicalOperation readAttributeLogicalOperation(final String originalName,
                                                                       final String attributeName) {
        return new ReadAttributeLogicalOperation(getLogger(), originalName, attributeName);
    }

    static WriteAttributeLogicalOperation writeAttributeLogicalOperation(final String originalName,
                                                                         final String attributeName){
        return new WriteAttributeLogicalOperation(getLogger(), originalName, attributeName);
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e) {
        getLogger().log(lvl, String.format(message, args), e);
    }

    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, emptyArray(String[].class), e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1}, e);
    }
}
