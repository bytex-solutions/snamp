package com.itworks.snamp.core;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.IllegalStateFlag;
import com.itworks.snamp.internal.annotations.MethodStub;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class LogicalOperation implements AutoCloseable {
    private static final ThreadLocal<LogicalOperation> operations = new ThreadLocal<>();
    static final CorrelationIdentifierGenerator correlIdGenerator = new DefaultCorrelationIdentifierGenerator();

    /**
     * Represents correlation identifier generator.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface CorrelationIdentifierGenerator{
        /**
         * Generates a new unique correlation identifier.
         * @return A new unique correlation identifier.
         */
        long generate();
    }

    protected static final class DefaultCorrelationIdentifierGenerator extends AtomicLong implements CorrelationIdentifierGenerator{
        public DefaultCorrelationIdentifierGenerator(final long initialValue){
            super(initialValue);
        }

        public DefaultCorrelationIdentifierGenerator(){
            this(0L);
        }

        @Override
        public long generate() {
            return getAndIncrement();
        }
    }

    private final String name;
    private LogicalOperation parent;
    private final IllegalStateFlag operationState = new IllegalStateFlag() {
        @Override
        protected IllegalStateException create() {
            return new IllegalStateException("Logical operation is finished");
        }
    };
    private final Stopwatch timer;
    private final long correlationID;

    /**
     * Initializes a new logical operation and push it into the stack.
     * @param name The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     * @param ticker The ticker used to measure duration of the logical operation execution.
     */
    protected LogicalOperation(final String name,
                               final CorrelationIdentifierGenerator correlationID,
                               final Ticker ticker){
        this.name = name;
        timer = Stopwatch.createStarted(ticker);
        if((parent = current()) != null)
            this.correlationID = parent.getCorrelationID();
        else if(correlationID != null)
            this.correlationID = correlationID.generate();
        else this.correlationID = -1;
        operations.set(this);
    }

    /**
     * Initializes a new logical operation and push it into the stack.
     * @param name The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     */
    public LogicalOperation(final String name,
                            final CorrelationIdentifierGenerator correlationID){
        this(name, correlationID, Ticker.systemTicker());
    }

    /**
     * Initializes a new logical operation and push it into the stack.
     * <p>
     *     This constructor generates a new unique correlation identifier
     *     across all threads in the application.
     * </p>
     * @param name The name of the logical operation.
     */
    public LogicalOperation(final String name){
        this(name, correlIdGenerator);
    }

    /**
     * Gets correlation identifier.
     * @return The correlation identifier.
     */
    public final long getCorrelationID(){
        return correlationID;
    }

    /**
     * Gets duration of logical operation execution.
     * @param desiredUnit The desired time measurement unit of returned value.
     * @return The duration of logical operation execution.
     */
    public final long getDuration(final TimeUnit desiredUnit){
        return timer.elapsed(desiredUnit);
    }

    /**
     * Gets duration of logical operation execution.
     * @return The duration of logical operation execution.
     */
    public final TimeSpan getDuration(){
        return new TimeSpan(getDuration(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    /**
     * Gets parent logical operation.
     * @return The parent logical operation.
     * @throws java.lang.IllegalStateException This operation is closed.
     */
    public final LogicalOperation getParent(){
        operationState.verify();
        return parent;
    }

    /**
     * Gets a value indicating that this operation is closed.
     * @return {@literal true}, if this operation is closed; otherwise, {@literal false}.
     */
    public final boolean isClosed(){
        return operationState.get();
    }

    /**
     * Gets name of this logical operation.
     * @return The name of this logical operation.
     */
    public final String getName(){
        return name;
    }

    /**
     * Gets current logical operation.
     * @return The current logical operation.
     */
    public static LogicalOperation current(){
        return operations.get();
    }

    /**
     * Processes the current logical operation.
     * @param handler The logical operation handler.
     * @param <E> Type of the exception that may be produced by handler.
     * @return {@literal true}, if the caller method is in logical operation; otherwise, {@literal false}.
     * @throws E Unable to process logical operation.
     */
    public static <E extends Throwable> boolean current(final Consumer<LogicalOperation, E> handler) throws E{
        final LogicalOperation current = current();
        if(current != null){
            handler.accept(current);
            return true;
        }
        else return false;
    }

    private static LogicalOperation pop(){
        final LogicalOperation current = operations.get();
        if(current != null) {
            operations.set(current.parent);
            current.parent = null;
        }
        return current;
    }

    /**
     * Captures the stack of logical operations at the current thread.
     * @return The stack of logical operations.
     */
    public static Stack<LogicalOperation> dumpStack(){
        final Stack<LogicalOperation> result = new Stack<>();
        LogicalOperation lookup = current();
        while (lookup != null){
            result.add(0, lookup);
            lookup = lookup.getParent();
        }
        return result;
    }

    protected final  <L extends LogicalOperation> L findParent(final Class<L> operationClass){
        LogicalOperation lookup = getParent();
        while (lookup != null)
            if(operationClass.isInstance(lookup)) return operationClass.cast(lookup);
            else lookup = lookup.parent;
        return null;
    }

    /**
     * Finds logical operation in the logical operation stack of the current thread by its type.
     * @param operationClass The type of the logical operation.
     * @param <L> The type of the logical operation.
     * @return The logical operation; or {@literal null} if the current stack doesn't contain
     *  the logical operation of appropriate type.
     */
    public static <L extends LogicalOperation> L find(final Class<L> operationClass){
        LogicalOperation lookup = current();
        return lookup != null ? lookup.findParent(operationClass) : null;
    }

    /**
     * Invokes at the end of the logical operation.
     */
    @MethodStub
    protected void onClose(){

    }

    /**
     * Collects string data used to create textual representation of this operation.
     * @param output An output map to populate with string data.
     */
    protected void collectStringData(final Map<String, Object> output) {
        output.put("name", name);
        output.put("duration", timer);
        output.put("correlationID", correlationID);
    }

    /**
     * Returns a string representation of this logical operation.
     * @return A string representation of this logical operation.
     */
    @Override
    public final String toString() {
        final Map<String, Object> result = new HashMap<>(10);
        collectStringData(result);
        return Joiner.on(',').withKeyValueSeparator("=").join(result);
    }

    /**
     * Pops the logical operation from the stack.
     */
    @Override
    public final void close() {
        try{
            onClose();
        }
        finally {
            pop();
            timer.stop();
            operationState.set();
        }
    }
}
