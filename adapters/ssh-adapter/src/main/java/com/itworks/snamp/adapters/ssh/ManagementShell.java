package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.WriteOnceRef;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents management shell.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManagementShell implements Command {
    private static final String COMMAND_DELIMITIER = "\\p{javaWhitespace}+";

    private static final class Interpreter extends Thread {
        private final AdapterController controller;
        private final InputStream inStream;
        private final OutputStream outStream;
        private final OutputStream errStream;
        private final ExitCallback callback;
        private final Logger logger;

        public Interpreter(final AdapterController controller,
                           final InputStream is,
                           final OutputStream os,
                           final OutputStream es,
                           final ExitCallback callback,
                           final Logger l) {
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            this.outStream = SystemUtils.IS_OS_LINUX ?
                    new FilterOutputStream(os){
                        @Override
                        public void write(final int i) throws IOException {
                            super.write(i);
                            // workaround for MacOSX!! reset line after CR..
                            if(i == ConsoleReader.CR.charAt(0))
                                super.write(ConsoleReader.RESET_LINE);
                        }
                    }: os;
            this.errStream = Objects.requireNonNull(es, "es is null.");
            this.inStream = Objects.requireNonNull(is, "is is null.");
            this.logger = Objects.requireNonNull(l, "l is null.");
            this.callback = callback;
            setDaemon(true);
            setName("SSH Adapter Shell Interpreter");
        }

        @Override
        public void run() {
            try (final PrintWriter error = new PrintWriter(errStream)) {
                final ConsoleReader reader = new ConsoleReader(inStream, outStream);
                final PrintWriter output = new PrintWriter(reader.getOutput());

                reader.setPrompt("ssh-adapter> ");
                reader.addCompleter(HelpShellCommand.createCommandCompleter());
                output.println("Welcome! You are connected to SNAMP SSH adapter.");
                output.println("Print 'help' to see all available commands.");
                output.println();
                output.flush();

                String command;
                while (!Objects.equals(command = reader.readLine(), ExitCommand.COMMAND_NAME))
                    if (command != null && command.length() > 0) {
                        doCommand(command, controller, output, error, logger);
                        output.flush();
                    }
                if (callback != null) callback.onExit(0);
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Network I/O problems detected.", e);
                if (callback != null) callback.onExit(-1, e.getMessage());
            }
        }
    }

    private final WriteOnceRef<InputStream> inStream;
    private final WriteOnceRef<OutputStream> outStream;
    private final WriteOnceRef<OutputStream> errStream;
    private final AdapterController controller;
    private final WriteOnceRef<ExitCallback> exitCallback;
    private final WriteOnceRef<Interpreter> interpreter;
    private final Logger logger;

    private ManagementShell(final AdapterController controller,
                            final Logger l) {
        this.controller = Objects.requireNonNull(controller, "controller is null.");
        this.logger = Objects.requireNonNull(l, "l is null.");
        inStream = new WriteOnceRef<>();
        outStream = new WriteOnceRef<>();
        errStream = new WriteOnceRef<>();
        exitCallback = new WriteOnceRef<>();
        interpreter = new WriteOnceRef<>();
    }

    public static Factory<Command> createFactory(final AdapterController controller,
                                                 final Logger l) {
        return new Factory<Command>() {
            @Override
            public Command create() {
                return new ManagementShell(controller, l);
            }
        };
    }

    /**
     * Set the input stream that can be used by the shell to read input.
     *
     * @param value The input stream to be associated with this command.
     */
    @Override
    public final void setInputStream(final InputStream value) {
        inStream.set(value);
    }

    /**
     * Set the output stream that can be used by the shell to write its output.
     *
     * @param value The output stream to be associated with this command.
     */
    @Override
    public final void setOutputStream(final OutputStream value) {
        outStream.set(value);
    }

    /**
     * Set the error stream that can be used by the shell to write its errors.
     *
     * @param value The error stream to be associated with this command.
     */
    @Override
    public final void setErrorStream(final OutputStream value) {
        errStream.set(value);
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
     * @param env Command execution environment.
     */
    @Override
    public void start(final Environment env) {
        final Interpreter i = new Interpreter(controller,
                inStream.get(),
                outStream.get(),
                errStream.get(),
                exitCallback.get(),
                logger);
        i.start();
        interpreter.set(i);
    }

    /**
     * Destroy the shell.
     * This method can be called by the SSH server to destroy the shell because
     * the client has disconnected somehow.
     */
    @Override
    public void destroy() {
        interpreter.get().interrupt();
    }

    static Command createSshCommand(final String commandLine,
                                 final AdapterController controller,
                                 final Logger logger) {
        final String[] parts = commandLine.split(COMMAND_DELIMITIER);
        final ManagementShellCommand factory = createCommand(parts[0], controller, logger);
        return factory.createSshCommand(ArrayUtils.remove(parts, 0));
    }

    static void doCommand(final String commandLine,
                                  final AdapterController controller,
                                  final PrintWriter outStream,
                                  final PrintWriter errStream,
                                  final Logger logger){
        final String[] parts = commandLine.split(COMMAND_DELIMITIER);
        doCommand(parts[0],
                ArrayUtils.remove(parts, 0),
                controller,
                outStream,
                errStream,
                logger);
    }

    private static ManagementShellCommand createCommand(final String command,
                                                        final AdapterController controller,
                                                        final Logger logger){
        switch (command) {
            case HelpShellCommand.COMMAND_NAME:
                return new HelpShellCommand(controller);
            default:
                return new UnknownShellCommand(command);
        }
    }

    private static void doCommand(final String command,
                          final String[] arguments,
                          final AdapterController controller,
                          final PrintWriter outStream,
                          final PrintWriter errStream,
                          final Logger logger){
        final ManagementShellCommand executor = createCommand(command, controller, logger);
        executor.doCommand(arguments, outStream, errStream);
    }
}
