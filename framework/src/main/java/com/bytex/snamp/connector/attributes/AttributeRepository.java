package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.FeatureRepository;
import com.bytex.snamp.connector.metrics.AttributeMetrics;
import com.bytex.snamp.connector.metrics.AttributeMetricsRecorder;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.*;
import java.beans.IntrospectionException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.callAndWrapException;
import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Represents repository of attributes.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public class AttributeRepository<F extends MBeanAttributeInfo> extends FeatureRepository<F> {
    private static final long serialVersionUID = -4783789186162664986L;

    /**
     * Represents reader of attribute value.
     * @param <F> Type of attributes in repository.
     */
    @FunctionalInterface
    public interface AttributeReader<F extends MBeanAttributeInfo> {
        Object getAttributeValue(final F attribute) throws Exception;

        default Attribute getAttribute(final F attribute) throws Exception {
            return new Attribute(attribute.getName(), getAttributeValue(attribute));
        }
    }

    /**
     * Represents writer of attribute value.
     * @param <F> Type of attributes in repository.
     */
    @FunctionalInterface
    public interface AttributeWriter<F extends MBeanAttributeInfo>{
        void setAttributeValue(final F attribute, final Object value) throws Exception;
    }

    private final AttributeMetricsRecorder recorder;
    /**
     * Represents metrics associated with this repository.
     */
    public transient final AttributeMetrics metrics;

    public AttributeRepository(){
        metrics = recorder = new AttributeMetricsRecorder();
    }

    /**
     * Gets attribute value.
     * @param attributeName Name of attribute to read.
     * @param reader Attribute value reader.
     * @return Attribute value.
     * @throws AttributeNotFoundException Attribute doesn't exist
     * @throws MBeanException Attribute reader throws exception.
     * @throws ReflectionException Attribute reader throws reflection-related exception.
     */
    public final Object getAttribute(final String attributeName, @Nonnull final AttributeReader<? super F> reader) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final F attribute = getResource().get(attributeName);
            if (attribute != null)
                return reader.getAttribute(attribute);
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            recorder.updateReads();
        }
        throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    public final void setAttribute(@Nonnull final Attribute attribute, @Nonnull final AttributeWriter<? super F> writer) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final F metadata = getResource().get(attribute.getName());
            if (metadata != null)
                writer.setAttributeValue(metadata, attribute.getValue());
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final MBeanException | ReflectionException | InvalidAttributeValueException e) {
            throw e;
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            recorder.updateWrites();
        }
        throw JMExceptionUtils.attributeNotFound(attribute.getName());
    }

    public final AttributeList getAttributes(@Nonnull final AttributeReader<? super F> reader) throws MBeanException, ReflectionException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            if (getResource().isEmpty())
                return new AttributeList();
            final AttributeList result = new AttributeList();
            for (final Map.Entry<String, F> entry : getResource().entrySet()) {
                result.add(new Attribute(entry.getKey(), reader.getAttribute(entry.getValue())));
                recorder.updateReads();
            }
            return result;
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
    }

    private static <F extends MBeanAttributeInfo> Callable<Attribute> createReadTask(final F metadata,
                                                                                     final AttributeReader<? super F> reader,
                                                                                     final AttributeMetricsRecorder recorder) {
        return () -> {
            final Attribute result = reader.getAttribute(metadata);
            recorder.updateReads();
            return result;
        };
    }

    public final AttributeList getAttributes(@Nonnull final AttributeReader<? super F> reader, @Nonnull final ExecutorService executor, @Nullable final Duration timeout) throws MBeanException, ReflectionException {
        final Collection<Future<Attribute>> completedTasks;
        try (final SafeCloseable ignored = readLock.acquireLock(timeout)) {
            switch (getResource().size()) {
                case 0:
                    return new AttributeList();
                case 1:
                    final F attribute = Iterables.getFirst(getResource().values(), null);
                    assert attribute != null;
                    completedTasks = Collections.singletonList(immediateFuture(reader.getAttribute(attribute)));
                    recorder.updateReads();
                    break;
                default:
                    final List<Callable<Attribute>> tasks = getResource()
                            .values()
                            .stream()
                            .map(metadata -> createReadTask(metadata, reader, recorder))
                            .collect(Collectors.toList());
                    completedTasks = timeout == null ?
                            executor.invokeAll(tasks) :
                            executor.invokeAll(tasks, timeout.toMillis(), TimeUnit.MILLISECONDS);
                    tasks.clear();
            }
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
        final AttributeList result = new AttributeList(completedTasks.size());
        for (final Future<Attribute> task : completedTasks)
            if (task.isDone())
                result.add(callAndWrapException(task::get, MBeanException::new));
        return result;
    }

    public static Optional<? extends MBeanAttributeInfo> findAttribute(final String attributeName, final MBeanInfo info) {
        return findFeature(info, MBeanInfo::getAttributes, attribute -> Objects.equals(attribute.getName(), attributeName));
    }
}
