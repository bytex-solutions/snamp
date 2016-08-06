package com.bytex.snamp.adapters.nagios;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ExceptionPlaceholder;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AttributeRequestProcessor implements Consumer<NagiosAttributeAccessor>, Acceptor<NagiosAttributeAccessor, ExceptionPlaceholder>, Supplier<NagiosPluginOutput> {
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
