package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.MethodStub;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Represents SSH adapter shell command.
 * @author Roman Sakno
 * @version 1.0
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

    protected final <T> T getService(final Class<T> serviceType){
        return context.queryObject(serviceType);
    }

    protected final AdapterController getAdapterController(){
        return getService(CommandExecutionContext.CONTROLLER);
    }

    protected final ExecutorService getExecutionService(){
        return getService(CommandExecutionContext.EXECUTOR);
    }

    protected final InputStream getConsoleInputStream(){
        return getService(InputStream.class);
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
            SshHelpers.log(Level.WARNING, "Unable to process command %s with arguments %s",
                    getClass().getSimpleName(),
                    Joiner.on(' ').join(arguments),
                    e);
        }
    }

    @Override
    public final Command createSshCommand(final String[] arguments) {
        return new Command() {
            private final WriteOnceRef<OutputStream> outStream = new WriteOnceRef<>();
            private final WriteOnceRef<OutputStream> errorStream = new WriteOnceRef<>();
            private final WriteOnceRef<ExitCallback> callback = new WriteOnceRef<>();
            private final WriteOnceRef<Future> commandExecutor = new WriteOnceRef<>();

            @Override
            @MethodStub
            public void setInputStream(final InputStream in) {
                //nothing to do
            }

            @Override
            public void setOutputStream(final OutputStream out) {
                outStream.set(out);
            }

            @Override
            public void setErrorStream(final OutputStream err) {
                errorStream.set(err);
            }

            @Override
            public void setExitCallback(final ExitCallback callback) {
                this.callback.set(callback);
            }

            @Override
            public void start(final Environment env) throws IOException {
                commandExecutor.set(getExecutionService().submit(new Runnable() {
                    private final OutputStream out = new FilterOutputStream(outStream.get()){
                        @Override
                        public void write(final int b) throws IOException {
                            switch (b){
                                case '\n':
                                case '\r':return;
                                default: super.write(b);
                            }
                        }
                    };
                    private final OutputStream err = TtyOutputStream.needToApply() ?
                            new TtyOutputStream(errorStream.get()) : errorStream.get();
                    private final ExitCallback exitCallback = callback.get();

                    @Override
                    public void run() {
                        try (final PrintWriter out = new PrintWriter(new OutputStreamWriter(this.out, StandardCharsets.UTF_8));
                             final PrintWriter err = new PrintWriter(new OutputStreamWriter(this.out, StandardCharsets.UTF_8))) {
                            AbstractManagementShellCommand.this.doCommand(arguments, out, err);
                        }

                        finally {
                            exitCallback.onExit(0);
                        }
                    }
                }));
            }

            @Override
            public void destroy() {
                final Future task = commandExecutor.get();
                if (task != null) task.cancel(true);
            }
        };
    }

    protected static CommandException invalidCommandFormat() {
        return new CommandException("Invalid command format");
    }
}
