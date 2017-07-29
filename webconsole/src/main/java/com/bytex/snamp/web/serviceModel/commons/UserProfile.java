package com.bytex.snamp.web.serviceModel.commons;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents a map with user settings.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
@JsonTypeName("userProfile")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public final class UserProfile {
    private String email;

    public UserProfile(){
        email = "";
    }

    @JsonProperty("email")
    public String getEmail(){
        return email;
    }

    public void setEmail(final String value){
        email = nullToEmpty(value);
    }
}
