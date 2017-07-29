package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.json.JsonUtils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.management.MBeanAttributeInfo;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class HttpAttributeAccessor extends AttributeAccessor implements HttpAccessor {
    static final String ATTRIBUTE_URL_PARAM = "attributeName";
    static final String ATTRIBUTE_ACCESS_PATH = "/attributes/{" + RESOURCE_URL_PARAM + "}/{" + ATTRIBUTE_URL_PARAM + "}";
    private final ObjectMapper formatter;

    HttpAttributeAccessor(final MBeanAttributeInfo attributeInfo) {
        super(attributeInfo);
        final String dateFormat = HttpGatewayConfigurationDescriptor.parseDateFormatParam(getMetadata().getDescriptor());
        formatter = new ObjectMapper();
        if (!isNullOrEmpty(dateFormat))
            formatter.setDateFormat(new SimpleDateFormat(dateFormat));
        formatter.registerModule(new JsonUtils());
    }

    String getJsonType(){
        return HttpGatewayHelpers.getJsonType(getType());
    }

    @Override
    protected String interceptGet(final Object value) throws InterceptionException {
        try {
            return formatter.writeValueAsString(value);
        } catch (final IOException e) {
            throw new InterceptionException(e);
        }
    }

    @Override
    protected Object interceptSet(final Object value) throws InterceptionException {
        if (getType() != null && value instanceof String)
            try {
                return formatter.readValue((String) value, getType().getJavaType());
            } catch (IOException e) {
                throw new InterceptionException(e);
            }
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
