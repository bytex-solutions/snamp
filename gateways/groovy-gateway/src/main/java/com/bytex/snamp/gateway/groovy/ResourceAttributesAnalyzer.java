package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.internal.Utils;
import groovy.lang.Closure;
import org.osgi.framework.InvalidSyntaxException;

import javax.annotation.Nonnull;
import javax.management.DescriptorRead;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

/**
 * Represents advanced attribute analyzer based on periodic attribute query.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public class ResourceAttributesAnalyzer<TAccessor extends AttributeAccessor> extends PeriodicPassiveChecker<TAccessor> implements ResourceFeaturesAnalyzer {
    private interface AttributeStatement extends FeatureStatement{
    }

    /**
     * Represents attribute value handler.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static class FilterAndProcessAttributeStatement implements Predicate, AttributeStatement{
        private final Predicate checker;
        private AttributeValueHandler<Object, ?> successHandler;
        private AttributeValueHandler<? super Throwable, ? extends RuntimeException> errorHandler;

        protected FilterAndProcessAttributeStatement(final Predicate checker) {
            this.checker = Objects.requireNonNull(checker);
            successHandler = null;
            errorHandler = null;
        }

        /**
         * Determines whether this handler is applicable to the specified attribute value.
         * @param attributeValue The value of the attribute to check.
         * @return {@literal true}, if this handle is applicable to the specified attribute value; otherwise, {@literal false}.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final boolean test(final Object attributeValue) {
            return checker.test(attributeValue);
        }

        public final FilterAndProcessAttributeStatement failure(@Nonnull final AttributeValueHandler<? super Throwable, ? extends RuntimeException> handler){
            errorHandler = handler;
            return this;
        }

        @SpecialUse(SpecialUse.Case.SCRIPTING)
        public final FilterAndProcessAttributeStatement failure(final Closure<?> handler){
            return failure(Closures.toAttributeHandler(handler));
        }

        public final FilterAndProcessAttributeStatement then(@Nonnull final AttributeValueHandler<Object, ?> handler){
            successHandler = handler;
            return this;
        }

        @SpecialUse(SpecialUse.Case.SCRIPTING)
        public final FilterAndProcessAttributeStatement then(final Closure<?> handler){
            return then(Closures.toAttributeHandler(handler));
        }

        private void onError(final String resourceName,
                             final MBeanAttributeInfo metadata,
                             final Throwable e) {
            if(errorHandler != null)
            errorHandler.handle(resourceName, metadata, e);
        }

        private void onSuccess(final String resourceName,
                               final MBeanAttributeInfo metadata,
                               final Object attributeValue) {
            if (successHandler != null)
                try {
                    successHandler.handle(resourceName, metadata, attributeValue);
                } catch (final Throwable e) {
                    onError(resourceName, metadata, e);
                }
        }
    }

    /**
     * Represents attribute selector statement.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public static class AttributeSelectStatement extends AbstractSelectStatement implements AttributeStatement {
        private final List<FilterAndProcessAttributeStatement> handlers;

        protected AttributeSelectStatement(final String expression) throws InvalidSyntaxException {
            super(expression);
            handlers = new LinkedList<>();
        }

        protected FilterAndProcessAttributeStatement createStatement(final Predicate valueChecker) {
            return new FilterAndProcessAttributeStatement(valueChecker);
        }

        public final FilterAndProcessAttributeStatement when(final Predicate valueChecker) {
            final FilterAndProcessAttributeStatement result = createStatement(valueChecker);
            handlers.add(result);
            return result;
        }

        @SpecialUse(SpecialUse.Case.SCRIPTING)
        public final FilterAndProcessAttributeStatement when(final Closure<Boolean> valueChecker){
            return when(Closures.toPredicate(valueChecker));
        }

        private void process(final String resourceName,
                             final AttributeAccessor accessor) {
            final Object attributeValue;
            try {
                attributeValue = accessor.getValue();
            } catch (final JMException e) {
                for (final FilterAndProcessAttributeStatement handler : handlers)
                    handler.onError(resourceName, accessor.getMetadata(), e);
                return;
            }
            handlers.stream()
                    .filter(handler -> handler.test(attributeValue))
                    .forEach(handler -> handler.onSuccess(resourceName, accessor.getMetadata(), attributeValue));
        }
    }

    private final Set<AttributeSelectStatement> selectionStatements;
    private final ClusterMember clusterMember;

    /**
     * Initializes a new attribute value sender.
     *
     * @param period     Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    public ResourceAttributesAnalyzer(final Duration period,
                                      final ModelOfAttributes<TAccessor> attributes) {
        super(period, attributes);
        selectionStatements = new LinkedHashSet<>(10);
        clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    protected AttributeSelectStatement createSelector(final String expression) throws InvalidSyntaxException {
        return new AttributeSelectStatement(expression);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    @Override
    public final AttributeSelectStatement select(final String expression) throws InvalidSyntaxException {
        final AttributeSelectStatement selector = createSelector(expression);
        selectionStatements.add(selector);
        return selector;
    }

    @Override
    public final boolean accept(final String resourceName, final TAccessor accessor) {
        //abort if passive node
        if (clusterMember.isActive()) {
            selectionStatements.stream()
                    .filter(group -> group.match((DescriptorRead) accessor))
                    .forEach(group -> group.process(resourceName, accessor));
            return true;
        } else return false;
    }
}
