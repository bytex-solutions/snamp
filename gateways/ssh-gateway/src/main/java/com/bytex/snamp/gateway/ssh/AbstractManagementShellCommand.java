package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.base.Joiner;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Represents SSH gateway shell command.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
abstract class AbstractManagementShellCommand extends BasicParser implements ManagementShellCommand {
    protected static final Option RESOURCE_OPTION = new Option("r",
            "resource",
            true,
            "Name of the managed resource");
    final static Options EMPTY_OPTIONS = new Options();



    static class CommandException extends Exception {
        private static final long serialVersionUID = -1305857667263784898L;

        CommandException(final String message, final Object... args) {
            super(String.format(message, args));
        }

        CommandException(final Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }

    private final CommandExecutionContext context;

    protected AbstractManagementShellCommand(final CommandExecutionContext context) {
        this.context = Objects.requireNonNull(context, "context is null.");
    }

    protected final <T> Optional<T> getService(final Class<T> serviceType){
        return context.queryObject(serviceType);
    }

    protected final GatewayController getGatewayController(){
        return getService(CommandExecutionContext.CONTROLLER).orElseThrow(AssertionError::new);
    }

    protected final ExecutorService getExecutionService(){
        return getService(CommandExecutionContext.EXECUTOR).orElseThrow(AssertionError::new);
    }

    protected final InputStream getConsoleInputStream(){
        return getService(InputStream.class).orElseThrow(AssertionError::new);
    }

    protected abstract Options getCommandOptions();

    protected abstract void doCommand(final CommandLine input, final PrintWriter output) throws CommandException;

    @Override
    public final void doCommand(final String[] arguments,
                                final PrintWriter outStream,
                                final PrintWriter errStream) {
        try {
            final CommandLine input = parse(getCommandOptions(), arguments);
            doCommand(input, outStream);
            outStream.flush();
        } catch (final Throwable e) {
            errStream.println(e.getMessage());
            errStream.flush();
            LoggerProvider.getLoggerForObject(this).log(Level.WARNING, String.format("Unable to process command %s with arguments %s",
                    getClass().getSimpleName(),
                    Joiner.on(' ').join(arguments)),
                    e);
        }
    }

    private static final class CarriageReturnFix extends FilterOutputStream{
        CarriageReturnFix(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int b) throws IOException {
            switch (b){
                case '\n':
                case '\r':return;
                default: super.write(b);
            }
        }
    }

    @Override
    public final Command createSshCommand(final String[] arguments) {
        return new Command() {
            private OutputStream outStream;
            private OutputStream errorStream;
            private ExitCallback callback;
            private Future<?> commandExecutor;

            @Override
            @MethodStub
            public void setInputStream(final InputStream in) {
                //nothing to do
            }

            @Override
            public void setOutputStream(final OutputStream out) {
                outStream = out;
            }

            @Override
            public void setErrorStream(final OutputStream err) {
                errorStream = err;
            }

            @Override
            public void setExitCallback(final ExitCallback value) {
                callback = value;
            }

            @Override
            public void start(final Environment env) throws IOException {
                final OutputStream err = TtyOutputStream.needToApply() ? new TtyOutputStream(errorStream) : errorStream;
                final ExitCallback exitCallback = callback;
                final OutputStream out = new CarriageReturnFix(outStream);
                commandExecutor = getExecutionService().submit(() -> {
                    try (final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                         final PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                        AbstractManagementShellCommand.this.doCommand(arguments, outWriter, errWriter);
                    } finally {
                        exitCallback.onExit(0);
                    }
                });
            }

            @Override
            public void destroy() {
                final Future<?> task = commandExecutor;
                if (task != null)
                    task.cancel(true);
            }
        };
    }

    protected static CommandException invalidCommandFormat() {
        return new CommandException("Invalid command format");
    }
}
