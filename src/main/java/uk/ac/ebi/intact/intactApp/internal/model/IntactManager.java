package uk.ac.ebi.intact.intactApp.internal.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.log4j.Logger;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.ExpandedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowResultsPanelTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.DetailPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;

// import org.jcolorbrewer.ColorBrewer;

public class IntactManager implements NetworkAddedListener, SessionLoadedListener, NetworkAboutToBeDestroyedListener, NetworkViewAboutToBeDestroyedListener {
    public static String CONFIGURI = "https://jensenlab.org/assets/stringapp/";
    public static String STRINGResolveURI = "http://version11.string-db.org/api/";
    public static String STITCHResolveURI = "http://stitch.embl.de/api/";
    public static String VIRUSESResolveURI = "http://viruses.string-db.org/cgi/webservice_handler.pl";
    //public static String STITCHResolveURI = "http://beta.stitch-db.org/api/";
    public static String URI = "https://api11.jensenlab.org/";
    public static String DATAVERSION = "11";
    public static String OLD_DATAVERSION = "10";
    public static String alternativeAPIProperty = "alternativeAPI";
    public static String alternativeCONFIGURIProperty = "alternativeCONFIGURI";
    public static String alternativeCONFIGURI = "";
    public static String CallerIdentity = "string_app";
    public static String APIVERSION = "String-api-version";
    public static String RESULT = "QueryResult";
    public static String STRINGDevelopmentURI = "http://string-gamma.org/api/";
    public static boolean enableViruses = true;
    public static boolean useSTRINGDevelopmentVersion = false;
    public static String ShowStructureImages = "showStructureImages";
    public static String ShowEnhancedLabels = "showEnhancedLabels";
    public static String ShowGlassBallEffect = "showGlassBallEffect";
    public static String ShowStringColors = "showStringColors";
    public static String ShowSingletons = "showSingletons";
    public static String HighlightNeighbors = "highlightNeighbors";
    public static String[] channels = {"databases", "experiments", "neighborhood", "fusion",
            "cooccurrence", "textmining", // Lime green
            "coexpression",
            "similarity" // Lila
    };
    final CyServiceRegistrar registrar;
    final CyEventHelper cyEventHelper;
    final Logger logger = Logger.getLogger(CyUserLog.NAME);
    final TaskManager<?, ?> dialogTaskManager;
    final SynchronousTaskManager<?> synchronousTaskManager;
    final CommandExecutorTaskFactory commandExecutorTaskFactory;
    final AvailableCommands availableCommands;
    private final CyRootNetworkManager rootNetworkManager;
    private final List<IntactNetworkCreatedListener> intactNetworkCreatedListeners = new ArrayList<>();
    private final List<IntactViewTypeChangedListener> intactViewTypeChangedListeners = new ArrayList<>();

    // These are various default values that are saved and restored from
    // the network table
    private ShowResultsPanelTaskFactory resultsPanelTaskFactory;
    private Boolean haveChemViz = null;
    private Boolean haveCyBrowser = null;
    private boolean haveURIs = false;
    private final Map<CyNetwork, IntactNetwork> intactNetworkMap;
    private final Map<CyNetworkView, IntactNetworkView> intactNetworkViewMap;
    private DetailPanel cytoPanel = null;
    // Settings default values.  Network specific values are stored in StringNetwork
    private boolean showImage = true;
    private boolean showEnhancedLabels = true;
    private boolean highlightNeighbors = false;
    private Species species;
    private double defaultConfidence = 0.40;
    private int additionalProteins = 0;
    private int maximumProteins = 100;
    private final Palette brewerPalette;
    private final List<EnrichmentTerm.TermCategory> categoryFilter = EnrichmentTerm.TermCategory.getValues();
    private final ChartType chartType = ChartType.SPLIT;
    private Map<String, Color> channelColors;
    private CyProperty<Properties> sessionProperties;
    private CyProperty<Properties> configProps;


    private static final Map<IntactNetworkView.Type, IntactStyle> intactStyles = new HashMap<>();


    private boolean ignore = false;

