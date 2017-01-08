package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ChartDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    public ChartDataSource() {
        super(Dashboard.class);

    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
