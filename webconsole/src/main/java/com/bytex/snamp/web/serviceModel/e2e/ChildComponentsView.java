package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.time.Duration;

/**
 * Represents E2E of child components.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("childComponents")
public final class ChildComponentsView extends AbstractComponentSpecificView {
    public static final class ViewData extends AdjacencyMatrixWithArrivals{
        private final String componentName;
        private final Duration shelfLife;

        private ViewData(final String componentName, final Duration shelfLife){
            this.componentName = componentName;
            this.shelfLife = shelfLife;
        }

        private void accept(final ComponentVertex source, final ComponentVertex destination) {
            if (hasAdjacency(source, destination)) return;   //to avoid infinite recursion
            setAdjacency(source, destination);
            computeArrivals(source);
            computeArrivals(destination);
            for (final ComponentVertex subDestination : destination)
                accept(destination, subDestination);
        }

        @Override
        public void accept(final ComponentVertex vertex) {
            if (vertex.getName().equals(componentName) && vertex.checkAge(shelfLife)) { //analyze child components only for the specified component
                computeArrivals(vertex);
                vertex.forEach(child -> accept(vertex, child));
            }
        }
    }

    @Override
    ViewData createMatrix() {
        return new ViewData(getTargetComponent(), getShelfLife());
    }
}
