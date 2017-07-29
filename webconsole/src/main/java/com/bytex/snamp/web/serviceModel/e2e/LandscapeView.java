package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.time.Duration;

/**
 * Represents E2E view of all components in IT landscape.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
@JsonTypeName("landscape")
public final class LandscapeView extends MatrixBasedView{
    /**
     * Represents overall landscape of all components in the environment.
     */
    public static final class ViewData extends AdjacencyMatrixWithArrivals {
        private final Duration shelfTime;

        private ViewData(final Duration shelfTime) {
            this.shelfTime = shelfTime;
        }

        @Override
        public void accept(final ComponentVertex vertex) {
            if(vertex.checkAge(shelfTime)) {
                computeArrivals(vertex);
                vertex.forEach(child -> setAdjacency(vertex, child));
            }
        }
    }

    @Override
    ViewData createMatrix() {
        return new ViewData(getShelfLife());
    }
}