    public IntactManager(CyServiceRegistrar registrar) {
        this.registrar = registrar;
        // Get our task managers
        dialogTaskManager = registrar.getService(TaskManager.class);
        synchronousTaskManager = registrar.getService(SynchronousTaskManager.class);
        availableCommands = registrar.getService(AvailableCommands.class);
        commandExecutorTaskFactory = registrar.getService(CommandExecutorTaskFactory.class);
        cyEventHelper = registrar.getService(CyEventHelper.class);
        intactNetworkMap = new HashMap<>();
        intactNetworkViewMap = new HashMap<>();

        setupStyles();
        FeatureClassifier.initMIIdSets();

        if (!haveEnhancedGraphics())
            showEnhancedLabels = false;

        PaletteProviderManager pm = registrar.getService(PaletteProviderManager.class);
        PaletteProvider brewerProvider = pm.getPaletteProvider("ColorBrewer");
        brewerPalette = brewerProvider.getPalette("Paired colors");

        // Make sure we've read in our species
        if (Species.getSpecies() == null) {
            try {
                Species.readSpecies(this);
            } catch (Exception e) {
                throw new RuntimeException("Can't read species information");
            }
        }

        channelColors = new HashMap<>();
        // Set up our default channel colors
        channelColors.put("databases", Color.CYAN);
        channelColors.put("experiments", Color.MAGENTA);
        channelColors.put("neighborhood", Color.GREEN);
        channelColors.put("fusion", Color.RED);
        channelColors.put("cooccurrence", Color.BLUE);
        channelColors.put("textmining", new Color(199, 234, 70)); // Lime green
        channelColors.put("coexpression", Color.BLACK);
        channelColors.put("similarity", new Color(163, 161, 255)); // Lila

        // Get our default settings
        configProps = ModelUtils.getPropertyService(this, SavePolicy.CONFIG_DIR);

        // check for an alternative config URI
        if (ModelUtils.hasProperty(configProps, alternativeCONFIGURIProperty)) {
            alternativeCONFIGURI = ModelUtils.getStringProperty(configProps,
                    alternativeCONFIGURIProperty);
        } else {
            ModelUtils.setStringProperty(configProps, alternativeCONFIGURIProperty, alternativeCONFIGURI);
        }

        // set all stringApp default proerties
        if (ModelUtils.hasProperty(configProps, ShowStructureImages)) {
            setShowImage(ModelUtils.getBooleanProperty(configProps, ShowStructureImages));
        }
        if (ModelUtils.hasProperty(configProps, ShowEnhancedLabels)) {
            setShowEnhancedLabels(ModelUtils.getBooleanProperty(configProps, ShowEnhancedLabels));
        }
        if (ModelUtils.hasProperty(configProps, HighlightNeighbors)) {
            setHighlightNeighbors(ModelUtils.getBooleanProperty(configProps, HighlightNeighbors));
        }


        if (ModelUtils.hasProperty(configProps, "species")) {
            setDefaultSpecies(ModelUtils.getStringProperty(configProps, "species"));
        }
        if (ModelUtils.hasProperty(configProps, "defaultConfidence")) {
            setDefaultConfidence(ModelUtils.getDoubleProperty(configProps, "defaultConfidence"));
        }
        if (ModelUtils.hasProperty(configProps, "additionalProteins")) {
            setDefaultAdditionalProteins(ModelUtils.getIntegerProperty(configProps, "additionalProteins"));
        }
        if (ModelUtils.hasProperty(configProps, "maxProteins")) {
            setDefaultMaxProteins(ModelUtils.getIntegerProperty(configProps, "maxProteins"));
        }

        CyNetworkViewManager networkViewManager = getService(CyNetworkViewManager.class);
        rootNetworkManager = getService(CyRootNetworkManager.class);
        // If we already have networks loaded, see if they are intact networks
        for (CyNetwork network : registrar.getService(CyNetworkManager.class).getNetworkSet()) {
            CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(network);
            if (rootNetwork.getBaseNetwork().getSUID().equals(network.getSUID())) {
                if (ModelUtils.isIntactNetwork(network)) {
                    IntactNetwork intactNetwork = new IntactNetwork(this);
                    addIntactNetwork(intactNetwork, network);
                    ModelUtils.buildIntactNetworkTableFromExistingOne(intactNetwork);
                    intactNetwork.completeMissingNodeColors();
                    for (CyNetworkView view : networkViewManager.getNetworkViews(network)) {
                        addNetworkView(view);
                    }
                }
            }
        }

        // Get a session property file for the current session
        sessionProperties = ModelUtils.getPropertyService(this, SavePolicy.SESSION_FILE);
    }

