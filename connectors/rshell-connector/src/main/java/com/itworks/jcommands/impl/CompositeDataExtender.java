package com.itworks.jcommands.impl;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeDataExtender implements ModelAdaptor {
    private CompositeDataExtender(){

    }

    private static Object getProperty(final Interpreter interpreter,
                                      final CompositeData dictionary,
                                      final String propertyName){
        if(dictionary.containsKey(propertyName))
            return dictionary.get(propertyName);
        else throw new STNoSuchPropertyException(new InvalidKeyException(), dictionary, propertyName);
    }

    @Override
    public Object getProperty(final Interpreter interp, final ST self, final Object o, final Object property, final String propertyName) throws STNoSuchPropertyException {
        if(o instanceof CompositeData)
            return getProperty(interp, (CompositeData)o, propertyName);
        else throw new STNoSuchPropertyException(new InvalidKeyException(), o, propertyName);
    }

    static void register(final STGroup groupDef){
        final ModelAdaptor adaptor = new CompositeDataExtender();
        groupDef.registerModelAdaptor(CompositeData.class, adaptor);
    }
}
