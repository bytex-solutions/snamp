package org.stringtemplate.v4.adaptors;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import javax.management.openmbean.CompositeData;

/**
 * @author Roman Sakno
 */
public final class CompositeDataAdaptor implements ModelAdaptor {
    private CompositeDataAdaptor(){

    }

    private static Object getProperty(final CompositeData dictionary,
                                      final String propertyName){
        if(dictionary.containsKey(propertyName))
            return dictionary.get(propertyName);
        else
            throw new STNoSuchPropertyException(new IllegalArgumentException(), dictionary, propertyName);
    }

    @Override
    public Object getProperty(final Interpreter interp, final ST self, final Object o, final Object property, final String propertyName) throws STNoSuchPropertyException {
        if(o instanceof CompositeData)
            return getProperty((CompositeData)o, propertyName);
        else
            throw new ClassCastException(String.format("Cannot cast %s to CompositeData", o));
    }

    public static void register(final STGroup groupDef){
        groupDef.registerModelAdaptor(CompositeData.class, new CompositeDataAdaptor());
    }
}
