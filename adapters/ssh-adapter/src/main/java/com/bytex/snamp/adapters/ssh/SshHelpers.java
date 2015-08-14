package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.ReadAttributeLogicalOperation;
import com.bytex.snamp.adapters.modeling.WriteAttributeLogicalOperation;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.jmx.json.Formatters;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshHelpers {
    static final TypeToken<Map<String, Object>> STRING_MAP_TYPE = new TypeToken<Map<String, Object>>() {};
    static final String ADAPTER_NAME = "ssh";
    private static final String LOGGER_NAME = AbstractResourceAdapter.getLoggerName(ADAPTER_NAME);
    static final Gson FORMATTER = Formatters.enableAll(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private SshHelpers(){

    }

    private static BundleContext getBundleContext(){
        return FrameworkUtil.getBundle(SshHelpers.class).getBundleContext();
    }

    static ReadAttributeLogicalOperation readAttributeLogicalOperation(final String originalName,
                                                                       final String attributeName) {
        return new ReadAttributeLogicalOperation(Logger.getLogger(LOGGER_NAME), originalName, attributeName, getBundleContext());
    }

    static WriteAttributeLogicalOperation writeAttributeLogicalOperation(final String originalName,
                                                                         final String attributeName){
        return new WriteAttributeLogicalOperation(Logger.getLogger(LOGGER_NAME), originalName, attributeName, getBundleContext());
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e){
        try(final LoggingScope logger = new LoggingScope(LOGGER_NAME, getBundleContext())){
            logger.log(lvl, String.format(message, args), e);
        }
    }

    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, new Object[0], e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Throwable e){
        log(lvl, message, new Object[]{arg0}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Object arg2, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1, arg2}, e);
    }
}
