package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.*;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;
import uk.ac.ebi.intact.intactApp.internal.model.*;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ImportNetworkTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.GetTermsPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.SearchOptionsPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class StringifyTask extends AbstractTask implements ObservableTask, TaskObserver {
    final IntactManager manager;
    private final Logger logger = Logger.getLogger(CyUserLog.NAME);
    private final Map<String, CyNode> nodeMap;
    @Tunable(description = "Network to set as a STRING network",
            longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
            exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
            context = "nogui", required = true)
    public CyNetwork network = null;
    @Tunable(description = "Column to use for STRING query",
            longDescription = "Select the column to use to query for STRING nodes",
            exampleStringValue = "name",
            context = "gui", required = true)
    public ListSingleSelection<CyColumn> tableColumn = null;
    @Tunable(description = "Column to use for STRING query",
            longDescription = "Select the column to use to query for STRING nodes",
            exampleStringValue = "name",
            context = "nogui", required = true)
    public String column = null;
    @Tunable(description = "Species for the query",
            longDescription = "Species to use for the query",
            exampleStringValue = "name",
            required = true)
    public ListSingleSelection<Species> species;
    @Tunable(description = "Confidence cutoff",
            longDescription = "The confidence score reflects the cumulated evidence that this " +
                    "interaction exists.  Only interactions with scores greater than " +
                    "this cutoff will be returned",
            exampleStringValue = "0.4",
            context = "nogui")

    public BoundedDouble cutoff = new BoundedDouble(0.0, 1.0, 1.0, false, false);
    private IntactNetwork intactNetwork;
    private CyNetwork net;
    private CyNetwork loadedNetwork = null;
    private int additionalNodes = 0;
    private SearchOptionsPanel optionsPanel = null;
    private TaskMonitor monitor;


    public StringifyTask(final IntactManager manager, final CyNetwork net) {
        this.manager = manager;
        this.net = net;
        species = new ListSingleSelection<>(Species.getSpecies());
        species.setSelectedValue(Species.getSpecies("Homo sapiens"));
        if (net != null) {
            List<CyColumn> colList = new ArrayList<>(net.getDefaultNodeTable().getColumns());
            tableColumn = new ListSingleSelection<>(colList);
            tableColumn.setSelectedValue(net.getDefaultNodeTable().getColumn("name"));
        } else {
            tableColumn = null;
        }
        nodeMap = new HashMap<>();
    }

    public StringifyTask(final IntactManager manager, final CyNetwork net, double confidence, Species sp, String nodeColumn) {
        this.manager = manager;
        this.net = net;
        species = new ListSingleSelection<>(Species.getSpecies());
        species.setSelectedValue(sp);
        if (net != null) {
            List<CyColumn> colList = new ArrayList<>(net.getDefaultNodeTable().getColumns());
            tableColumn = new ListSingleSelection<>(colList);
            tableColumn.setSelectedValue(net.getDefaultNodeTable().getColumn(nodeColumn));
        } else {
            tableColumn = null;
        }
        cutoff.setValue(confidence);
        nodeMap = new HashMap<>();
    }

    public void run(TaskMonitor monitor) {
        this.monitor = monitor;
        monitor.setTitle("Stringify network");

        if (network != null) {
            net = network;
            tableColumn = null;
        } else if (net == null) {
            net = manager.getService(CyApplicationManager.class).getCurrentNetwork();
        }

        // Do a little sanity checking
        if (net == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "No network specified");
            return;
        }

        if (ModelUtils.isIntactNetwork(net) && ModelUtils.isCurrentDataVersion(net)) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "Network '" + net + "' is already a STRING network");
            return;
        }

        CyColumn col = null;
        if (tableColumn != null)
            col = tableColumn.getSelectedValue();

        if (tableColumn == null) {
            if (column == null)
                column = "name";

            col = net.getDefaultNodeTable().getColumn(column);
        }

        List<String> stringList = col.getValues(String.class);
        column = col.getName();

        String terms = ModelUtils.listToString(stringList);

        // We want the query with newlines, so we need to convert
        terms = terms.replace(",", "\n");
        // Now, strip off any blank lines
        terms = terms.replaceAll("(?m)^\\s*", "");

        // Get the network
        intactNetwork = new IntactNetwork(manager);
        int taxon = species.getSelectedValue().getTaxId();

        // Are we command or GUI based?
        if (tableColumn != null) {
            optionsPanel = new SearchOptionsPanel(manager);
            optionsPanel.setConfidence((int) (cutoff.getValue() * 100));
            optionsPanel.setAdditionalNodes(additionalNodes);
            optionsPanel.setSpecies(species.getSelectedValue());

            // GUI based
            TaskIterator ti =
                    new TaskIterator(new GetAnnotationsTask(intactNetwork, taxon, terms,
                            Databases.STRING.getAPIName()));
            manager.execute(ti, this);
            return;
        }

        // Get the annotations
        Map<String, List<Annotation>> annotations =
                intactNetwork.getAnnotations(taxon, terms, Databases.STRING.getAPIName(), false);

        if (annotations == null || annotations.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.ERROR,
                    "Query '" + trunc(terms) + "' returned no results");
            throw new RuntimeException("Query '" + trunc(terms) + "' returned no results");
        }

        boolean resolved = intactNetwork.resolveAnnotations();
        if (!resolved) {
            // Resolve the annotations by choosing the first stringID for each
            for (String term : annotations.keySet()) {
                intactNetwork.addResolvedStringID(term, annotations.get(term).get(0).getStringId());
            }
        }

        Map<String, String> queryTermMap = new HashMap<>();
        List<String> stringIds = intactNetwork.combineIds(queryTermMap);
        LoadInteractions load =
                new LoadInteractions(intactNetwork, species.toString(), taxon,
                        (int) (cutoff.getValue() * 100), additionalNodes, stringIds, queryTermMap, "", Databases.STRING.getAPIName());
        manager.execute(new TaskIterator(load), true);
        loadedNetwork = intactNetwork.getNetwork();
        if (loadedNetwork == null) {
            throw new RuntimeException("Query '" + terms + "' returned no results");
        }

        CopyTask copyTask = new CopyTask(manager, column, net, intactNetwork);
        copyTask.run(monitor);
    }

    private String trunc(String str) {
        if (str.length() > 1000)
            return str.substring(0, 1000) + "...";
        return str;
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
            // System.out.println("Calling importNetwork");
            importNetwork(taxon, (int) (cutoff.getValue() * 100), additionalNodes);

            // Creating the copyTask
            CopyTask copyTask = new CopyTask(manager, column, net, intactNetwork);
            copyTask.run(monitor);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JDialog d = new JDialog();
                    d.setTitle("Resolve Ambiguous Terms");
                    d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    // GetTermsPanel panel = new GetTermsPanel(manager, stringNetwork, Databases.STRING.getAPIName(),
                    //                                         getSpecies(), false, getConfidence(), getAdditionalNodes());
                    CopyTask copyTask = new CopyTask(manager, column, net, intactNetwork);
                    GetTermsPanel panel = new GetTermsPanel(manager, intactNetwork,
                            Databases.STRING.getAPIName(), false,
                            optionsPanel, copyTask);
                    panel.createResolutionPanel();
                    d.setContentPane(panel);
                    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    d.pack();
                    d.setVisible(true);
                }
            });
        }

    }

    public String getSpecies() {
        // This will eventually come from the OptionsComponent...
        if (optionsPanel.getSpecies() != null)
            return optionsPanel.getSpecies().toString();
        return "Homo sapiens"; // Homo sapiens
    }

    void importNetwork(int taxon, int confidence, int additionalNodes) {
        Map<String, String> queryTermMap = new HashMap<>();
        List<String> stringIds = intactNetwork.combineIds(queryTermMap);
        TaskFactory factory = new ImportNetworkTaskFactory(intactNetwork, getSpecies(),
                taxon, confidence, additionalNodes, stringIds,
                queryTermMap, Databases.STRING.getAPIName());
        if (optionsPanel.getLoadEnrichment())
            manager.execute(factory.createTaskIterator(), this, true);
        else
            manager.execute(factory.createTaskIterator(), this, true);
        loadedNetwork = intactNetwork.getNetwork();
    }

    @Override
    public void allFinished(FinishStatus finishStatus) {
    }


    @Override
    public <R> R getResults(Class<? extends R> clzz) {
        if (clzz.equals(CyNetwork.class)) {
            return (R) net;
        } else if (clzz.equals(Long.class)) {
            if (net == null)
                return null;
            return (R) net.getSUID();
        } else if (clzz.equals(JSONResult.class)) {
            JSONResult res = () -> {
                if (net == null) return "{}";
                else return "{\"network\": " + net.getSUID() + "}";
            };
            return (R) res;
        } else if (clzz.equals(String.class)) {
            if (net == null)
                return (R) "No network was set";
            String resp = "Set network '"
                    + net.getRow(net).get(CyNetwork.NAME, String.class);
            resp += " as STRING network";
            return (R) resp;
        }
        return null;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class, Long.class, CyNetwork.class);
    }

    private class CopyTask extends AbstractTask {
        String column;
        CyNetwork network;
        IntactNetwork intactNetwork;
        IntactManager manager;

        CopyTask(IntactManager manager, String col, CyNetwork network, IntactNetwork intactNetwork) {
            this.manager = manager;
            this.column = col;
            this.network = network;
            this.intactNetwork = intactNetwork;
        }

        public void run(TaskMonitor monitor) {
            CyNetwork loadedNetwork = intactNetwork.getNetwork();

            // Get all of the nodes in the network
            ModelUtils.createNodeMap(loadedNetwork, nodeMap, ModelUtils.QUERYTERM);

            // TODO: think about that once more
            // we could also check for string network -> !ModelUtils.isStringNetwork(net)
            if (cutoff.getValue() == 1.0)
                ModelUtils.copyEdges(network, loadedNetwork, nodeMap, column);

            ModelUtils.copyNodeAttributes(network, loadedNetwork, nodeMap, column);

            ModelUtils.copyNodePositions(manager, network, loadedNetwork, nodeMap, column);
        }

    }

}
