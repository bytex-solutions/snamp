package com.itworks.snamp.adapters.groovy;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.adapters.PeriodicPassiveChecker;
import com.itworks.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

/**
 * Represents advanced attribute analyser based on periodic checker.
 */
public class PeriodicPassiveAnalyzer<TAccessor extends AttributeAccessor> extends PeriodicPassiveChecker<TAccessor> {
    public static class AttributeValueHandler implements Predicate{
        private final Predicate checker;

        public AttributeValueHandler(final Predicate checker){
            this.checker = Objects.requireNonNull(checker);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final boolean apply(final Object attributeValue) {
            return checker.apply(attributeValue);
        }

        private void onError(final Throwable e) {

        }

        private void onSuccess(final String resourceName,
                               final MBeanAttributeInfo metadata,
                               final Object attributeValue) {

        }
    }

    /**
     * Represents attribute selector.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static class AttributeSelector extends ExpressionBasedDescriptorFilter {
        private final LinkedList<AttributeValueHandler> handlers;

        public AttributeSelector(final String expression) throws InvalidSyntaxException {
            super(expression);
            handlers = new LinkedList<>();
        }

        public final boolean match(final Supplier<? extends MBeanAttributeInfo> accessor) {
            return match(accessor.get());
        }

        protected AttributeValueHandler createValueHandler(final Predicate valueChecker){
            return new AttributeValueHandler(valueChecker);
        }

        public final AttributeValueHandler when(final Predicate valueChecker){
            final AttributeValueHandler result = createValueHandler(valueChecker);
            handlers.add(result);
            return result;
        }

        private void process(final String resourceName,
                             final AttributeAccessor accessor) {
            final Object attributeValue;
            try{
                attributeValue = accessor.getValue();
            } catch (final JMException e){
                for(final AttributeValueHandler handler: handlers)
                    handler.onError(e);
                return;
            }
            for(final AttributeValueHandler handler: handlers)
                if(handler.apply(attributeValue))
                    handler.onSuccess(resourceName, accessor.getMetadata(), attributeValue);
        }
    }

    private final Set<AttributeSelector> groups;

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

    protected AttributeSelector createSelector(final String expression) throws InvalidSyntaxException {
        return new AttributeSelector(expression);
    }

    public final AttributeSelector select(final String expression) throws InvalidSyntaxException {
        final AttributeSelector selector = createSelector(expression);
        groups.add(selector);
        return selector;
    }

    @Override
    public final void processAttribute(final String resourceName, final TAccessor accessor) {
        for (final AttributeSelector group : groups)
            if(group.match(accessor))
                group.process(resourceName, accessor);
    }
}
