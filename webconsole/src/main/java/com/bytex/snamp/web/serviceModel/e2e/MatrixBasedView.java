package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.DurationSerializer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.time.Duration;

/**
 * Represents view which data can be constructed as adjacency matrix.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class MatrixBasedView extends E2EView {
    private Duration historyDepth;

    MatrixBasedView(){
        
    }

    @JsonProperty("shelfLife")
    @JsonDeserialize(using = DurationDeserializer.class)
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public final void setShelfLife(final Duration value){
        historyDepth = value;
    }

    @JsonSerialize(using = DurationSerializer.class)
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public final Duration getShelfLife() {
        return historyDepth;
    }

    abstract AdjacencyMatrix createMatrix();

    @Override
    final AdjacencyMatrix build(final TopologyAnalyzer analyzer) {
        final AdjacencyMatrix matrix = createMatrix();
        analyzer.visitVertices(matrix);
        return matrix;
    }
}
