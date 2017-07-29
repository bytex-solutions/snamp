package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.core.LoggerProvider;
import jline.console.ConsoleReader;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents management shell.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class ManagementShell implements Command, SessionAware {
    private static final Pattern COMMAND_DELIMITER = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    private static final class CommandExecutionContextImpl implements CommandExecutionContext{
        private final Aggregator aggregator;

        private CommandExecutionContextImpl(final GatewayController controller,
                                            final ExecutorService executor){
            aggregator = AbstractAggregator.builder()
                    .addValue(CONTROLLER, controller)
                    .addValue(EXECUTOR, executor)
                    .build();
        }

        private CommandExecutionContextImpl(final GatewayController controller,
                                            final ExecutorService executor,
                                            final InputStream reader) {
            aggregator = AbstractAggregator.builder()
                    .addValue(CONTROLLER, controller)
                    .addValue(EXECUTOR, executor)
                    .addValue(INPUT_STREAM, reader)
                    .build();
        }

        @Override
        public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
            return aggregator.queryObject(objectType);
        }
    }

    private static final class Interpreter extends Thread {
        private final GatewayController controller;
        private final OutputStream errStream;
        private final InputStream inStream;
        private final OutputStream outStream;
        private final ExitCallback callback;
        private final ExecutorService executor;

        private Interpreter(final GatewayController controller,
                           final InputStream is,
                           OutputStream os,
                           final OutputStream es,
                           final ExitCallback callback,
                           final ExecutorService executor) {
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            if (TtyOutputStream.needToApply()) {
                this.outStream = new TtyOutputStream(os);
                this.errStream = new TtyOutputStream(es);
            } else {
                this.errStream = es;
                this.outStream = os;
            }
            this.inStream = is;
            this.callback = callback;
            this.executor = Objects.requireNonNull(executor, "executor is null.");
            setDaemon(true);
            setName("SSH Gateway Shell Interpreter");
        }

        @Override
        public void run() {
            try (final PrintWriter error = new PrintWriter(new OutputStreamWriter(errStream, StandardCharsets.UTF_8))) {
                final ConsoleReader reader = new ConsoleReader(inStream, outStream);
                reader.setExpandEvents(false);
                final PrintWriter output = new PrintWriter(reader.getOutput());
                reader.setPrompt("ssh-gateway> ");
                reader.addCompleter(HelpCommand.createCommandCompleter());
                output.println("Welcome! You are connected to SNAMP SSH gateway.");
                output.println("Print 'help' to see all available commands.");
                output.println();
                output.flush();

                String command;
                while (!Objects.equals(command = reader.readLine(), ExitCommand.COMMAND_NAME))
                    if (command != null && command.length() > 0) {
                        doCommand(command,
                                new CommandExecutionContextImpl(controller, executor, inStream),
                                output, error);
                        output.flush();
                    }
                if (callback != null) callback.onExit(0);
            } catch (final IOException e) {
                LoggerProvider.getLoggerForObject(this).log(Level.SEVERE, "Network I/O problems detected.", e);
                if (callback != null) callback.onExit(-1, e.getMessage());
            }
        }
    }

    private InputStream inStream;
    private OutputStream outStream;
    private OutputStream errStream;
    private final GatewayController controller;
    private ExitCallback exitCallback;
    private Interpreter interpreter;
    private Session session;
    private final ExecutorService executor;

    private ManagementShell(final GatewayController controller,
                            final ExecutorService executor) {
        this.controller = Objects.requireNonNull(controller, "controller is null.");
        this.executor = executor;
    }

    static Factory<Command> createFactory(final GatewayController controller,
                                          final ExecutorService executor) {
        return () -> new ManagementShell(controller, executor);
    }

    /**
     * Set the input stream that can be used by the shell to read input.
     *
     * @param value The input stream to be associated with this command.
     */
    @Override
    public void setInputStream(final InputStream value) {
        inStream = Objects.requireNonNull(value);
    }

    /**
     * Set the output stream that can be used by the shell to write its output.
     *
     * @param value The output stream to be associated with this command.
     */
    @Override
    public void setOutputStream(final OutputStream value) {
        outStream = Objects.requireNonNull(value);
    }

    /**
     * Set the error stream that can be used by the shell to write its errors.
     *
     * @param value The error stream to be associated with this command.
     */
    @Override
    public void setErrorStream(final OutputStream value) {
        errStream = Objects.requireNonNull(value);
    }

    /**
     * Set the callback that the shell has to call when it is closed.
     *
     * @param value The callback to be associated with this command.
     */
    @Override
    public void setExitCallback(final ExitCallback value) {
        exitCallback = Objects.requireNonNull(value);
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
        interpreter = new Interpreter(controller, inStream, outStream, errStream, exitCallback, executor);
        interpreter.start();
    }

    /**
     * Destroy the shell.
     * This method can be called by the SSH server to destroy the shell because
     * the client has disconnected somehow.
     */
    @Override
    public void destroy() {
        interpreter.interrupt();
        interpreter = null;
    }

    /**
     * Set the server session in which this shell will be executed.
     *
     * @param value The server session to be associated with this shell.
     */
    @Override
    public void setSession(final ServerSession value) {
        session = Objects.requireNonNull(value);
    }

    private static String[] splitArguments(final String value){
        final List<String> matchList = new LinkedList<>();
        final Matcher regexMatcher = COMMAND_DELIMITER.matcher(value);
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
        return matchList.toArray(emptyArray(String[].class));
    }

    private static Command createSshCommand(final String commandLine,
                                            final CommandExecutionContext controller) {
        final String[] parts = splitArguments(commandLine);
        final ManagementShellCommand factory = createCommand(ArrayUtils.getFirst(parts).orElseThrow(AssertionError::new), controller);
        return factory.createSshCommand(ArrayUtils.remove(parts, 0));
    }

    static Command createSshCommand(final String commandLine,
                                    final GatewayController controller,
                                    final ExecutorService executor){
        return createSshCommand(commandLine,
                new CommandExecutionContextImpl(controller, executor));
    }

    static void doCommand(final String commandLine,
                                  final CommandExecutionContext context,
                                  final PrintWriter outStream,
                                  final PrintWriter errStream){
        final String[] parts = splitArguments(commandLine);
        doCommand(ArrayUtils.getFirst(parts).orElseThrow(IllegalArgumentException::new),
                ArrayUtils.remove(parts, 0),
                context,
                outStream,
                errStream);
    }

    private static ManagementShellCommand createCommand(final String command,
                                                        final CommandExecutionContext context){
        switch (command) {
            case HelpCommand.COMMAND_NAME:
                return new HelpCommand(context);
            case ExitCommand.COMMAND_NAME:
                return new ExitCommand(context);
            case ListOfResourcesCommand.COMMAND_NAME:
                return new ListOfResourcesCommand(context);
            case ListOfAttributesCommand.COMMAND_NAME:
                return new ListOfAttributesCommand(context);
            case GetAttributeCommand.COMMAND_NAME:
                return new GetAttributeCommand(context);
            case SetAttributeCommand.COMMAND_NAME:
                return new SetAttributeCommand(context);
            case NotificationsCommand.COMMAND_NAME:
                return new NotificationsCommand(context);
            case HealthStatusCommand.COMMAND_NAME:
                return new HealthStatusCommand(context);
            default:
                return new UnknownShellCommand(command);
        }
    }

    private static void doCommand(final String command,
                          final String[] arguments,
                          final CommandExecutionContext context,
                          final PrintWriter outStream,
                          final PrintWriter errStream){
        final ManagementShellCommand executor = createCommand(command, context);
        executor.doCommand(arguments, outStream, errStream);
    }
}
