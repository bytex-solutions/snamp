package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.json.ThreadLocalJsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.node.BaseJsonNode;
import org.codehaus.jackson.node.ObjectNode;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AttributeChartData implements ChartData, JsonSerializableWithType {
    private final Attribute attribute;
    private final Class<? extends Chart> chartType;
    private final String instanceName;

    protected AttributeChartData(final String instanceName,
                                 final Attribute attribute,
                                 final Class<? extends Chart> chartType) {
        this.attribute = Objects.requireNonNull(attribute);
        this.chartType = Objects.requireNonNull(chartType);
        if (!chartType.isAnnotationPresent(JsonTypeName.class))
            throw new IllegalArgumentException(String.format("Chart class %s is not annotated with @JsonTypeName", chartType));
        this.instanceName = Objects.requireNonNull(instanceName);
    }

    @JsonIgnore
    public final Class<? extends Chart> getChartType(){
        return chartType;
    }

    @JsonIgnore
    public final Attribute getAttribute(){
        return attribute;
    }

    @JsonIgnore
    public final String getInstanceName(){
        return instanceName;
    }

    @Nonnull
    protected BaseJsonNode toJsonNode() throws JsonProcessingException {
        final ObjectNode result = ThreadLocalJsonFactory.getFactory().objectNode();
        final JsonTypeName typeName = chartType.getAnnotation(JsonTypeName.class);
        assert typeName != null;    //checked in constructor
        result.put("chartType", typeName.value());
        result.put("attributeName", attribute.getName());
        result.put("attributeValue", ObjectMapperSingleton.INSTANCE.valueToTree(attribute.getValue()));
        result.put("instanceName", instanceName);
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
