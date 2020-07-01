package uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

public enum InteractorType {
    BIO_ACTIVE_ENTITY("bioactive entity", "MI_1100", NodeShapeVisualProperty.TRIANGLE),
    PROTEIN("protein", "MI_0326", NodeShapeVisualProperty.ELLIPSE),
    GENE("gene", "MI_0250", NodeShapeVisualProperty.ROUND_RECTANGLE),
    NUCLEIC_ACID("nucleic acid", "", NodeShapeVisualProperty.PARALLELOGRAM),
    DNA("deoxyribonucleic acid", "MI_0319", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE")),
    DNA_S("dna", "", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE")),
    RNA("ribonucleic acid", "MI_0320", NodeShapeVisualProperty.DIAMOND),
    RNA_S("rna", "", NodeShapeVisualProperty.DIAMOND),
    PEPTIDE("peptide", "MI_0327", NodeShapeVisualProperty.ELLIPSE),
    MOLECULE_SET("molecule set", "MI_1304", NodeShapeVisualProperty.OCTAGON),
    COMPLEX("complex", "MI_0314", NodeShapeVisualProperty.HEXAGON);

    public final String name;
    public final String MI_ID;
    public final NodeShape shape;

    InteractorType(String name, String MI_ID, NodeShape shape) {
        this.name = name;
        this.MI_ID = MI_ID;
        this.shape = shape;
    }
}
