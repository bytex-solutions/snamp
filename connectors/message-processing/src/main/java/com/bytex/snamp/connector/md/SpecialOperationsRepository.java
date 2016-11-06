package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.NotificationSource;
import com.bytex.snamp.connector.operations.reflection.JavaBeanOperationRepository;
import com.google.common.collect.ImmutableList;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * Represents repository with special operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SpecialOperationsRepository extends JavaBeanOperationRepository {
    private static final ImmutableList<MethodDescriptor> METHODS = interfaceStaticInitialize(() -> {
        final BeanInfo info = Introspector.getBeanInfo(NotificationDispatcher.class, NotificationSource.class);
        return ImmutableList.copyOf(info.getMethodDescriptors());
    });

    private final Logger logger;

    SpecialOperationsRepository(final String resourceName, final NotificationDispatcher dispatcher, final Logger logger) {
        super(resourceName, dispatcher);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected void failedToEnableOperation(final String operationName, final Exception e) {
        failedToEnableOperation(logger, Level.SEVERE, operationName, e);
    }

    @Override
    protected ImmutableList<MethodDescriptor> getMethods() {
        return METHODS;
    }
}
