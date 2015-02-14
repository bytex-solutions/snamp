package com.itworks.jcommands.impl;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.TabularData;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TabularDataExtender implements ModelAdaptor {
    private TabularDataExtender(){

    }

    @Override
    public Object getProperty(final Interpreter interp, final ST self, final Object o, final Object property, final String propertyName) throws STNoSuchPropertyException {
        if(o instanceof TabularData)
            return getProperty(interp, (TabularData)o, propertyName);
        else throw new STNoSuchPropertyException(new InvalidKeyException(), o, propertyName);
    }

    private Object getProperty(final Interpreter interp, final TabularData table, final String propertyName) {
        switch (propertyName){
            case "length": return table.size();
            case "array":
                final CompositeData[] rows = new CompositeData[table.size()];
                int index = 0;
                for(final Object r: table.values())
                    rows[index++] = (CompositeData)r;
                return rows;
            default: throw new STNoSuchPropertyException(new InvalidKeyException(), table, propertyName);
        }
    }

    static void register(final STGroup groupDef) {
        final ModelAdaptor adaptor = new TabularDataExtender();
        groupDef.registerModelAdaptor(TabularData.class, adaptor);
    }
}
