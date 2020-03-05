package uk.ac.ebi.intact.intactApp.internal.utils.styles.FunctionalMapping;

import org.cytoscape.view.vizmap.VisualMappingFunction;

import java.util.function.Function;

public interface FunctionalMapping<K, V> extends VisualMappingFunction<K, V> {

    void setFunction(Function<K, V> function);
    Function<K, V> getFunction();

    public static final String FUNCTIONAL = "Functional Mapping";
}