    public void setupStyles() {
        StyleMapper.initializeTaxIdToPaint();
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();
        IntactStyle collapsed = new CollapsedIntactStyle(this);
        IntactStyle expanded = new ExpandedIntactStyle(this);
        IntactStyle mutation = new MutationIntactStyle(this);

        for (IntactStyle style : new IntactStyle[]{collapsed, expanded, mutation}) {
            intactStyles.put(style.getStyleViewType(), style);
        }
    }

    public Map<IntactNetworkView.Type, IntactStyle> getIntactStyles() {
        return new HashMap<>(intactStyles);
    }

    public void applyStyle(String styleName) {
        applyStyle(styleName, getCurrentNetworkView());
    }

    public void applyStyle(String styleName, CyNetworkView view) {
        intactStyles.get(styleName).applyStyle(view);
    }

    public void intactViewTypeChanged(IntactNetworkView.Type newType, IntactNetworkView iView) {
        intactStyles.get(newType).applyStyle(iView.getView());
        iView.setType(newType);
        fireIntactViewTypeChangedEvent(new IntactViewTypeChangedEvent(this, newType));
    }

    private void fireIntactViewTypeChangedEvent(IntactViewTypeChangedEvent event) {
        for (IntactViewTypeChangedListener listener: intactViewTypeChangedListeners) {
            listener.handleEvent(event);
        }
    }

    public void toggleFancyStyles() {
        for (IntactStyle style : intactStyles.values()) {
            style.toggleFancy();
        }
    }

    public void updateURIsFromConfig() {
        // Update urls with those from the sever
        Map<String, String> args = new HashMap<>();
        String url = CONFIGURI + CallerIdentity + ".json";
        IntactManager manager = this;

        // Run this in the background in case we have a timeout
        Executors.newCachedThreadPool().execute(() -> {
            JsonNode uris = NullNode.getInstance();
            // use alternative config URI if available and otherwise retrieve the default one
            // based on the app version
            if (alternativeCONFIGURI != null && alternativeCONFIGURI.length() > 0) {
                uris = ModelUtils.getResultsFromJSON(
                        HttpUtils.getJSON(alternativeCONFIGURI, args, manager)
                );
            }
            if (uris != null) {
                if (uris.has("URI")) {
                    URI = uris.get("URI").textValue();
                }
                if (uris.has("STRINGResolveURI")) {
                    STRINGResolveURI = uris.get("STRINGResolveURI").textValue();
                }
                if (uris.has("STITCHResolveURI")) {
                    STITCHResolveURI = uris.get("STITCHResolveURI").textValue();
                }
                if (uris.has("VIRUSESResolveURI")) {
                    VIRUSESResolveURI = uris.get("VIRUSESResolveURI").textValue();
                }
                if (uris.has("DataVersion")) {
                    DATAVERSION = uris.get("DataVersion").textValue();
                }
                if (uris.has("messageUserError")) {
                    error(uris.get("messageUserError").textValue());
                }
                if (uris.has("messageUserCriticalError")) {
                    critical(uris.get("messageUserCriticalError").textValue());
                }
                if (uris.has("messageUserWarning")) {
                    warn(uris.get("messageUserWarning").textValue());
                }
                if (uris.has("messageUserInfo")) {
                    info(uris.get("messageUserInfo").textValue());
                }
            }
            haveURIs = true;
        });
    }

