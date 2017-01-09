package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class HttpAttributeAccessor extends AttributeAccessor implements HttpAccessor {
    static final String ATTRIBUTE_URL_PARAM = "attributeName";
    static final String ATTRIBUTE_ACCESS_PATH = "/attributes/{" + RESOURCE_URL_PARAM + "}/{" + ATTRIBUTE_URL_PARAM + "}";
    private final Gson formatter;

    HttpAttributeAccessor(final MBeanAttributeInfo attributeInfo) {
        super(attributeInfo);
        final String dateFormat = HttpGatewayConfigurationDescriptor.parseDateFormatParam(getMetadata().getDescriptor());
        GsonBuilder builder = new GsonBuilder();
        if (dateFormat != null && dateFormat.length() > 0)
            builder = builder.setDateFormat(dateFormat);
        builder = JsonUtils.registerBufferAdapters(builder);
        builder = JsonUtils.registerOpenTypeAdapters(builder);
        formatter = builder
                .serializeSpecialFloatingPointValues()
                .serializeNulls().create();
    }

    String getJsonType(){
        return HttpGatewayHelpers.getJsonType(getType());
    }

    @Override
    protected String interceptGet(final Object value) {
        return formatter.toJson(value);
    }

    @Override
    protected Object interceptSet(final Object value) throws InterceptionException {
        if (getType() != null && value instanceof String)
            return formatter.fromJson((String) value, getType().getJavaType());
        else throw new InterceptionException(new IllegalArgumentException("String expected"));
    }

    @Override
    public String getPath(final String servletContext,
                          final String resourceName) {
        return servletContext + ATTRIBUTE_ACCESS_PATH
                .replace("{" + RESOURCE_URL_PARAM + "}", resourceName)
                .replace("{" + ATTRIBUTE_URL_PARAM + "}", getName());
    }
}
