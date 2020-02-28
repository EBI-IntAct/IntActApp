package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.GetEnrichmentTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetStringIDsFromPubmedTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowEnrichmentPanelTaskFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: [Optional] Improve non-gui mode
public class PubMedQueryPanel extends JPanel {
    final IntactManager manager;
    IntactNetwork intactNetwork = null;
    IntactNetwork initialIntactNetwork = null;
    JTextArea pubmedQuery;
    JPanel mainSearchPanel;
    JComboBox<Species> speciesCombo;
    JButton importButton;
    SearchOptionsPanel optionsPanel;
    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat intFormatter = new DecimalFormat("#0");

    private boolean ignore = false;
    private Species species;

    private int confidence = 40;
    private int additionalNodes = 100;

    private boolean loadEnrichment = false;

    public PubMedQueryPanel(final IntactManager manager) {
        super(new GridBagLayout());
        this.manager = manager;
        this.species = null;
        this.confidence = (int) (manager.getDefaultConfidence() * 100);
        this.additionalNodes = manager.getDefaultMaxProteins();
        init();
    }

    public PubMedQueryPanel(final IntactManager manager, IntactNetwork intactNetwork) {
        super(new GridBagLayout());
        this.manager = manager;
        this.intactNetwork = intactNetwork;
        this.initialIntactNetwork = intactNetwork;
        this.species = null;
        this.confidence = (int) (manager.getDefaultConfidence() * 100);
        this.additionalNodes = manager.getDefaultMaxProteins();
        init();
    }

    public PubMedQueryPanel(final IntactManager manager, IntactNetwork intactNetwork, String query,
                            SearchOptionsPanel searchOptions) {
        this(manager, intactNetwork, query,
                searchOptions.getSpecies(),
                searchOptions.getConfidence(), searchOptions.getAdditionalNodes());
        loadEnrichment = searchOptions.getLoadEnrichment();
        optionsPanel.setLoadEnrichment(loadEnrichment);
    }

    public PubMedQueryPanel(final IntactManager manager, IntactNetwork intactNetwork, String query,
                            final Species species, int confidence, int additionalNodes) {
        super(new GridBagLayout());
        this.manager = manager;
        this.intactNetwork = intactNetwork;
        this.initialIntactNetwork = intactNetwork;
        this.species = species;
        this.confidence = confidence;
        this.additionalNodes = additionalNodes;
        init();
        pubmedQuery.setText(query);
    }

    public void doImport() {
        importButton.doClick();
    }

