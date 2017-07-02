package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;

import javax.management.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 * Represents a collection of connected attributes.
 */
final class HttpModelOfAttributes extends ModelOfAttributes<HttpAttributeAccessor> implements AttributeSupport {
    private static final int METHOD_NOT_ALLOWED = 405;

    @Override
    public String getAttribute(final String resourceName, final String attributeName) throws WebApplicationException {
        try {
            return Objects.toString(getAttributeValue(resourceName, attributeName));
        } catch (final AttributeNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (final ReflectionException | MBeanException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void setAttribute(final String resourceName, final String attributeName, final String value) throws WebApplicationException {
        try {
            setAttributeValue(resourceName, attributeName, value);
        } catch (final AttributeNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (final AttributeAccessor.InterceptionException e) {
            throw new WebApplicationException(e, METHOD_NOT_ALLOWED);
        } catch (final InvalidAttributeValueException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (final MBeanException | ReflectionException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected HttpAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new HttpAttributeAccessor(metadata);
    }
}
