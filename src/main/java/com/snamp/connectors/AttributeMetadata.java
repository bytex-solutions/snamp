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
    /**
     * Returns the attribute name.
     * @return The attribute name.
     */
    public String getAttributeName();

    /**
     *
     * @return
     */
    public String getNamespace();
    public boolean canRead();

    /**
     * Determines whether the value of this attribute can be changed.
     * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
     */
    public boolean canWrite();

    /**
     * Returns the mutable set of tags.
     * @return The mutable set of custom tags.
     */
    public Set<Object> tags();

    /**
     * Determines whether the value of the attribute can be cached after first reading
     * and supplied as real attribute value before first write.
     * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
     */
    public boolean cacheable();

    /**
     * Returns the attribute connection options.
     * @return The attribute connection options.
     */
    public AttributeConnectionOptions options();
}
