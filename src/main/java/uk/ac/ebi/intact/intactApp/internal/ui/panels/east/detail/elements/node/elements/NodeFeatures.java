package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier.*;
import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;
import static uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils.groupElementsInPanel;

public class NodeFeatures extends AbstractNodeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final List<Feature> features;
    private final boolean showFeatureEdge;
    private final boolean selectableFeatureEdge;

    public NodeFeatures(IntactNode iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, boolean selectableFeatureEdge) {
        this(iNode, features, openBrowser, showFeatureEdge, selectableFeatureEdge, backgroundColor);
    }

    public NodeFeatures(IntactNode iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, boolean selectableFeatureEdge, Color background) {
        super("Features summary", iNode, openBrowser);
        this.features = features;
        this.showFeatureEdge = showFeatureEdge;
        this.selectableFeatureEdge = selectableFeatureEdge;
        this.setBackground(background);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(backgroundColor);
        executor.execute(this::fillReportedFeatures);
    }

    private void fillReportedFeatures() {
        Map<FeatureClass, List<Feature>> classification = classify(features);
        boolean empty = true;
        for (FeatureClass featureClass : root) {
            CollapsablePanel featurePanel = recursivelyBuildFeatures(classification, featureClass);
            if (featurePanel != null) {
                empty = false;
                content.add(featurePanel);
            }
        }
        if (empty) {
            setVisible(false);
        }

    }

    private CollapsablePanel recursivelyBuildFeatures(Map<FeatureClass, List<Feature>> classification, FeatureClass featureClass) {
        if (featureClass instanceof InnerFeatureClass) {
            InnerFeatureClass innerFeatureClass = (InnerFeatureClass) featureClass;
            List<CollapsablePanel> subFeaturePanels = new ArrayList<>();
            for (FeatureClass subFeatureClass : innerFeatureClass.subClasses) {
                CollapsablePanel subFeaturePanel = recursivelyBuildFeatures(classification, subFeatureClass);
                if (subFeaturePanel != null) {
                    subFeaturePanels.add(subFeaturePanel);
                }
            }
            if (classification.containsKey(innerFeatureClass.nonDefinedLeaf)) {
                subFeaturePanels.add(createFeatureList(innerFeatureClass.nonDefinedLeaf, classification.get(innerFeatureClass.nonDefinedLeaf)));
            }
            if (!subFeaturePanels.isEmpty()) {
                VerticalPanel featureListPanel = new VerticalPanel(getBackground());
                for (CollapsablePanel subFeaturePanel : subFeaturePanels) {
                    featureListPanel.add(subFeaturePanel);
                }
                return new CollapsablePanel(innerFeatureClass.name, featureListPanel, false);
            }
            return null;
        } else if (classification.containsKey(featureClass)) {
            return createFeatureList(featureClass, classification.get(featureClass));
        } else {
            return null;
        }

    }

    private CollapsablePanel createFeatureList(FeatureClass featureClass, List<Feature> features) {
        VerticalPanel featureListPanel = new VerticalPanel(getBackground());
        if (showFeatureEdge) {
            groupElementsInPanel(featureListPanel, getBackground(), features, feature -> feature.type,
                    (featureTypePanel, featuresOfType) -> groupElementsInPanel(featureTypePanel, getBackground(), featuresOfType, feature -> feature.name,
                            (featureNamePanel, featuresOfName) -> {
                                for (Feature feature : featuresOfName) {
                                    LinePanel line = new LinePanel(getBackground());
                                    if (selectableFeatureEdge) {
                                        line.add(createSelectEdgeButton(feature.edge));
                                        IntactNode otherNode = feature.edge.source.equals(feature.node) ? feature.edge.target : feature.edge.source;
                                        line.add(new JLabel("Observed on edge with " + otherNode.name + " (" + feature.edge.ac + ")"));
                                    } else {
                                        line.add(LinkUtils.createIntactEdgeLink(openBrowser, feature.edge));
                                    }
                                    featureNamePanel.add(line);
                                }
                            }
                    )
            );
        } else {
            for (Feature feature : features) {
                LinePanel line = new LinePanel(getBackground());
                line.add(new JLabel(feature.type + " (" + feature.name + ")"));
                line.add(Box.createHorizontalGlue());
                featureListPanel.add(line);
            }
        }

        return new CollapsablePanel(featureClass.name, featureListPanel, true);
    }


    public static Map<IntactEvidenceEdge, List<JCheckBox>> edgeToCheckBoxes = new HashMap<>();
    private final Map<JCheckBox, IntactEvidenceEdge> checkBoxes = new HashMap<>();

    private JCheckBox createSelectEdgeButton(IntactEvidenceEdge edge) {
        JCheckBox selectEdgeCheckBox = new JCheckBox();
        selectEdgeCheckBox.setSelected(edge.edgeRow.get(CyNetwork.SELECTED, Boolean.class));
        selectEdgeCheckBox.setToolTipText("Select edge");
        checkBoxes.put(selectEdgeCheckBox, edge);

        if (!edgeToCheckBoxes.containsKey(edge)) {
            ArrayList<JCheckBox> edgeCheckBoxes = new ArrayList<>();
            edgeCheckBoxes.add(selectEdgeCheckBox);
            edgeToCheckBoxes.put(edge, edgeCheckBoxes);
        } else {
            edgeToCheckBoxes.get(edge).add(selectEdgeCheckBox);
        }

        selectEdgeCheckBox.addItemListener(e -> {
            IntactManager manager = edge.iNetwork.getManager();
            IntactNetworkView currentIView = manager.getCurrentIntactNetworkView();
            if (currentIView != null && currentIView.getType() == IntactNetworkView.Type.COLLAPSED) {
                manager.execute(new ExpandViewTaskFactory(manager).createTaskIterator());
            }

            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            edge.iNetwork.getNetwork().getRow(edge.edge).set(CyNetwork.SELECTED, selected);

            for (JCheckBox checkBox : edgeToCheckBoxes.get(edge)) {
                checkBox.setSelected(selected);
            }

        });
        return selectEdgeCheckBox;
    }

    public void deleteEdgeSelectionCheckboxes() {
        for (Map.Entry<JCheckBox, IntactEvidenceEdge> entry : checkBoxes.entrySet()) {
            JCheckBox selectCheckBox = entry.getKey();
            IntactEvidenceEdge edge = entry.getValue();
            List<JCheckBox> edgeCheckBoxes = edgeToCheckBoxes.get(edge);
            edgeCheckBoxes.remove(selectCheckBox);
            if (edgeCheckBoxes.isEmpty()) {
                edgeToCheckBoxes.remove(edge);
            }
        }

    }
}
