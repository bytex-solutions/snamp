package org.stringtemplate.v4.adaptors;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.List;

/**
 * Provides model adaptor for type of {@link List}.
 * @author Roman Sakno
 */
public final class ListAdaptor implements ModelAdaptor {
    private ListAdaptor(){

    }

    private static Object getProperty(final Interpreter interpeter, final List<?> o, final String propertyName) {
        return o.get(Integer.parseInt(propertyName));
    }

    @Override
    public Object getProperty(final Interpreter interpeter,
                              final ST self,
                              final Object o, final Object property, final String propertyName) throws STNoSuchPropertyException {
        if(o instanceof List<?>)
            return getProperty(interpeter, (List<?>) o, propertyName);
        else
            throw new ClassCastException(String.format("Cannot cast %s to List", o));
    }

    public static void register(final STGroup groupDef){
        groupDef.registerModelAdaptor(List.class, new ListAdaptor());
    }
}
