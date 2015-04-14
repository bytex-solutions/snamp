package com.itworks.snamp.adapters.nagios;

import com.google.common.base.Supplier;
import com.itworks.snamp.SafeConsumer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttributeRequestProcessor implements SafeConsumer<NagiosAttributeAccessor>, Supplier<NagiosPluginOutput> {
    private NagiosPluginOutput output;

    @Override
    public void accept(final NagiosAttributeAccessor accessor) {
        this.output = accessor.toNagiosOutput();
    }

    @Override
    public NagiosPluginOutput get() {
        return output;
    }
}
