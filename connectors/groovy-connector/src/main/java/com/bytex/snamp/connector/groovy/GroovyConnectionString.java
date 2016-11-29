package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ArrayUtils;
import com.google.common.base.Splitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Provides parsed connection string.
 * @since 2.0
 * @version 2.0
 */
final class GroovyConnectionString {
    private static final Splitter PATH_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private final String scriptName;
    private final URL[] path;

    GroovyConnectionString(final String connectionString) throws MalformedURLException {
        final List<String> path = PATH_SPLITTER.splitToList(connectionString);
        switch (path.size()) {
            case 0:
                throw new MalformedURLException("Malformed connection string");
            case 1:
                scriptName = path.get(0);
                this.path = ArrayUtils.emptyArray(URL[].class);
                break;
            default:
                scriptName = path.get(0);
                this.path = new URL[path.size() - 1];
                for(int i = 1; i < path.size(); i++)
                    this.path[i - 1] = new URL(path.get(i));
        }
    }

    String getScriptName(){
        return scriptName;
    }

    URL[] getScriptPath(){
        return path;
    }
}
