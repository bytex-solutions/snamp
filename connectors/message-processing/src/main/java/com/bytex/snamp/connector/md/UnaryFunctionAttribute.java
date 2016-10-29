package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents attribute that transforms value of another attribute in read-only manner.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public abstract class UnaryFunctionAttribute extends ProcessingAttribute {
    private static final long serialVersionUID = 3652422235984102814L;
    private final String sourceAttribute;

    protected UnaryFunctionAttribute(final String name,
                           final String sourceAttribute,
                           final OpenType<?> type,
                           final String description,
                           final AttributeDescriptor descriptor) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
        this.sourceAttribute = Objects.requireNonNull(sourceAttribute);
    }

    protected abstract Object getValue(final AttributeAccessor operand) throws Exception;

    @Override
    protected final Object getValue(final AttributeSupport support) throws Exception {
        try(final AttributeAccessor accessor = new AttributeAccessor(sourceAttribute, support)){
            return getValue(accessor);
        }
    }
}
