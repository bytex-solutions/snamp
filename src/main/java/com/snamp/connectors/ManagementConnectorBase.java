package com.snamp.connectors;

import com.snamp.*;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.*;

/**
 * Represents an abstract class for building custom management connectors.
 * @author roman
 */
public abstract class ManagementConnectorBase implements ManagementConnector {

    /**
     * Represents default implementation of the attribute descriptor.
     */
    protected static abstract class GenericAttributeMetadata implements AttributeMetadata {
        private final String attributeName;
        private final String namespace;

        public GenericAttributeMetadata(final String attributeName, final String namespace){
            if(attributeName == null) throw new IllegalArgumentException("attributeName is null.");
            else if(namespace == null) throw new IllegalArgumentException("namespace is null.");
            this.attributeName = attributeName;
            this.namespace = namespace;
        }

        @Override
        public final String put(String s, String s2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final String remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void putAll(Map<? extends String, ? extends String> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the attribute name.
         * @return The attribute name.
         */
        @Override
        public final String getAttributeName() {
            return attributeName;
        }

        /**
         * By default, returns {@literal true}.
         * @return
         */
        @Override
        public boolean canRead() {
            return true;
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return true;
        }

        /**
         * Determines whether the value of the attribute can be cached after first reading
         * and supplied as real attribute value before first write, return {@literal false} by default.
         *
         * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
         */
        @Override
        public boolean cacheable() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private final ReadWriteLock coordinator; //transaction coordinator
    private final Map<String, AttributeMetadata> attributes;


    /**
     * Initializes a new management connector.
     */
    protected ManagementConnectorBase(){
        this.attributes = new HashMap<>();
        this.coordinator = new ReentrantReadWriteLock();
    }

    /**
     *  Throws an exception if the connector is not initialized.
     */
    protected abstract void verifyInitialization();

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    protected abstract AttributeMetadata connectAttribute(final String attributeName, final Map<String, String> options);

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) {
        verifyInitialization();
        final Lock writeLock =  coordinator.writeLock();
        writeLock.lock();
        try {
            //return existed attribute without exception to increase flexibility of the API
            if(attributes.containsKey(id)) return attributes.get(id);
            final AttributeMetadata attr;
            if((attr = connectAttribute(attributeName, options)) != null)
                attributes.put(id, attr);
            return attr;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the value of the attribute.
     * @param attribute The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws TimeoutException
     */
    protected abstract Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to read attribute from this connector.
     * @param readTimeout The attribute value read operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException{
        final CountdownTimer timer = new CountdownTimer(readTimeout);
        final Lock readLock = coordinator.readLock();
        timer.start();
        if(readTimeout == TimeSpan.infinite) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.duration, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
           return defaultValue;
        }
        timer.stop();
        //read lock is acquired, forces the custom reading operation
        try{
            return getAttributeValue(attributes.get(id), timer.getElapsedTime(), defaultValue);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Reads a set of attributes.
     * @param output The dictionary with set of attribute keys to read and associated default values.
     * @param readTimeout The attribute value read operation timeout.
     * @return The set of attributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        final Lock readLock = coordinator.readLock();
        final CountdownTimer timer = new CountdownTimer(readTimeout);
        timer.start();
        if(readTimeout == TimeSpan.infinite) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.duration, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return new HashSet<>();
        }
        timer.stop();
        //accumulator for really existed attribute IDs
        final Set<String> result = new HashSet<>();
        try{
            final Object missing = new Object(); //this object represents default value for understanding
            //whether the attribute value is unavailable
            for(final String id: output.keySet()){
                timer.start();
                final Object value = getAttributeValue(attributes.get(id), timer.getElapsedTime(), missing);
                if(value != missing) { //attribute value is available
                    result.add(id);
                    output.put(id, value);
                }
                timer.stop();
            }
        }
        finally {
            readLock.unlock();
        }
        return result;
    }

    /**
     * Sends the attribute value to the remote agent.
     * @param attribute The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return
     */
    protected abstract boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value);

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws TimeoutException The attribute value cannot be write in the specified duration.
     */
    @Override
    public final boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException {
        final Lock writeLock = coordinator.writeLock();
        final CountdownTimer timer = new CountdownTimer(writeTimeout);
        timer.start();
        if(writeTimeout == TimeSpan.infinite) writeLock.lock();
        else try {
            if(!writeLock.tryLock(writeTimeout.duration, writeTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return false;
        }
        timer.stop();
        try{
            return attributes.containsKey(id) ? setAttributeValue(attributes.get(id), timer.getElapsedTime(), value) : false;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Writes a set of attributes inside of the transaction.
     * @param values The dictionary of attributes keys and its values.
     * @param writeTimeout
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws TimeoutException
     */
    @Override
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException {
        final Lock writeLock = coordinator.writeLock();
        final CountdownTimer timer = new CountdownTimer(writeTimeout);
        timer.start();
        if(writeTimeout == TimeSpan.infinite) writeLock.lock();
        else try {
            if(!writeLock.tryLock(writeTimeout.duration, writeTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return false;
        }
        timer.stop();
        boolean result = true;
        try{
            final Object missing = new Object(); //this object represents default value for understanding
            //whether the attribute value is unavailable
            for(final Map.Entry<String, Object> entry: values.entrySet()){
                timer.start();
                result &= setAttributeValue(attributes.get(entry.getKey()), timer.getElapsedTime(), entry.getValue());
                timer.stop();
            }
        }
        finally {
            writeLock.unlock();
        }
        return result;
    }

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    protected boolean disconnectAttributeCore(final String id){
        return true;
    }

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public final synchronized boolean disconnectAttribute(final String id) {
        if(attributes.containsKey(id) && disconnectAttributeCore(id)){
            attributes.remove(id);
            return true;
        }
        else return false;
    }

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    @Override
    public final AttributeMetadata getAttributeInfo(String id) {
        final Lock readLock = coordinator.readLock();
        readLock.lock();
        try {
            return attributes.get(id);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Returns an iterator through attribute identifiers.
     * @return An iterator through attribute identifiers.
     */
    @Override
    public final Iterator<String> iterator() {
        return attributes.keySet().iterator();
    }
}
