package com.itworks.snamp.adapters;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

/**
 * Represents advanced attribute analyzer based on periodic attribute query.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class PeriodicPassiveAnalyzer<TAccessor extends AttributeAccessor> extends PeriodicPassiveChecker<TAccessor> {
    private interface AttributeStatement{
    }

    public interface AttributeValueHandler<I, E extends Throwable>{
        void handle(final String resourceName,
                    final MBeanAttributeInfo metadata,
                    final I attributeValue) throws E;
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
    public static class CheckAndProcessAttributeStatement implements Predicate, AttributeStatement{
        private final Predicate checker;
        private final WriteOnceRef<AttributeValueHandler<Object, ?>> successHandler;
        private final WriteOnceRef<AttributeValueHandler<Throwable, ?>> errorHandler;

        protected CheckAndProcessAttributeStatement(final Predicate checker){
            this.checker = Objects.requireNonNull(checker);
            this.successHandler = new WriteOnceRef<AttributeValueHandler<Object, ?>>(handlerStub());
            this.errorHandler = new WriteOnceRef<AttributeValueHandler<Throwable, ?>>(PeriodicPassiveAnalyzer.<Throwable, Throwable>handlerStub());
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

        private void onError(final String resourceName,
                             final MBeanAttributeInfo metadata,
                             final Throwable e) {

        }

        private void onSuccess(final String resourceName,
                               final MBeanAttributeInfo metadata,
                               final Object attributeValue) {

        }
    }

    /**
     * Represents attribute selector statement.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static class AttributeSelectStatement extends ExpressionBasedDescriptorFilter implements AttributeStatement {
        private final LinkedList<CheckAndProcessAttributeStatement> handlers;

        protected AttributeSelectStatement(final String expression) throws InvalidSyntaxException {
            super(expression);
            handlers = new LinkedList<>();
        }

        public final boolean match(final Supplier<? extends MBeanAttributeInfo> accessor) {
            return match(accessor.get());
        }

        protected CheckAndProcessAttributeStatement createValueHandler(final Predicate valueChecker){
            return new CheckAndProcessAttributeStatement(valueChecker);
        }

        public final CheckAndProcessAttributeStatement when(final Predicate valueChecker){
            final CheckAndProcessAttributeStatement result = createValueHandler(valueChecker);
            handlers.add(result);
            return result;
        }

        private void process(final String resourceName,
                             final AttributeAccessor accessor) {
            final Object attributeValue;
            try{
                attributeValue = accessor.getValue();
            } catch (final JMException e){
                for(final CheckAndProcessAttributeStatement handler: handlers)
                    handler.onError(resourceName, accessor.getMetadata(), e);
                return;
            }
            for(final CheckAndProcessAttributeStatement handler: handlers)
                if(handler.apply(attributeValue))
                    handler.onSuccess(resourceName, accessor.getMetadata(), attributeValue);
        }
    }

    private final Set<AttributeSelectStatement> groups;

    /**
     * Initializes a new attribute value sender.
     *
     * @param period     Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    public PeriodicPassiveAnalyzer(final TimeSpan period,
                                   final AbstractAttributesModel<TAccessor> attributes) {
        super(period, attributes);
        groups = new LinkedHashSet<>(10);
    }

    protected AttributeSelectStatement createSelector(final String expression) throws InvalidSyntaxException {
        return new AttributeSelectStatement(expression);
    }

    public final AttributeSelectStatement select(final String expression) throws InvalidSyntaxException {
        final AttributeSelectStatement selector = createSelector(expression);
        groups.add(selector);
        return selector;
    }

    @Override
    public final void processAttribute(final String resourceName, final TAccessor accessor) {
        for (final AttributeSelectStatement group : groups)
            if(group.match(accessor))
                group.process(resourceName, accessor);
    }
}