    public CyNetwork createNetwork(String name) {
        CyNetwork network = registrar.getService(CyNetworkFactory.class).createNetwork();
        CyNetworkManager netMgr = registrar.getService(CyNetworkManager.class);

        Set<CyNetwork> nets = netMgr.getNetworkSet();
        Set<CyNetwork> allNets = new HashSet<>(nets);
        for (CyNetwork net : nets) {
            allNets.add(((CySubNetwork) net).getRootNetwork());
        }
        // See if this name is already taken by a network or a network collection (root network)
        int index = -1;
        boolean match = false;
        for (CyNetwork net : allNets) {
            String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
            if (netName.equals(name)) {
                match = true;
            } else if (netName.startsWith(name)) {
                String subname = netName.substring(name.length());
                if (subname.startsWith(" - ")) {
                    try {
                        int v = Integer.parseInt(subname.substring(3));
                        if (v >= index)
                            index = v + 1;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (match && index < 0) {
            name = name + " - 1";
        } else if (index > 0) {
            name = name + " - " + index;
        }
        network.getRow(network).set(CyNetwork.NAME, name);

        return network;
    }

    public void updateStylesColorScheme(Long parentTaxId, Color newColor, boolean addDescendants) {
        Map<Long, Paint> colorScheme = StyleMapper.updateChildrenColors(parentTaxId, newColor, addDescendants);
        for (IntactStyle style : intactStyles.values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    public void resetStyles() {
        StyleMapper.resetMappings();
        for (IntactStyle style : intactStyles.values()) {
            style.setNodePaintStyle();
        }
        for (IntactNetwork network : intactNetworkMap.values()) {
            network.completeMissingNodeColors();
        }
    }


    public List<IntactNetwork> getIntactNetworks() {
        return new ArrayList<>(intactNetworkMap.values());
    }

    public String getNetworkName(CyNetwork net) {
        return net.getRow(net).get(CyNetwork.NAME, String.class);
    }

    public CyNetworkView createNetworkView(CyNetwork network) {
        CyNetworkView view = registrar.getService(CyNetworkViewFactory.class)
                .createNetworkView(network);
        if (intactNetworkMap.containsKey(network)) {
            intactNetworkMap.get(network).hideExpandedEdgesOnViewCreation(view);
            intactStyles.get(CollapsedIntactStyle.type).applyStyle(view);
        }
        return view;
    }

    public void addNetwork(CyNetwork network) {
        registrar.getService(CyNetworkManager.class).addNetwork(network);
        registrar.getService(CyApplicationManager.class).setCurrentNetwork(network);
    }

    public CyNetwork getCurrentNetwork() {
        return registrar.getService(CyApplicationManager.class).getCurrentNetwork();
    }

    public CyNetworkView getCurrentNetworkView() {
        return registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
    }

    public boolean showImage() {
        return showImage;

    }

    public void setShowImage(boolean set) {
        showImage = set;
    }

    public boolean showEnhancedLabels() {
        return showEnhancedLabels;
    }

    public void setShowEnhancedLabels(boolean set) {
        showEnhancedLabels = set;
    }


    public boolean highlightNeighbors() {
        return highlightNeighbors;
    }

    public void setHighlightNeighbors(boolean set) {
        highlightNeighbors = set;
    }

    public void setCytoPanel(DetailPanel panel) {
        this.cytoPanel = panel;
    }


    public Species getDefaultSpecies() {
        if (species == null) {
            // Set Human as the default
            for (Species s : Species.getSpecies()) {
                if (s.toString().equals("Homo sapiens")) {
                    species = s;
                    break;
                }
            }
        }
        return species;
    }

    public void setDefaultSpecies(Species defaultSpecies) {
        species = defaultSpecies;
    }

    public void setDefaultSpecies(String defaultSpecies) {
        species = Species.getSpecies(defaultSpecies);
    }

    public double getDefaultConfidence() {
        return defaultConfidence;
    }

    public void setDefaultConfidence(double conf) {
        defaultConfidence = conf;
    }

    public int getDefaultAdditionalProteins() {
        return additionalProteins;
    }

    public void setDefaultAdditionalProteins(int ap) {
        additionalProteins = ap;
    }

    public int getDefaultMaxProteins() {
        return maximumProteins;
    }

    public void setDefaultMaxProteins(int max) {
        maximumProteins = max;
    }

    public void flushEvents() {
        cyEventHelper.flushPayloadEvents();
    }

    public void execute(TaskIterator iterator) {
        execute(iterator, false);
    }

    public void execute(TaskIterator iterator, TaskObserver observer) {
        execute(iterator, observer, false);
    }

    public void execute(TaskIterator iterator, boolean synchronous) {
        if (synchronous) {
            synchronousTaskManager.execute(iterator);
        } else {
            dialogTaskManager.execute(iterator);
        }
    }

    public void execute(TaskIterator iterator, TaskObserver observer, boolean synchronous) {
        if (synchronous) {
            synchronousTaskManager.execute(iterator, observer);
        } else {
            dialogTaskManager.execute(iterator, observer);
        }
    }

    public void executeCommand(String namespace, String command,
                               Map<String, Object> args, TaskObserver observer) {
        TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(namespace, command, args, observer);
        execute(ti, true);
    }

    public String getDataVersion() {
        return DATAVERSION;
    }

    public String getOldDataVersion() {
        return OLD_DATAVERSION;
    }

    private String getDataAPIURL() {
        String alternativeAPI = ModelUtils.getStringProperty(configProps,
                alternativeAPIProperty);
        if (alternativeAPI != null && alternativeAPI.length() > 0) return alternativeAPI;
        return URI;
    }

    public String getNetworkURL() {
        return getDataAPIURL() + "network";
    }

    public String getResolveURL(String useDATABASE) {
        if (useDATABASE.equals(Databases.STITCH.getAPIName()))
            return STITCHResolveURI;
        else if (useDATABASE.equals(Databases.VIRUSES.getAPIName()))
            return VIRUSESResolveURI;
        else if (useSTRINGDevelopmentVersion)
            return STRINGDevelopmentURI;

        return STRINGResolveURI;
    }

    public boolean isVirusesEnabled() {
        return enableViruses;
    }

    public void info(String info) {
        logger.info(info);
    }

    public void warn(String warn) {
        logger.warn(warn);
    }

    public void error(String error) {
        logger.error(error);
    }

    public void critical(String criticalError) {
        logger.error(criticalError);
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(null, "<html><p style=\"width:200px;\">" + criticalError + "</p></html>", "Critical stringApp error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void ignoreAdd() {
        ignore = true;
    }

    public void listenToAdd() {
        ignore = false;
    }

    public void updateSettings() {
        double overlapCutoff = 0.5;
        ModelUtils.setStringProperty(configProps, "confidence", Double.toString(overlapCutoff));
        ModelUtils.setStringProperty(configProps, "showImage", Boolean.toString(showImage));
        ModelUtils.setStringProperty(configProps, "showEnhancedLabels", Boolean.toString(showEnhancedLabels));
        ModelUtils.setStringProperty(configProps, "highlightNeighbors", Boolean.toString(highlightNeighbors));

        ModelUtils.setStringProperty(configProps, "species", getDefaultSpecies().toString());
        ModelUtils.setStringProperty(configProps, "defaultConfidence", Double.toString(getDefaultConfidence()));
        ModelUtils.setStringProperty(configProps, "additionalProteins", Integer.toString(getDefaultAdditionalProteins()));
        ModelUtils.setStringProperty(configProps, "maxProteins", Integer.toString(getDefaultMaxProteins()));

        ModelUtils.setStringProperty(configProps, "overlapCutoff", Double.toString(overlapCutoff));
        int topTerms = 5;
        ModelUtils.setStringProperty(configProps, "topTerms", Integer.toString(topTerms));
        ModelUtils.setStringProperty(configProps, "chartType", chartType.name());
        ModelUtils.setStringProperty(configProps, "enrichmentPalette", brewerPalette.toString());
        boolean removeOverlap = false;
        ModelUtils.setStringProperty(configProps, "removeOverlap", Boolean.toString(removeOverlap));
        {
            StringBuilder categories = new StringBuilder();
            for (EnrichmentTerm.TermCategory c : categoryFilter) {
                categories.append(c.name()).append(",");
            }
            if (categories.length() > 1)
                categories = new StringBuilder(categories.substring(categories.length() - 1));
            ModelUtils.setStringProperty(configProps, "categoryFilter", categories.toString());
        }

        ModelUtils.setStringProperty(configProps, "channelColors", getChannelColorString());
    }

    public void handleEvent(NetworkAddedEvent nae) {
        CyNetwork network = nae.getNetwork();
        if (ignore) return;

        // This is a string network only if we have a confidence score in the network table,
        // "@id", "species", "canonical name", and "sequence" columns in the node table, and
        // a "score" column in the edge table
        if (ModelUtils.isIntactNetwork(network)) {
            IntactNetwork intactNet = new IntactNetwork(this);
            addIntactNetwork(intactNet, network);
            showResultsPanel();
            intactNet.completeMissingNodeColors();
        }
    }

    public void handleEvent(SessionLoadedEvent event) {
        // Get any properties we stored in the session
        sessionProperties = ModelUtils.getPropertyService(this, SavePolicy.SESSION_FILE);

        // Create string networks for any networks loaded by string
        Set<CyNetwork> networks = event.getLoadedSession().getNetworks();
        Set<CyNetwork> networksToUpgrade = new HashSet<>();
        for (CyNetwork network : networks) {
            if (ModelUtils.isIntactNetwork(network)) {
                if (ModelUtils.ifHaveIntactNS(network)) {
                    IntactNetwork intactNetwork = new IntactNetwork(this);
                    addIntactNetwork(intactNetwork, network);
                    ModelUtils.buildIntactNetworkTableFromExistingOne(intactNetwork);
                    intactNetwork.completeMissingNodeColors();
                } else if (ModelUtils.getDataVersion(network) == null) {
                    networksToUpgrade.add(network);
                }
            }
        }

        linkIntactTablesToNetwork(event.getLoadedSession().getTables());

        for (CyNetworkView view : event.getLoadedSession().getNetworkViews()) {
            if (ModelUtils.isIntactNetwork(view.getModel())) {
                addNetworkView(view);
            }
        }


        if (ModelUtils.ifHaveIntactNS(getCurrentNetwork()))
            showResultsPanel();
        else
            hideResultsPanel();
    }

    public void linkIntactTablesToNetwork(Collection<CyTableMetadata> tables) {
        for (CyTableMetadata tableM : tables) {
            CyTable table = tableM.getTable();
            CyColumn nodeRefs = table.getColumn(ModelUtils.NODE_REF);
            if (nodeRefs != null) { // If the table is an Intact unassigned table
                List<Long> values = nodeRefs.getValues(Long.class);
                if (!values.isEmpty()) {
                    Long nodeSUID = values.get(0);
                    if (nodeSUID != null) {
                        for (IntactNetwork iNetwork : intactNetworkMap.values()) {
                            if (iNetwork.getNetwork().getNode(nodeSUID) != null) { // If the node referenced belong to this network
                                if (table.getColumn(ModelUtils.IDENTIFIER_ID) != null) {
                                    iNetwork.setIdentifiersTable(table);
                                } else {
                                    iNetwork.setFeaturesTable(table);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
        CyNetwork network = e.getNetwork();
        // delete enrichment tables
        CyTableManager tableManager = getService(CyTableManager.class);
        IntactNetwork intactNetwork = intactNetworkMap.get(network);
        if (intactNetwork != null) {
            tableManager.deleteTable(intactNetwork.featuresTable.getSUID());
            tableManager.deleteTable(intactNetwork.identifiersTable.getSUID());
            // remove as string network
            intactNetworkMap.remove(network);

        }
    }

    public void showResultsPanel() {
        if (cytoPanel == null) {
            execute(resultsPanelTaskFactory.createTaskIterator(), true);
        } else {
            // Make sure we show it
            cytoPanel.showCytoPanel();
        }
    }

    public void hideResultsPanel() {
        if (cytoPanel != null) {
            cytoPanel.hideCytoPanel();
        }
    }

    public ShowResultsPanelTaskFactory getShowResultsPanelTaskFactory() {
        return resultsPanelTaskFactory;
    }

    public void setShowResultsPanelTaskFactory(ShowResultsPanelTaskFactory factory) {
        resultsPanelTaskFactory = factory;
    }

    public <T> T getService(Class<? extends T> clazz) {
        return registrar.getService(clazz);
    }

    public <T> T getService(Class<? extends T> clazz, String filter) {
        return registrar.getService(clazz, filter);
    }

    public void registerService(Object service, Class<?> clazz, Properties props) {
        registrar.registerService(service, clazz, props);
    }

    public void registerAllServices(CyProperty<Properties> service, Properties props) {
        registrar.registerAllServices(service, props);
    }

    public void unregisterService(Object service, Class<?> clazz) {
        registrar.unregisterService(service, clazz);
    }

    public void setVersion(String version) {
        String v = version.replace('.', '_');
        IntactManager.CallerIdentity = "string_app_v" + v;
    }

    public boolean haveEnhancedGraphics() {
        return availableCommands.getNamespaces().contains("enhancedGraphics");
    }

    public boolean haveURIs() {
        return haveURIs;
    }

    public boolean haveChemViz() {
        if (haveChemViz == null)
            haveChemViz = availableCommands.getNamespaces().contains("chemviz");
        return haveChemViz;
    }

    public boolean haveCyBrowser() {
        if (haveCyBrowser == null)
            haveCyBrowser = availableCommands.getNamespaces().contains("cybrowser");
        return haveCyBrowser;
    }


    public void setChannelColors(Map<String, Color> colorMap) {
        channelColors = colorMap;
    }


    public String getChannelColorString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            Color clr = channelColors.get(channels[i]);
            int rgb = clr.getRGB();
            str.append("#").append(Integer.toUnsignedString(rgb, 16)).append("|"); // get the hex
        }

        return str.substring(0, str.length() - 1);
    }

    public CyProperty<Properties> getConfigProperties() {
        return configProps;
    }

    // Assumes hex color: #ff000000
    private Color parseColor(String s) {
        int r = 0, g = 0, b = 0;
        if (s.length() == 9)
            s = s.substring(3);
        else if (s.length() == 7)
            s = s.substring(1);
        else return Color.BLACK;

        r = Integer.parseInt(s.substring(0, 2), 16);
        g = Integer.parseInt(s.substring(2, 4), 16);
        b = Integer.parseInt(s.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    public void addNetworkView(CyNetworkView view) {
        intactNetworkViewMap.put(view, new IntactNetworkView(this, view));
    }


    @Override
    public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
        intactNetworkViewMap.remove(e.getNetworkView());
    }

    public void addIntactNetwork(IntactNetwork intactNetwork, CyNetwork network) {
        intactNetworkMap.put(network, intactNetwork);
        intactNetwork.setNetwork(network);
        for (IntactNetworkCreatedListener listener : intactNetworkCreatedListeners) {
            listener.handleEvent(new IntactNetworkCreatedEvent(this, intactNetwork));
        }
    }

    public IntactNetwork getIntactNetwork(CyNetwork network) {
        if (intactNetworkMap.containsKey(network))
            return intactNetworkMap.get(network);
        return null;
    }

    public IntactNetwork getCurrentIntactNetwork() {
        return intactNetworkMap.get(getCurrentNetwork());
    }

    public IntactNetworkView getIntactNetworkView(CyNetworkView view) {
        return intactNetworkViewMap.get(view);
    }

    public IntactNetworkView getCurrentIntactNetworkView() {
        return intactNetworkViewMap.get(getCurrentNetworkView());
    }


    public void addIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.add(listener);
    }

    public void removeIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.remove(listener);
    }

    public void addIntactViewTypeChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.add(listener);
    }

    public void removeIntactViewTypeChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.remove(listener);
    }

}
