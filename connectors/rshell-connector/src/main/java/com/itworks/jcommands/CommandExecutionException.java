package com.itworks.jcommands;

import net.schmizz.sshj.common.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an exception that is thrown by command that doesn't relate to I/O problems.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CommandExecutionException extends Exception {
    /**
     *
     * @param message
     */
    public CommandExecutionException(final String message){
        super(message);
    }

    public static void readAndThrow(final InputStream err) throws CommandExecutionException, IOException{
        try(final ByteArrayOutputStream os = IOUtils.readFully(err)){
            if(os.size() > 0) throw new CommandExecutionException(os.toString());
        }
    }

    public <E extends Throwable> CommandExecutionException(final String message, final E innerException){
        super(message, innerException);
    }
}
