package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

/**
 * Created by temni on 2/8/2015.
 */
final class GetInstalledConnectorsOperation extends InstalledComponents  {

    private static final String NAME = "getInstalledConnectorsOperation";

    GetInstalledConnectorsOperation(final SnampManager manager) throws OpenDataException{
        super(NAME, manager);
    }

    @Override
    public TabularData getValue() throws OpenDataException{
        final TabularData result = new TabularDataSupport(openType);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            result.put(createRow(component));
        return result;
    }
}
