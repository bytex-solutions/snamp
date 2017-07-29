package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents definition of E2E View.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(LandscapeView.class),
        @JsonSubTypes.Type(ChildComponentsView.class),
        @JsonSubTypes.Type(ComponentModulesView.class)
})
public abstract class E2EView {
    private final Map<String, Object> preferences;
    private String name;

    E2EView(){
        preferences = new HashMap<>();
        name = "";
    }

    @JsonProperty("preferences")
    public final Map<String, Object> getPreferences() {
        return preferences;
    }

    public final void setPreferences(final Map<String, Object> value) {
        preferences.clear();
        preferences.putAll(value);
    }

    /**
     * Gets name of this view.
     * @return Name of this view.
     */
    @JsonProperty("name")
    public final String getName(){
        return name;
    }

    public final void setName(final String value){
        name = value;
    }

    /**
     * Fills this E2E View from graph provided by topology analyzer.
     *
     * @param analyzer Topology analyzer. Cannot be {@literal null}.
     */
    abstract Object build(final TopologyAnalyzer analyzer);
}