package uk.ac.ebi.intact.app.internal.model.core.elements;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;

public interface Element {
    boolean isSelected();
    Network getNetwork();
}
