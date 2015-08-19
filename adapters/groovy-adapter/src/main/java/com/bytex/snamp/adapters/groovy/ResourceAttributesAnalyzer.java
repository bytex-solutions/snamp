package com.bytex.snamp.adapters.groovy;

import com.google.common.base.Predicate;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.modeling.ModelOfAttributes;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.adapters.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.SpecialUse;
import groovy.lang.Closure;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.DescriptorRead;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.*;

/**
 * Represents advanced attribute analyzer based on periodic attribute query.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class ResourceAttributesAnalyzer<TAccessor extends AttributeAccessor> extends PeriodicPassiveChecker<TAccessor> implements ResourceFeaturesAnalyzer {
    private interface AttributeStatement extends FeatureStatement{
    }

    private static <I, E extends Throwable> AttributeValueHandler<I, E> handlerStub(){
        return new AttributeValueHandler<I, E>() {
            @Override
            public void handle(final String resourceName, final MBeanAttributeInfo metadata, final I attributeValue) {

            }
        };
    }

    /**
     * Represents attribute value handler.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static class FilterAndProcessAttributeStatement implements Predicate, AttributeStatement{
        private final Predicate checker;
        private final WriteOnceRef<AttributeValueHandler<Object, ?>> successHandler;
        private final WriteOnceRef<AttributeValueHandler<? super Throwable, ? extends RuntimeException>> errorHandler;

        protected FilterAndProcessAttributeStatement(final Predicate checker){
            this.checker = Objects.requireNonNull(checker);
            this.successHandler = new WriteOnceRef<AttributeValueHandler<Object, ?>>(handlerStub());
            this.errorHandler = new WriteOnceRef<AttributeValueHandler<? super Throwable, ? extends RuntimeException>>(ResourceAttributesAnalyzer.<Throwable, RuntimeException>handlerStub());
        }

        /**
         * Determines whether this handler is applicable to the specified attribute value.
         * @param attributeValue The value of the attribute to check.
         * @return {@literal true}, if this handle is applicable to the specified attribute value; otherwise, {@literal false}.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final boolean apply(final Object attributeValue) {
            return checker.apply(attributeValue);
        }

        public final FilterAndProcessAttributeStatement failure(final AttributeValueHandler<? super Throwable, ? extends RuntimeException> handler){
            errorHandler.set(Objects.requireNonNull(handler));
            return this;
        }

        @SpecialUse
        public final FilterAndProcessAttributeStatement failure(final Closure<?> handler){
            return failure(Closures.<Throwable, RuntimeException>toAttributeHandler(handler));
        }

        public final FilterAndProcessAttributeStatement then(final AttributeValueHandler<Object, ?> handler){
            successHandler.set(Objects.requireNonNull(handler));
            return this;
        }

        @SpecialUse
        public final FilterAndProcessAttributeStatement then(final Closure<?> handler){
            return then(Closures.toAttributeHandler(handler));
        }

        private void onError(final String resourceName,
                             final MBeanAttributeInfo metadata,
                             final Throwable e) {
            final AttributeValueHandler<? super Throwable, ? extends RuntimeException> handler = errorHandler.get();
            handler.handle(resourceName, metadata, e);
        }

        private void onSuccess(final String resourceName,
                               final MBeanAttributeInfo metadata,
                               final Object attributeValue) {
            final AttributeValueHandler<Object, ?> hander = successHandler.get();
            try {
                hander.handle(resourceName, metadata, attributeValue);
            }catch (final Throwable e){
                onError(resourceName, metadata, e);
            }
        }
    }

    /**
     * Represents attribute selector statement.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
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

        @SpecialUse
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
            for (final FilterAndProcessAttributeStatement handler : handlers)
                if (handler.apply(attributeValue))
                    handler.onSuccess(resourceName, accessor.getMetadata(), attributeValue);
        }
    }

    private final Set<AttributeSelectStatement> selectionStatements;

    /**
     * Initializes a new attribute value sender.
     *
     * @param period     Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    public ResourceAttributesAnalyzer(final TimeSpan period,
                                      final ModelOfAttributes<TAccessor> attributes) {
        super(period, attributes);
        selectionStatements = new LinkedHashSet<>(10);
    }

    protected AttributeSelectStatement createSelector(final String expression) throws InvalidSyntaxException {
        return new AttributeSelectStatement(expression);
    }

    @SpecialUse
    @Override
    public final AttributeSelectStatement select(final String expression) throws InvalidSyntaxException {
        final AttributeSelectStatement selector = createSelector(expression);
        selectionStatements.add(selector);
        return selector;
    }

    @Override
    public final void processAttribute(final String resourceName, final TAccessor accessor) {
        for (final AttributeSelectStatement group : selectionStatements)
            if (group.match((DescriptorRead) accessor))
                group.process(resourceName, accessor);
    }
}