    private void init() {
        // Create the surrounding panel
        setPreferredSize(new Dimension(800, 600));
        EasyGBC c = new EasyGBC();

        // Create the species panel
        List<Species> speciesList = Species.getSpecies();
        if (speciesList == null) {
            try {
                speciesList = Species.readSpecies(manager);
            } catch (Exception e) {
                manager.error("Unable to get species: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        JPanel speciesBox = createSpeciesComboBox(speciesList);
        add(speciesBox, c.expandHoriz().insets(0, 5, 0, 5));

        // Create the search list panel
        mainSearchPanel = createSearchPanel();
        add(mainSearchPanel, c.down().expandBoth().insets(5, 5, 0, 5));

        optionsPanel = new SearchOptionsPanel(manager, true, false, false);
        optionsPanel.setMinimumSize(new Dimension(400, 150));
        optionsPanel.setConfidence(confidence);
        optionsPanel.setAdditionalNodes(additionalNodes);
        add(optionsPanel, c.down().expandHoriz().insets(5, 5, 0, 5));

        // Add Query/Cancel buttons
        JPanel buttonPanel = createControlButtons();
        add(buttonPanel, c.down().expandHoriz().insets(0, 5, 5, 5));
    }

    JPanel createSearchPanel() {
        String ttText = "<html>Enter any PubMed query, but remember to quote multiple-word terms e.g.:" +
                "<dl><dd>\"drug metabolism\"</dd>" +
                "<dd>(\"Science\")[Journal] AND cancer[Title/Abstract]</dd>" +
                "<dd>Ideker[Author]</dd></dl></html>";
        JPanel queryPanel = new JPanel(new GridBagLayout());
        queryPanel.setPreferredSize(new Dimension(600, 300));
        EasyGBC c = new EasyGBC();

        JLabel queryLabel = new JLabel("Pubmed Query:");
        queryLabel.setToolTipText(ttText);

        c.noExpand().anchor("northwest").insets(0, 5, 0, 5);
        queryPanel.add(queryLabel, c);
        pubmedQuery = new JTextArea();
        pubmedQuery.setToolTipText(ttText);
        JScrollPane jsp = new JScrollPane(pubmedQuery);
        c.down().expandBoth().insets(5, 10, 5, 10);
        queryPanel.add(jsp, c);
        return queryPanel;
    }

    JPanel createSpeciesComboBox(List<Species> speciesList) {
        JPanel speciesPanel = new JPanel(new GridBagLayout());
        EasyGBC c = new EasyGBC();
        JLabel speciesLabel = new JLabel("Species:");
        c.noExpand().insets(0, 5, 0, 5);
        speciesPanel.add(speciesLabel, c);
        speciesCombo = new JComboBox<>(speciesList.toArray(new Species[0]));

        if (species == null) {
            speciesCombo.setSelectedItem(manager.getDefaultSpecies());
        } else {
            speciesCombo.setSelectedItem(species);
        }
        JComboBoxDecorator.decorate(speciesCombo, true, true);
        c.right().expandHoriz().insets(0, 5, 0, 5);
        speciesPanel.add(speciesCombo, c);
        return speciesPanel;
    }

    JPanel createControlButtons() {
        JPanel buttonPanel = new JPanel();
        BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(layout);
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        importButton = new JButton(new InitialAction());

        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(importButton);
        return buttonPanel;
    }

    public void cancel() {
        intactNetwork = initialIntactNetwork;
        if (intactNetwork != null) intactNetwork.reset();
        importButton.setEnabled(true);
        importButton.setAction(new InitialAction());
        ((Window) getRootPane().getParent()).dispose();
    }


    class InitialAction extends AbstractAction implements TaskObserver {
        public InitialAction() {
            super("Import");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            loadEnrichment = optionsPanel.getLoadEnrichment();
            // Start our task cascade
            Species species = (Species) speciesCombo.getSelectedItem();
            if (intactNetwork == null)
                intactNetwork = new IntactNetwork(manager);

            int taxon = species.getTaxId();
            String query = pubmedQuery.getText();
            if (query == null || query.length() == 0) {
                JOptionPane.showMessageDialog(null, "No query was entered -- nothing to do",
                        "Nothing entered", JOptionPane.ERROR_MESSAGE);
                return;
            }

            confidence = optionsPanel.getConfidence();
            additionalNodes = optionsPanel.getAdditionalNodes();

            manager.info("Getting pubmed IDs for " + species.getName() + "query: " + query);

            // Launch a task to get the annotations.
            manager.execute(new TaskIterator(new GetStringIDsFromPubmedTask(intactNetwork, species,
                    additionalNodes, confidence, query)), this);
            // cancel();
            ((Window) getRootPane().getParent()).dispose();
        }

        @Override
        public void taskFinished(ObservableTask task) {
        }

        @Override
        public void allFinished(FinishStatus finishStatus) {
            //
            if (loadEnrichment) {
                GetEnrichmentTaskFactory tf = new GetEnrichmentTaskFactory(manager, true);
                ShowEnrichmentPanelTaskFactory showTf = manager.getShowEnrichmentPanelTaskFactory();
                tf.setShowEnrichmentPanelFactory(showTf);
                TunableSetter setter = manager.getService(TunableSetter.class);
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("cutoff", 0.05);
                TaskIterator newIterator =
                        setter.createTaskIterator(tf.createTaskIterator(manager.getCurrentNetwork()), valueMap);
                // System.out.println("stringNetwork network = "+stringNetwork.getNetwork());
                manager.execute(newIterator);
                ((Window) getRootPane().getParent()).dispose();
            }
        }


    }

}
