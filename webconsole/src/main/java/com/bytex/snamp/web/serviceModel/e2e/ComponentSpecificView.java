package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents component-specific E2E view.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@JsonTypeName("componentSpecificView")
public final class ComponentSpecificView extends E2EView implements TopologyAnalyzer.Visitor{


    @Override
    void build(final TopologyAnalyzer analyzer) {
        analyzer.visit(this);
    }

    private void fillView(final ComponentVertex root){

    }

    @Override
    public boolean visit(final ComponentVertex vertex) {
        if (vertex.getName().equals(getTargetComponent())) {
            fillView(vertex);
            return false;   //abort iteration
        } else
            return true;
    }
}
