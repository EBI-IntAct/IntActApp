package uk.ac.ebi.intact.app.internal.tasks.view.parameters;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;

import java.util.*;

public class OrthologyViewParameterTask extends AbstractViewTask {
    private final String DEFAULT_ORTHOLOGY_DB = "panther";
    private final boolean isParameterApplied;
    private NetworkView networkView;

    public OrthologyViewParameterTask(Manager manager, NetworkView networkView, boolean isParameterApplied) {
        super(manager, networkView);
        this.networkView = networkView;
        this.isParameterApplied = isParameterApplied;
    }

    public OrthologyViewParameterTask(Manager manager, boolean currentView) {
        super(manager, currentView);
        this.isParameterApplied = true;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        chooseData();
        manager.data.viewParameterChanged(chosenView);
        if (isParameterApplied) {
            chosenNetwork.collapseGroups(NodeFields.ORTHOLOG_GROUP_ID.name, DEFAULT_ORTHOLOGY_DB);
            applyParameterLayout();
        } else {
            chosenNetwork.expandGroups();
            resetLayout();
        }
    }

    private void applyParameterLayout() {
        CyLayoutAlgorithmManager layoutAlgorithmManager = manager.utils.getService(CyLayoutAlgorithmManager.class);
        CyLayoutAlgorithm layoutAlgorithm = layoutAlgorithmManager.getLayout("cose");

        Set<View<CyNode>> nodeViews = new HashSet<>();
        for (CyNode node : chosenNetwork.getCyNetwork().getNodeList()) {
            View<CyNode> nodeView = cyView.getNodeView(node);
            if (nodeView != null) {
                nodeViews.add(nodeView);
            }
        }

        Object layoutContext = layoutAlgorithm.createLayoutContext();
        TunableSetter tunableSetter = manager.utils.getService(TunableSetter.class);

        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("springStrength", 10);
        layoutArgs.put("gravityStrength", 80);

        tunableSetter.applyTunables(layoutContext, layoutArgs);

        TaskIterator taskIterator = layoutAlgorithm.createTaskIterator(
                cyView,
                layoutContext,
                nodeViews,
                null
        );

        manager.utils.getService(TaskManager.class).execute(taskIterator);
        cyView.updateView();
    }

    private void resetLayout() {
        CyLayoutAlgorithmManager layoutAlgorithmManager = manager.utils.getService(CyLayoutAlgorithmManager.class);
        CyLayoutAlgorithm layoutAlgorithm = layoutAlgorithmManager.getLayout("force-directed-cl");

        Set<View<CyNode>> nodeViews = new HashSet<>();
        for (CyNode node : networkView.getNetwork().getCyNetwork().getNodeList()) {
            View<CyNode> nodeView = networkView.cyView.getNodeView(node);
            if (nodeView != null) {
                nodeViews.add(nodeView);
            }
        }

        Object layoutContext = layoutAlgorithm.createLayoutContext();
        TunableSetter setter = manager.utils.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(layoutContext, layoutArgs);

        TaskIterator taskIterator = layoutAlgorithm.createTaskIterator(
                networkView.cyView,
                layoutContext,
                nodeViews,
                null
        );
        manager.utils.getService(TaskManager.class).execute(taskIterator);
        chosenView.cyView.updateView();
    }
}