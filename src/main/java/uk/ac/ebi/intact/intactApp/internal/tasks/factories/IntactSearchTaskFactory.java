package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.Databases;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetAnnotationsTask;
import uk.ac.ebi.intact.intactApp.internal.ui.GetTermsPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.SearchOptionsPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.SearchQueryComponent;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntactSearchTaskFactory extends AbstractNetworkSearchTaskFactory implements TaskObserver {
    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/ICON_PNG/Cropped_Gradient790.png");
    static String INTACT_ID = "uk.ac.ebi.intact";
    static String INTACT_URL = "https://www.ebi.ac.uk/intact/";
    static String INTACT_NAME = "INTACT protein query";
    static String INTACT_DESC = "Search Intact for protein-protein interactions";
    static String INTACT_DESC_LONG = "<html>The protein query retrieves an INTACT network for one or more proteins. <br />" +
            Paths.get("").toAbsolutePath().toString() +
            "</html>";
    private final Logger logger = Logger.getLogger(CyUserLog.NAME);
    IntactManager manager;
    private IntactNetwork intactNetwork = null;
    private SearchOptionsPanel optionsPanel = null;
    private SearchQueryComponent queryComponent = null;

    public IntactSearchTaskFactory(IntactManager manager) {
        super(INTACT_ID, INTACT_NAME, INTACT_DESC, icon, IntactSearchTaskFactory.stringURL());
        this.manager = manager;
    }

    private static URL stringURL() {
        try {
            return new URL(INTACT_URL);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public boolean isReady() {
        return manager.haveURIs() &&
                queryComponent.getQueryText() != null && queryComponent.getQueryText().length() > 0 && getTaxId() != -1;
    }

    public TaskIterator createTaskIterator() {
        String terms = queryComponent.getQueryText();
        if (optionsPanel.getUseSmartDelimiters())
            terms = TextUtils.smartDelimit(terms);


        intactNetwork = new IntactNetwork(manager);
        int taxon = getTaxId();
        return new TaskIterator(new GetAnnotationsTask(intactNetwork, taxon, terms, Databases.STRING.getAPIName()));
    }

    @Override
    public String getName() {
        return INTACT_NAME;
    }

    @Override
    public String getId() {
        return INTACT_ID;
    }

    @Override
    public String getDescription() {
        return INTACT_DESC_LONG;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public URL getWebsite() {
        return IntactSearchTaskFactory.stringURL();
    }

    // Create a JPanel that provides the species, confidence interval, and number of interactions
    // NOTE: we need to use reasonable defaults since it's likely the user won't actually change it...
    @Override
    public JComponent getOptionsComponent() {
        optionsPanel = new SearchOptionsPanel(manager);
        optionsPanel.setUseSmartDelimiters(true);
        return optionsPanel;
    }

    @Override
    public JComponent getQueryComponent() {
        if (queryComponent == null)
            queryComponent = new SearchQueryComponent();
        return queryComponent;
    }

    @Override
    public TaskObserver getTaskObserver() {
        return this;
    }

    public int getTaxId() {
        try {
            if (optionsPanel.getSpecies() != null) {
                return optionsPanel.getSpecies().getTaxId();
            }
            return 9606; // Homo sapiens
        } catch (ClassCastException e) {
            // The user might not have given us a full species name
            String name = optionsPanel.getSpeciesText();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "Unknown species: '" + name + "'",
                            "Unknown species", JOptionPane.ERROR_MESSAGE);
                }
            });
            return -1;
        }
    }

    public String getSpecies() {
        // This will eventually come from the OptionsComponent...
        if (optionsPanel.getSpecies() != null)
            return optionsPanel.getSpecies().toString();
        return "Homo sapiens"; // Homo sapiens
    }

    public int getAdditionalNodes() {
        // This will eventually come from the OptionsComponent...
        return optionsPanel.getAdditionalNodes();
    }

    public int getConfidence() {
        // This will eventually come from the OptionsComponent...
        return optionsPanel.getConfidence();
    }

    @Override
    public void allFinished(FinishStatus finishStatus) {
    }


    @Override
    public void taskFinished(ObservableTask task) {
        if (!(task instanceof GetAnnotationsTask)) {
            return;
        }
        GetAnnotationsTask annTask = (GetAnnotationsTask) task;

        final int taxon = annTask.getTaxon();
        if (intactNetwork.getAnnotations() == null || intactNetwork.getAnnotations().size() == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "Your query returned no results",
                            "No results", JOptionPane.ERROR_MESSAGE);
                }
            });
            return;
        }
        boolean noAmbiguity = intactNetwork.resolveAnnotations();
        if (noAmbiguity) {
            int additionalNodes = getAdditionalNodes();
            // This mimics the String web site behavior
            if (intactNetwork.getResolvedTerms() == 1 && additionalNodes == 0) {
                additionalNodes = 10;
                logger.warn("STRING Protein: Only one protein was selected -- additional interactions set to 10");
            }

            final int addNodes = additionalNodes;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    importNetwork(taxon, getConfidence(), addNodes);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JDialog d = new JDialog();
                    d.setTitle("Resolve Ambiguous Terms");
                    d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    GetTermsPanel panel = new GetTermsPanel(manager, intactNetwork,
                            Databases.STRING.getAPIName(), false, optionsPanel);
                    panel.createResolutionPanel();
                    d.setContentPane(panel);
                    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    d.pack();
                    d.setVisible(true);
                }
            });
        }
    }

    void importNetwork(int taxon, int confidence, int additionalNodes) {
        Map<String, String> queryTermMap = new HashMap<>();
        List<String> stringIds = intactNetwork.combineIds(queryTermMap);
        // System.out.println("Importing "+stringIds);
        TaskFactory factory = new ImportNetworkTaskFactory(intactNetwork, getSpecies(),
                taxon, confidence, additionalNodes, stringIds,
                queryTermMap, Databases.STRING.getAPIName());
        if (optionsPanel.getLoadEnrichment())
            manager.execute(factory.createTaskIterator(), this);
        else
            manager.execute(factory.createTaskIterator());
    }
}
