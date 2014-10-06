package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Wrapper;
import com.itworks.snamp.WriteOnceRef;
import com.itworks.snamp.internal.annotations.MethodStub;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Represents SSH adapter shell command.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class ManagementShellCommand extends BasicParser implements Command, SessionAware, Runnable {
    /**
     * Represents an exception raised by command interpreter.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected class CommandException extends Exception{
        public CommandException(final Throwable cause){
            super(cause);
        }

        public CommandException(final String message, final Throwable cause){
            super(message, cause);
        }

        public CommandException(final String message){
            super(message);
        }
    }

    protected static final String[] EMPTY_ARGS = new String[0];
    private final WriteOnceRef<InputStream> in;
    private final WriteOnceRef<OutputStream> out;
    private final WriteOnceRef<OutputStream> err;
    private final WriteOnceRef<ExitCallback> exitCallback;
    private final WriteOnceRef<Future> task;
    private WeakReference<ServerSession> session;
    protected final AdapterController controller;
    private final String[] commandArgs;

    protected ManagementShellCommand(final AdapterController controller,
                                     final String[] args) {
        in = new WriteOnceRef<>();
        out = new WriteOnceRef<>();
        err = new WriteOnceRef<>();
        task = new WriteOnceRef<>();
        exitCallback = new WriteOnceRef<>();
        session = null;
        this.controller = Objects.requireNonNull(controller, "controller is null.");
        this.commandArgs = args;
    }

    /**
     * Set the input stream that can be used by the shell to read input.
     *
     * @param value The input stream to be associated with this command.
     */
    @Override
    public final void setInputStream(final InputStream value) {
        in.set(value);
    }

    /**
     * Set the output stream that can be used by the shell to write its output.
     *
     * @param value The output stream to be associated with this command.
     */
    @Override
    public final void setOutputStream(final OutputStream value) {
        out.set(value);
    }

    /**
     * Set the error stream that can be used by the shell to write its errors.
     *
     * @param value The error stream to be associated with this command.
     */
    @Override
    public final void setErrorStream(final OutputStream value) {
        err.set(value);
    }

    /**
     * Set the callback that the shell has to call when it is closed.
     *
     * @param value The callback to be associated with this command.
     */
    @Override
    public final void setExitCallback(final ExitCallback value) {
        exitCallback.set(value);
    }

    /**
     * Set the server session in which this shell will be executed.
     *
     * @param value The session that executes this command.
     */
    @Override
    public final void setSession(final ServerSession value) {
        session = new WeakReference<>(value);
    }

    /**
     * Gets the server session associated with this command.
     * @return The server session associated with this command.
     */
    protected final ServerSession getSession(){

        return session != null ? session.get() : null;
    }

    /**
     * Gets options associated with this command.
     * @return The command options.
     */
    protected abstract Options getCommandOptions();

    /**
     * Executes command synchronously.
     * @param input Parsed command line.
     * @param output Output stream for the command execution result.
     * @throws CommandException An exception occurred during command interpretation.
     */
    protected abstract void doCommand(final CommandLine input, final PrintStream output) throws CommandException;

    /**
     * Executes command synchronously.
     * @see #doCommand(org.apache.commons.cli.CommandLine, java.io.PrintStream)
     */
    @Override
    public final void run() {
        try (final PrintStream printStream = new PrintStream(out.get(), true)) {
            doCommand(parse(getCommandOptions(), commandArgs), printStream);
        } catch (final CommandException | ParseException e) {
            try (final PrintStream errorStream = new PrintStream(err.get(), true)) {
                errorStream.println(e.getMessage());
            }
        } catch (final Exception e) {
            try (final PrintStream errorStream = new PrintStream(err.get(), true)) {
                errorStream.println(e);
                e.printStackTrace(errorStream);
            }
            controller.getLogger().log(Level.SEVERE, "Unexpected exception in SSH server", e);
        }
    }

    /**
     * Starts the shell.
     * All streams must have been set before calling this method.
     * The command should implement Runnable, and this method
     * should spawn a new thread like:
     * <pre>
     * {@code
     * Thread(this).start();
     * }
     * </pre>
     *
     * @param env An environment for the command.
     * @throws java.io.IOException
     */
    @Override
    public final void start(final Environment env) throws IOException {
        task.set(controller.getCommandExecutorService().submit(this));
    }

    /**
     * Destroy the shell.
     * This method can be called by the SSH server to destroy the shell because
     * the client has disconnected somehow.
     */
    @Override
    @MethodStub
    public final void destroy() {
        task.handle(new Wrapper.WrappedObjectHandler<Future, Void>() {
            @Override
            public Void invoke(final Future task) {
                if (task != null) task.cancel(true);
                return null;
            }
        });
    }
}
