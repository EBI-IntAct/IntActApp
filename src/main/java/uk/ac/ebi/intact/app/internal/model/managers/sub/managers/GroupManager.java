package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {
    private final Manager manager;
    private final Map<CyNetwork, List<CyGroup>> groups = new HashMap<>();

    public GroupManager(Manager manager) {
        this.manager = manager;
    }

    public void group(Collection<Node> nodes)
}
