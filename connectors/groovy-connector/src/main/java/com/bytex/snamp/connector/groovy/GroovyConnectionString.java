package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.IOUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

/**
 * Provides parsed connection string.
 * @since 2.0
 * @version 2.0
 */
final class GroovyConnectionString {
    private final String scriptName;
    private final URL[] path;

    GroovyConnectionString(final String connectionString) throws MalformedURLException {
        final String[] path = IOUtils.splitPath(connectionString, String.class, Function.identity());
        switch (path.length){
            case 0:
                throw new MalformedURLException("Malformed connection string");
            case 1:
                scriptName = path[0];
                this.path = ArrayUtils.emptyArray(URL[].class);
                break;
            default:
                scriptName = path[0];
                this.path = new URL[path.length - 1];
                for(int i = 1; i < this.path.length; i++)
                    this.path[i - 1] = new URL(path[i]);
        }
    }

    String getScriptName(){
        return scriptName;
    }

    URL[] getScriptPath(){
        return path;
    }
}
