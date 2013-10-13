package com.snamp.hosting;

import com.snamp.TimeSpan;

import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents Read-Evaluation-Print loop for the SNAMP commands.
 * @author roman
 */
abstract class ReplServer {
    /**
     * Initializes a new REPL-server.
     */
    protected ReplServer(){

    }

    protected abstract boolean doCommand(final String commmand, final PrintStream output);

    public final void loop(final InputStream input, final PrintStream output) throws IOException{
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(input))){
            while (doCommand(reader.readLine(), output)){

            }
        }

    }
}
