package com.bytex.snamp.connector;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;

import javax.annotation.Nonnull;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents repository of features.
 * @param <F> Type of features in repository.
 * @since 2.1
 */
public class FeatureRepository<F extends MBeanFeatureInfo> extends ConcurrentResourceAccessor<Map<String, F>> {
    private static final long serialVersionUID = 3823766811029165472L;

    public FeatureRepository() {
        super(new HashMap<>());
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final int size = getResource().size();
            out.writeInt(size);
            for(final Map.Entry<String, F> entry: getResource().entrySet()){
                out.writeUTF(entry.getKey());
                out.writeObject(entry.getValue());
            }
        } catch (final InterruptedException | TimeoutException e) {
            throw new IOException(e);
        }
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        try (final SafeCloseable ignored = writeLock.acquireLock(null)) {
            for (int size = in.readInt(); size > 0; size--) {
                getResource().put(in.readUTF(), (F) in.readObject());
            }
        } catch (final InterruptedException | TimeoutException e) {
            throw new IOException(e);
        }
    }

    /**
     * Iterates over features in repository.
     * @param action Consumer to be applied for every feature in this repository.
     */
    public void forEachFeature(final Consumer<? super F> action) {
        read(features -> {
            features.values().forEach(action);
            return null;
        });
    }

    protected static <F extends MBeanFeatureInfo> Optional<? extends F> findFeature(@Nonnull final MBeanInfo info,
                                                                                    @Nonnull final Function<? super MBeanInfo, F[]> resolver,
                                                                                    @Nonnull final Predicate<? super F> finder){
        return Arrays.stream(resolver.apply(info))
            .filter(finder)
            .findFirst();
    }
}
