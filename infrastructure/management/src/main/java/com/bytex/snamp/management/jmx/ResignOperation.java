package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.SimpleType;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ResignOperation extends OpenMBean.OpenOperation<Boolean, SimpleType<Boolean>> {
    ResignOperation() {
        super("resign", SimpleType.BOOLEAN);
    }

    @Override
    public Boolean invoke(final Map<String, ?> arguments) {
        return resign(Utils.getBundleContextOfObject(this));
    }

    public static boolean resign(final BundleContext context) {
        return ServiceHolder.tryCreate(context, ClusterMember.class)
                .map(nodeService -> {
                    try {
                        nodeService.get().resign();
                        return true;
                    } catch (final IllegalArgumentException e) {
                        return false;
                    } finally {
                        nodeService.release(context);
                    }
                }).orElse(false);
    }
}
