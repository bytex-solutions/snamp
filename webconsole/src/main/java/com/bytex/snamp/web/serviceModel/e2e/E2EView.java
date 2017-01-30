package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Represents definition of E2E View.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({@JsonSubTypes.Type(
    LandscapeView.class
)})
public interface E2EView {
    /**
     * Fills this E2E View from graph provided by topology analyzer.
     *
     * @param analyzer Topology analyzer. Cannot be {@literal null}.
     */
    Object build(final TopologyAnalyzer analyzer);
}
