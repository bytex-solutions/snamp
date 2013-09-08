package com.snamp.connectors;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 9/7/13
 * Time: 10:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AttributeMetadata {
    public String getAttributeName();
    public String getNamespace();
    public boolean canRead();
    public boolean canWrite();
    public Set<Object> tags();
    public boolean cacheable();
}
