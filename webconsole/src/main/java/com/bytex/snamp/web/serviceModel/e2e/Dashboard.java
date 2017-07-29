package com.bytex.snamp.web.serviceModel.e2e;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents dashboard with E2E views.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
@JsonTypeName("E2EDashboard")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public final class Dashboard {
    private final List<E2EView> views = new LinkedList<>();

    @JsonProperty("views")
    public List<E2EView> getViews(){
        return views;
    }

    public void setViews(final List<E2EView> value){
        views.clear();
        views.addAll(value);
    }
}
