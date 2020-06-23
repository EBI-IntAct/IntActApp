package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.CollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.app.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;
import static uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils.groupElementsInPanel;

public class NodeFeatures extends AbstractNodeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final List<Feature> features;
    private final boolean showFeatureEdge;
    private final CollapsedEdge summaryEdge;

    public NodeFeatures(Node iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, CollapsedEdge summaryEdge) {
        this(iNode, features, openBrowser, showFeatureEdge, summaryEdge, backgroundColor);
    }

    public NodeFeatures(Node iNode, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, CollapsedEdge summaryEdge, Color background) {
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
        Map<FeatureClassifier.FeatureClass, List<Feature>> classification = FeatureClassifier.classify(features);
        boolean empty = true;
        for (FeatureClassifier.FeatureClass featureClass : FeatureClassifier.root) {
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

    private CollapsablePanel recursivelyBuildFeatures(Map<FeatureClassifier.FeatureClass, List<Feature>> classification, FeatureClassifier.FeatureClass featureClass) {
        if (featureClass instanceof FeatureClassifier.InnerFeatureClass) {
            FeatureClassifier.InnerFeatureClass innerFeatureClass = (FeatureClassifier.InnerFeatureClass) featureClass;
            List<CollapsablePanel> subFeaturePanels = new ArrayList<>();
            for (FeatureClassifier.FeatureClass subFeatureClass : innerFeatureClass.subClasses) {
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

    private CollapsablePanel createFeatureList(FeatureClassifier.FeatureClass featureClass, List<Feature> features) {
        VerticalPanel featureListPanel = new VerticalPanel(getBackground());
        if (showFeatureEdge) {
            groupElementsInPanel(featureListPanel, getBackground(), features, feature -> feature.type,
                    (featureTypePanel, featuresOfType) -> groupElementsInPanel(featureTypePanel, getBackground(), featuresOfType, feature -> feature.name,
                            (featureNamePanel, featuresOfName) -> {
                                for (Feature feature : featuresOfName) {
                                    List<EvidenceEdge> featureEdges = feature.getEdges();
                                    for (EvidenceEdge edge : featureEdges) {
                                        LinePanel line = new LinePanel(getBackground());
                                        if (summaryEdge == null) {
                                            Node otherNode;
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

    public static Map<EvidenceEdge, List<SelectEdgeButton>> edgeToCheckBoxes = new HashMap<>();
    private final Map<SelectEdgeButton, EvidenceEdge> checkBoxes = new HashMap<>();

    private class SelectEdgeButton extends JCheckBox implements ItemListener {
        private boolean silenceListener = false;
        private final EvidenceEdge edge;

        public SelectEdgeButton(EvidenceEdge edge) {
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
            Manager manager = edge.network.getManager();
            NetworkView currentIView = manager.data.getCurrentIntactNetworkView();
            if (currentIView != null && currentIView.getType() == NetworkView.Type.COLLAPSED) {
                manager.utils.execute(new ExpandViewTaskFactory(manager, true).createTaskIterator());
            }

            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            edge.network.getCyNetwork().getRow(edge.edge).set(CyNetwork.SELECTED, selected);

            for (SelectEdgeButton checkBox : edgeToCheckBoxes.get(edge)) {
                checkBox.silenceListener = true;
                checkBox.setSelected(selected);
                checkBox.silenceListener = false;
            }
        }
    }

    public void deleteEdgeSelectionCheckboxes() {
        for (Map.Entry<SelectEdgeButton, EvidenceEdge> entry : checkBoxes.entrySet()) {
            JCheckBox selectCheckBox = entry.getKey();
            EvidenceEdge edge = entry.getValue();
            List<SelectEdgeButton> edgeCheckBoxes = edgeToCheckBoxes.get(edge);
            edgeCheckBoxes.remove(selectCheckBox);
            if (edgeCheckBoxes.isEmpty()) {
                edgeToCheckBoxes.remove(edge);
            }
        }

    }
}
