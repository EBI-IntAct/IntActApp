package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.OLSMapper;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.Taxons;
import uk.ac.ebi.intact.intactApp.internal.ui.legend.NodeColorPicker;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class IntactLegendPanel extends AbstractIntactPanel {
    public final static Color tabbedBackground = new Color(229, 229, 229);
    public final static Color transparent = new Color(229, 229, 229, 0);
    private EasyGBC nodeLayoutHelper = new EasyGBC();
    private EasyGBC layoutHelper = new EasyGBC();
    private Map<Long, NodeColorPicker> colorPickers = new HashMap<>();
    private List<Long> predefinedTaxIds = new ArrayList<>();
    private JPanel mainPanel = new JPanel(new GridBagLayout());
    private JPanel nodePanel = new JPanel(new GridBagLayout());
    private JPanel edgePanel = new JPanel(new GridBagLayout());

    private JPanel collaspedEdgePanel = new JPanel(new GridBagLayout());
    private JPanel expandedEdgePanel = new JPanel(new GridBagLayout());
    private JPanel mutationEdgePanel = new JPanel(new GridBagLayout());

    private JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    public IntactLegendPanel(final IntactManager manager) {
        super(manager);
        setBackground(tabbedBackground);
        mainPanel.setBackground(Color.white);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        init();
        filterCurrentLegends();
        revalidate();
        repaint();
    }


    private void init() {
        setLayout(new GridBagLayout());
        createResetStyleButton();

        mainPanel.add(createNodePanel(), layoutHelper.down().anchor("west").expandHoriz());
        mainPanel.add(createEdgePanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(scrollPane, new EasyGBC().down().anchor("west").expandBoth());
    }

    private void createResetStyleButton() {
        JButton resetStylesButton = new JButton("Reset styles");
        resetStylesButton.addActionListener(e -> new Thread(() -> {
            manager.resetStyles();
            OLSMapper.originalKingdomColors.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
            OLSMapper.originalTaxIdToPaint.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
        }).start());
        mainPanel.add(resetStylesButton);
    }

    private JPanel createNodePanel() {
        createLegend(Taxons.getSpecies());
        addSeparator(nodePanel);
        createLegend(List.of(Taxons.CHEMICAL_SYNTHESIS));
        addSeparator(nodePanel);
        createLegend(Taxons.getKingdoms());
        return new CollapsablePanel(iconFont, "Node legends", nodePanel, false, 20);
    }

    private JPanel createEdgePanel() {
        createCollaspedEdgePanel();
        createExpandedEdgePanel();
        createMutationEdgePanel();
        return new CollapsablePanel(iconFont, "Edge legends", edgePanel, false, 20);
    }

    private void createCollaspedEdgePanel() {

    }

    private void createExpandedEdgePanel() {

    }

    private void createMutationEdgePanel() {

    }


    private void addSeparator(JPanel panel) {
        JSeparator separator;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.getPreferredSize().height = 1;
        panel.add(separator, nodeLayoutHelper.down().anchor("west").expandHoriz());
    }

    private void createLegend(List<Taxons> taxons) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(transparent);

        taxons.forEach((taxon) -> {
            Map<Long, Paint> reference = (taxon.isSpecies) ? OLSMapper.taxIdToPaint : OLSMapper.kingdomColors;
            NodeColorPicker nodeColorPicker = new NodeColorPicker(taxon.descriptor, (Color) reference.get(taxon.taxId), taxon.isSpecies);
            nodeColorPicker.addColorChangedListener(e -> {
                manager.updateStylesColorScheme(taxon.taxId, e.newColor);
                reference.put(taxon.taxId, e.newColor);
            });
            if (taxon.isSpecies) {
                predefinedTaxIds.add(taxon.taxId);
            }
            colorPickers.put(taxon.taxId, nodeColorPicker);
            panel.add(nodeColorPicker, nodeLayoutHelper.down().anchor("west").expandHoriz());
        });
        nodePanel.add(panel, nodeLayoutHelper.down().anchor("west").expandHoriz());
    }

    private void filterCurrentLegends() {
        new Thread(() -> {
            Set<Long> networkTaxIds = currentIView.getNetwork().getTaxIds();

            while (OLSMapper.speciesNotReady())
                TimeUtils.sleep(100);

            for (Long taxId : predefinedTaxIds) {
                if (networkTaxIds.contains(taxId) || (OLSMapper.taxIdToChildrenTaxIds.containsKey(taxId) && !Collections.disjoint(networkTaxIds, OLSMapper.taxIdToChildrenTaxIds.get(taxId)))) {
                    colorPickers.get(taxId).setVisible(true);
                } else {
                    colorPickers.get(taxId).setVisible(false);
                }
            }
        }).start();
    }

    @Override
    void doFilter(String type) {
    }

    @Override
    public void networkViewChanged(CyNetworkView view) {
        IntactNetworkView oldIntactNetworkView = currentIView;
        super.networkViewChanged(view);

        filterCurrentLegends();

        if (oldIntactNetworkView == null || oldIntactNetworkView.getType() != manager.getIntactNetworkView(view).getType()) {
            edgePanel.removeAll();
            JPanel newPanel;

            switch (currentIView.getType()) {
                default:
                case COLLAPSED:
                    newPanel = collaspedEdgePanel;
                    break;
                case EXPANDED:
                    newPanel = expandedEdgePanel;
                    break;
                case MUTATION:
                    newPanel = mutationEdgePanel;
                    break;
            }

            edgePanel.add(newPanel);
        }
    }
}

