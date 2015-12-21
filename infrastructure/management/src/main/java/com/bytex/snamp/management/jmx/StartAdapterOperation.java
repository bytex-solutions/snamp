package com.bytex.snamp.management.jmx;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.jmx.OpenMBean;
import org.osgi.framework.BundleException;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Start adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StartAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final String NAME = "startAdapter";

    /**
     * Instantiates a new Start adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartAdapterOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, ADAPTER_NAME_PARAM);
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws BundleException {
        ResourceAdapterActivator.startResourceAdapter(getBundleContextOfObject(this), ADAPTER_NAME_PARAM.getArgument(arguments));
        return null;
    }
}