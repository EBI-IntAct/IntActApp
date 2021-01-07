package uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

public enum InteractorType {
    BIO_ACTIVE_ENTITY("bioactive entity", "MI_1100", NodeShapeVisualProperty.TRIANGLE, true, true, null),
    PROTEIN("protein", "MI_0326", NodeShapeVisualProperty.ELLIPSE, true, true, null),
    GENE("gene", "MI_0250", NodeShapeVisualProperty.ROUND_RECTANGLE, true, true, null),
    NUCLEIC_ACID("nucleic acid", "MI_0318", NodeShapeVisualProperty.PARALLELOGRAM, false, true, null),
    DNA("deoxyribonucleic acid", "MI_0319", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"), true, true, NUCLEIC_ACID),
    DNA_S("dna", "", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"), false, false, NUCLEIC_ACID),
    RNA("ribonucleic acid", "MI_0320", NodeShapeVisualProperty.DIAMOND, true, true, NUCLEIC_ACID),
    RNA_S("rna", "", NodeShapeVisualProperty.DIAMOND, false, false, NUCLEIC_ACID),
    PEPTIDE("peptide", "MI_0327", NodeShapeVisualProperty.ELLIPSE, false, true, null),
    MOLECULE_SET("molecule set", "MI_1304", NodeShapeVisualProperty.OCTAGON, true, true, null),
    COMPLEX("complex", "MI_0314", NodeShapeVisualProperty.HEXAGON, true, true, null);

    public final String name;
    public final String MI_ID;
    public final NodeShape shape;
    public final boolean queryChildren;
    public final boolean displayInLegend;
    public final InteractorType parent;

    InteractorType(String name, String MI_ID, NodeShape shape, boolean queryChildren, boolean displayInLegend, InteractorType parent) {
        this.name = name;
        this.MI_ID = MI_ID;
        this.shape = shape;
        this.queryChildren = queryChildren;
        this.displayInLegend = displayInLegend;
        this.parent = parent;
    }
}
