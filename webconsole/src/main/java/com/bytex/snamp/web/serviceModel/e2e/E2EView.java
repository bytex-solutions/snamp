package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Objects;

/**
 * Represents definition of E2E View.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({@JsonSubTypes.Type(
    ComponentSpecificView.class
)})
public abstract class E2EView {
    private String rootComponent = "";

    E2EView(){

    }

    @JsonProperty("rootComponent")
    public final String getTargetComponent(){
        return rootComponent;
    }

    public final void setTargetComponent(final String value){
        rootComponent = Objects.requireNonNull(value);
    }

    /**
     * Fills this E2E View from graph provided by topology analyzer.
     * @param analyzer Topology analyzer. Cannot be {@literal null}.
     */
    abstract ViewData build(final TopologyAnalyzer analyzer);
}
