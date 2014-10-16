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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents management shell.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManagementShell implements Command {
    private static final Pattern COMMAND_DELIMITIER = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    private static final class TtyOutputStream extends FilterOutputStream {
        private TtyOutputStream(final OutputStream underlyingStream) {
            super(underlyingStream);
        }

        @Override
        public void write(final int i) throws IOException {
            super.write(i);
            // workaround for MacOSX and Linux reset line after CR..
            if (i == ConsoleReader.CR.charAt(0))
                super.write(ConsoleReader.RESET_LINE);
        }
    }

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
            if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
                this.outStream = new TtyOutputStream(os);
                this.errStream = new TtyOutputStream(es);
            } else {
                this.outStream = os;
                this.errStream = es;
            }
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
                reader.setExpandEvents(false);
                final PrintWriter output = new PrintWriter(reader.getOutput());
                reader.setPrompt("ssh-adapter> ");
                reader.addCompleter(HelpCommand.createCommandCompleter());
                output.println("Welcome! You are connected to SNAMP SSH adapter.");
                output.println("Print 'help' to see all available commands.");
                output.println();
                output.flush();

                String command;
                while (!Objects.equals(command = reader.readLine(), ExitCommand.COMMAND_NAME))
                    if (command != null && command.length() > 0) {
                        doCommand(command, controller, output, error);
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

    private static String[] splitArguments(final String value){
        final List<String> matchList = new LinkedList<>();
        final Matcher regexMatcher = COMMAND_DELIMITIER.matcher(value);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        return matchList.toArray(new String[matchList.size()]);
    }

    static Command createSshCommand(final String commandLine,
                                 final AdapterController controller) {
        final String[] parts = splitArguments(commandLine);
        final ManagementShellCommand factory = createCommand(parts[0], controller);
        return factory.createSshCommand(ArrayUtils.remove(parts, 0));
    }

    static void doCommand(final String commandLine,
                                  final AdapterController controller,
                                  final PrintWriter outStream,
                                  final PrintWriter errStream){
        final String[] parts = splitArguments(commandLine);
        doCommand(parts[0],
                ArrayUtils.remove(parts, 0),
                controller,
                outStream,
                errStream);
    }

    private static ManagementShellCommand createCommand(final String command,
                                                        final AdapterController controller){
        switch (command) {
            case HelpCommand.COMMAND_NAME:
                return new HelpCommand(controller);
            case ExitCommand.COMMAND_NAME:
                return new ExitCommand(controller);
            case ListOfResourcesCommand.COMMAND_NAME:
                return new ListOfResourcesCommand(controller);
            case ListOfAttributesCommand.COMMAND_NAME:
                return new ListOfAttributesCommand(controller);
            case GetAttributeCommand.COMMAND_NAME:
                return new GetAttributeCommand(controller);
            case SetAttributeCommand.COMMAND_NAME:
                return new SetAttributeCommand(controller);
            case SetArrayCommand.COMMAND_NAME:
                return new SetArrayCommand(controller);
            default:
                return new UnknownShellCommand(command);
        }
    }

    private static void doCommand(final String command,
                          final String[] arguments,
                          final AdapterController controller,
                          final PrintWriter outStream,
                          final PrintWriter errStream){
        final ManagementShellCommand executor = createCommand(command, controller);
        executor.doCommand(arguments, outStream, errStream);
    }
}
