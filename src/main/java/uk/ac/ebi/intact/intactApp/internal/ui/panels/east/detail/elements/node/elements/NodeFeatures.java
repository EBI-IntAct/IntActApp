package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.tasks.view.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier.*;
import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;
import static uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils.groupElementsInPanel;

public class NodeFeatures extends AbstractNodeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final List<Feature> features;
    private final boolean showFeatureEdge;
    private final IntactCollapsedEdge summaryEdge;

    public NodeFeatures(IntactNode iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, IntactCollapsedEdge summaryEdge) {
        this(iNode, features, openBrowser, showFeatureEdge, summaryEdge, backgroundColor);
    }

    public NodeFeatures(IntactNode iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, IntactCollapsedEdge summaryEdge, Color background) {
        super("Features summary", iNode, openBrowser);
        this.features = features;
        this.showFeatureEdge = showFeatureEdge;
        this.summaryEdge = summaryEdge;
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
                                    List<IntactEvidenceEdge> featureEdges = feature.getEdges();
                                    for (IntactEvidenceEdge edge : featureEdges) {
                                        LinePanel line = new LinePanel(getBackground());
                                        if (summaryEdge == null) {
                                            IntactNode otherNode;
                                            if (iNode.equals(edge.source)) {
                                                otherNode = edge.target;
                                            } else if (iNode.equals(edge.target)) {
                                                otherNode = edge.source;
                                            } else {
                                                continue;
                                            }
                                            line.add(new SelectEdgeButton(edge));
                                            line.add(new JLabel("Observed on edge with " + otherNode.name + " (" + edge.ac + ")"));
                                        } else {
                                            if (summaryEdge.subEdgeSUIDs.contains(edge.edge.getSUID())) {
                                                line.add(LinkUtils.createIntactEdgeLink(openBrowser, edge));
                                            }
                                        }
                                        featureNamePanel.add(line);
                                    }
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

    public static Map<IntactEvidenceEdge, List<SelectEdgeButton>> edgeToCheckBoxes = new HashMap<>();
    private final Map<SelectEdgeButton, IntactEvidenceEdge> checkBoxes = new HashMap<>();

    private class SelectEdgeButton extends JCheckBox implements ItemListener {
        private boolean silenceListener = false;
        private final IntactEvidenceEdge edge;

        public SelectEdgeButton(IntactEvidenceEdge edge) {
            this.edge = edge;
            setSelected(edge.edgeRow.get(CyNetwork.SELECTED, Boolean.class));
            setToolTipText("Select edge");
            checkBoxes.put(this, edge);
            CollectionUtils.addToGroups(edgeToCheckBoxes, this, selectEdgeButton -> edge);
            addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (silenceListener) return;
            IntactManager manager = edge.iNetwork.getManager();
            IntactNetworkView currentIView = manager.data.getCurrentIntactNetworkView();
            if (currentIView != null && currentIView.type == IntactNetworkView.Type.COLLAPSED) {
                manager.utils.execute(new ExpandViewTaskFactory(manager, true).createTaskIterator());
            }

            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            edge.iNetwork.getNetwork().getRow(edge.edge).set(CyNetwork.SELECTED, selected);

            for (SelectEdgeButton checkBox : edgeToCheckBoxes.get(edge)) {
                checkBox.silenceListener = true;
                checkBox.setSelected(selected);
                checkBox.silenceListener = false;
            }
        }
    }

    public void deleteEdgeSelectionCheckboxes() {
        for (Map.Entry<SelectEdgeButton, IntactEvidenceEdge> entry : checkBoxes.entrySet()) {
            JCheckBox selectCheckBox = entry.getKey();
            IntactEvidenceEdge edge = entry.getValue();
            List<SelectEdgeButton> edgeCheckBoxes = edgeToCheckBoxes.get(edge);
            edgeCheckBoxes.remove(selectCheckBox);
            if (edgeCheckBoxes.isEmpty()) {
                edgeToCheckBoxes.remove(edge);
            }
        }

    }
}
