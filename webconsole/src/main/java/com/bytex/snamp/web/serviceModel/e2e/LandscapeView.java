package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents E2E view of all components in IT landscape.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@JsonTypeName("landscape")
public final class LandscapeView implements MatrixBasedView{
    /**
     * Represents overall landscape of all components in the environment.
     */
    public static final class ViewData extends AdjacencyMatrixWithArrivals {
        private ViewData(){
            
        }

        @Override
        boolean filterRootComponent(final ComponentVertex vertex) {
            return true;
        }

        @Override
        boolean filterChildComponent(final ComponentVertex vertex) {
            return true;
        }
    }

    @Override
    public ViewData build(final TopologyAnalyzer analyzer) {
        final ViewData matrix = new ViewData();
        analyzer.forEach(matrix);
        return matrix;
    }
}
