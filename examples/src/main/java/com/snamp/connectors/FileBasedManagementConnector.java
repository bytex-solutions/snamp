package com.snamp.connectors;

import com.snamp.TimeSpan;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@PluginImplementation
public final class FileBasedManagementConnector extends AbstractManagementConnector {
    public static final String NAME = "jproperties";

    public FileBasedManagementConnector(){
    }

    /**
     * Throws an exception if the connector is not initialized.
     */
    @Override
    protected void verifyInitialization() {

    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param options       Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected GenericAttributeMetadata<?> connectAttributeCore(final String attributeName, final Map<String, String> options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the value of the attribute.
     *
     * @param attribute    The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws java.util.concurrent.TimeoutException
     *
     */
    @Override
    protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sends the attribute value to the remote agent.
     *
     * @param attribute    The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return {@literal true} if attribute value is overridden successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Executes remote action.
     *
     * @param actionName The name of the action,
     * @param args       The invocation arguments.
     * @param timeout    The Invocation timeout.
     * @return The invocation result.
     */
    @Override
    public Object doAction(final String actionName, final Arguments args, final TimeSpan timeout) throws UnsupportedOperationException, TimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p/>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p/>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p/>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p/>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p/>
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p/>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
