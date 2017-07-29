package com.bytex.snamp.management.http.model;

import com.bytex.snamp.jmx.WellKnownType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.management.openmbean.OpenType;
import java.util.HashMap;
import java.util.Map;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * Represents DTO for feature binding.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public final class FeatureBindingDataObject {
    private final Map<String, String> bindingProperties;
    private final Object mappedType;

    public FeatureBindingDataObject(final FeatureBindingInfo<?> bindingInfo) {
        bindingProperties = new HashMap<>();
        this.mappedType = bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE);

        bindingInfo.getProperties().forEach(property -> {
            switch (property) {
                case FeatureBindingInfo.MAPPED_TYPE:
                    return;
                default:
                    final Object propertyValue = bindingInfo.getProperty(property);
                    if (propertyValue != null)
                        bindingProperties.put(property, String.valueOf(propertyValue));
            }
        });
    }

    @JsonProperty
    public Map<String, String> getBindingProperties(){
        return bindingProperties;
    }

    @JsonProperty
    public String getMappedType() {
        if (mappedType == null)
            return null;
        else if (mappedType instanceof WellKnownType)
            return ((WellKnownType) mappedType).getDisplayName();
        else if (mappedType instanceof OpenType<?>)
            return WellKnownType.getType((OpenType<?>) mappedType).getDisplayName();
        else
            return String.valueOf(mappedType);
    }
}
