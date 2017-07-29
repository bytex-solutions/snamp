package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.web.serviceModel.ObjectMapperSingleton;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.node.ObjectNode;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AttributeChartData implements ChartData, JsonSerializableWithType {
    private final Attribute attribute;
    private final String resourceName;

    protected AttributeChartData(final String resourceName,
                                 final Attribute attribute,
                                 final Class<? extends Chart> chartType) {
        this.attribute = Objects.requireNonNull(attribute);
        if (!chartType.isAnnotationPresent(JsonTypeName.class))
            throw new IllegalArgumentException(String.format("Chart class %s is not annotated with @JsonTypeName", chartType));
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    @JsonIgnore
    public final Attribute getAttribute(){
        return attribute;
    }

    @JsonIgnore
    public final String getResourceName(){
        return resourceName;
    }

    @Override
    public Object getData(final int dimension) {
        switch (dimension) {
            case 0:
                return getResourceName();
            case 1:
                return getAttribute().getValue();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Nonnull
    protected ObjectNode toJsonNode() throws JsonProcessingException {
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        result.put("attributeName", attribute.getName());
        result.put("attributeValue", ObjectMapperSingleton.INSTANCE.valueToTree(attribute.getValue()));
        result.put("resourceName", resourceName);
        return result;
    }

    @Override
    public final void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        toJsonNode().serialize(jgen, provider);
    }

    @Override
    public final void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException {
        toJsonNode().serializeWithType(jgen, provider, typeSer);
    }
}
