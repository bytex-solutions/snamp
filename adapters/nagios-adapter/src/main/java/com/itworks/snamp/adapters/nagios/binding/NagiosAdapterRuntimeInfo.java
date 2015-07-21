package com.itworks.snamp.adapters.nagios.binding;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.AttributesModelReader;
import com.itworks.snamp.adapters.binding.FeatureBindingInfo;
import com.itworks.snamp.adapters.nagios.NagiosAttributeAccessor;
import com.itworks.snamp.internal.RecordReader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NagiosAdapterRuntimeInfo {
    private NagiosAdapterRuntimeInfo(){

    }

    private static Collection<NagiosAttributeBindingInfo> getAttributes(final String servletContext,
                                                                        final AttributesModelReader<NagiosAttributeAccessor> attributes){
        final List<NagiosAttributeBindingInfo> result = new LinkedList<>();
        attributes.forEachAttribute(new RecordReader<String, NagiosAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final NagiosAttributeAccessor accessor) {
                result.add(new NagiosAttributeBindingInfo(servletContext, resourceName, accessor));
                return true;
            }
        });
        return result;
    }

    public static <B extends FeatureBindingInfo> Collection<? extends B> getBindingInfo(final Class<B> bindingType,
                                                                                        final String servletContext,
                                                                                        final AttributesModelReader<NagiosAttributeAccessor> attributes){
        if(bindingType.isAssignableFrom(NagiosAttributeBindingInfo.class))
            return (Collection<B>)getAttributes(servletContext, attributes);
        else return Collections.emptyList();
    }
}
