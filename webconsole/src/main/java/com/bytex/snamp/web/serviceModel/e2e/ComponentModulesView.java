package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents communications scheme between modules in the component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("componentModules")
public final class ComponentModulesView extends AbstractComponentSpecificView {
    public static final class ViewData extends AdjacencyMatrixWithArrivals{
        private final String componentName;

        private ViewData(final String componentName){
            this.componentName = componentName;
        }

        private void accept(final ComponentVertex source, final ComponentVertex destination) {
            if (destination.getName().equals(componentName) && !hasAdjacency(source, destination)) {
                setAdjacency(source, destination);
                computeArrivals(source);
                computeArrivals(destination);
                for (final ComponentVertex subDestination : destination)
                    accept(destination, subDestination);
            }
        }

        @Override
        public void accept(final ComponentVertex vertex) {
            if (vertex.getName().equals(componentName)) { //analyze child components only for the specified component
                computeArrivals(vertex);
                vertex.forEach(child -> accept(vertex, child));
            }
        }
    }

    @Override
    ViewData createMatrix() {
        return new ViewData(getTargetComponent());
    }
}
