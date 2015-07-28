package com.bytex.snamp.connectors.attributes;

import javax.management.MBeanAttributeInfo;
import java.io.Serializable;

/**
 * Describes managed resource attribute specifier.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum  AttributeSpecifier implements Serializable {
    NOT_ACCESSIBLE(false, false, false),
    READ_ONLY(false, true, false),
    WRITE_ONLY(true, false, false),
    READ_AS_FLAG_ONLY(false, true, true),
    READ_WRITE(true, true, false),
    READ_AS_FLAG_WRITE(true, true, true);

    private final boolean isWritable;
    private final boolean isReadable;
    private final boolean isFlag;

    AttributeSpecifier(final boolean writable,
                       final boolean readable,
                       final boolean isFlag){
        this.isFlag = isFlag;
        this.isReadable = readable;
        this.isWritable = writable;
    }

    /**
     * Determines whether the attribute can supply value.
     * @return {@literal true}, if attribute is readable; otherwise, {@literal false}.
     */
    public boolean canRead(){
        return isReadable;
    }

    /**
     * Determines whether the attribute can be modified.
     * @return {@literal true}, if attribute can be modified; otherwise, {@literal false}.
     */
    public boolean canWrite(){
        return isWritable;
    }

    /**
     * Determines whether the attribute can supply flag ({@code boolean} value).
     * @return {@literal true}, if attribute is readable as flag; otherwise, {@literal false}.
     */
    public boolean isFlag(){
        return isFlag;
    }

    public AttributeSpecifier writable(final boolean value) {
        if(isWritable == value) return this;
        else for(final AttributeSpecifier spec: values())
            if(spec.isWritable == value &&
                    spec.isReadable == isReadable &&
                    spec.isFlag == isFlag) return spec;
        return NOT_ACCESSIBLE;
    }

    public AttributeSpecifier readable(final boolean value){
        if(isReadable == value) return this;
        else for(final AttributeSpecifier spec: values())
            if(spec.isReadable == value &&
                    spec.isWritable == isWritable &&
                    spec.isFlag == isFlag) return spec;
        return NOT_ACCESSIBLE;
    }

    public AttributeSpecifier flag(final boolean value){
        if(value == isFlag) return this;
        else for(final AttributeSpecifier spec: values())
            if(spec.isReadable == isReadable &&
                    spec.isWritable == isWritable &&
                    spec.isFlag == value) return spec;
        return NOT_ACCESSIBLE;
    }

    /**
     * Gets attribute specifier.
     * @param attributeInfo The attribute metadata.
     * @return The attribute specifier.
     */
    public static AttributeSpecifier get(final MBeanAttributeInfo attributeInfo) {
        for (final AttributeSpecifier spec : values())
            if (spec.isWritable == attributeInfo.isWritable() &&
                    spec.isReadable == attributeInfo.isReadable() &&
                    spec.isFlag == attributeInfo.isIs())
                return spec;
        return NOT_ACCESSIBLE;
    }
}
