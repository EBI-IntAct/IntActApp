package uk.ac.ebi.intact.intactApp.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;

import java.util.*;
import java.util.regex.Pattern;

public class ModelUtils {

    // Namespaces
    public static String INTACTDB_NAMESPACE = "IntAct Database";
    public static String COLLAPSED_NAMESPACE = "Collapsed";
    public static String FEATURE_NAMESPACE = "Feature";
    public static String IDENTIFIER_NAMESPACE = "Identifier";
    public static String TABLE_NAMESPACE = "Table";
    public static String NAMESPACE_SEPARATOR = "::";

    public static final String NODE_REF = "LastNode.SUID";
    // Network tables column
    public static final String FEATURES_TABLE_REF = "Features.SUID";
    public static final String IDENTIFIERS_TABLE_REF = "Identifiers.SUID";

    // Feature table columns
    public static final String FEATURES = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Features";
    public static final String SOURCE_FEATURES = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Source features";
    public static final String TARGET_FEATURES = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Target features";
    public static final String FEATURE_AC = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Accession";
    public static final String FEATURE_NAME = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Name";
    public static final String FEATURE_TYPE = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Type";
    public static final String FEATURE_TYPE_MI_ID = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Type MI Id";
    public static final String FEATURE_TYPE_MOD_ID = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Type Mod Id";
    public static final String FEATURE_TYPE_PAR_ID = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Type Par Id";
    public static final String FEATURE_EDGE_IDS = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Edge IDs";
    public static final String FEATURE_EDGE_SUIDS = FEATURE_NAMESPACE + NAMESPACE_SEPARATOR + "Edge SUIDs";

    // Xrefs table columns
    public static final String IDENTIFIERS = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Identifiers";
    public static final String IDENTIFIER_AC = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Accession";
    public static final String IDENTIFIER_ID = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Identifier";
    public static final String IDENTIFIER_DB_NAME = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Database Name";
    public static final String IDENTIFIER_DB_MI_ID = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Database MI Id";
    public static final String IDENTIFIER_QUALIFIER = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Qualifier";
    public static final String IDENTIFIER_QUALIFIER_ID = IDENTIFIER_NAMESPACE + NAMESPACE_SEPARATOR + "Qualifier MI Id";

    // Node information
    public static String ELABEL_STYLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "enhancedLabel Passthrough";


    public static String INTACT_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Id";
    public static String INTACT_AC = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Accession Id";
    public static String PREFERRED_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Preferred Id";
    public static String PREFERRED_ID_DB = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Preferred Id Database";
    public static String PREFERRED_ID_DB_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Preferred Id Database MI ID";
    public static String TAX_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Tax Id";
    public static String MUTATION = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Mutation";
    public static String FULL_NAME = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Full name";

    public static String INTERACTION_TYPE_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Interaction type MI Id";
    public static String DETECTION_METHOD = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Detection method";
    public static String DETECTION_METHOD_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Detection method MI id";
    public static String EXPANSION_TYPE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Expansion type";
    public static String HOST_ORGANISM = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Host organism";
    public static String HOST_ORGANISM_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Host organism tax id";
    public static String PUBMED_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "PubMed id";
    public static String AFFECTED_BY_MUTATION = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Affected by mutation";
    public static String MI_SCORE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "MI Score";

    public static String SOURCE_BIOLOGICAL_ROLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Source biological role";
    public static String SOURCE_BIOLOGICAL_ROLE_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Source biological role MI id";
    public static String TARGET_BIOLOGICAL_ROLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Target biological role";
    public static String TARGET_BIOLOGICAL_ROLE_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Target biological role MI id";


    public static String C_IS_COLLAPSED = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "is collapsed";
    public static String C_INTACT_IDS = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "collapsed edge IDs";
    public static String C_INTACT_SUIDS = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "collapsed edge SUIDs";
    public static String C_NB_EDGES = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "# evidences";

    public static String QUERYTERM = "query term";
    public static String SPECIES = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "species";
    public static String STRINGID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "database identifier";
    public static String TYPE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Interactor type";
    public static String TYPE_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Interactor type mi id";

    public static String COMPARTMENT_NAMESPACE = "compartment";

    // public static String TM_LINKOUT = "TextMining Linkout";
    public static List<String> ignoreKeys = new ArrayList<>(Arrays.asList("@id", "description",
            "id", "preferred_id", "preferred_id_database_name", "preferred_id_database_mi_identifier", "type", "type_mi_id", "species", "interactor_name", "label", "taxId",
            "source", "target", "ac", "interaction_detection_method", "interaction_type", "mi_score", "disrupted_by_mutation",
            "expansion_type", "host_organism", "host_organism_tax_id", "interaction_detection_method_mi_identifier", "interaction_type_mi_identifier",
            "type_mi_identifier", "type_mod_identifier", "type_par_identifier", "identifiers", "pubmed_id", "full_name"
    ));
    public static List<String> namespacedNodeAttributes = new ArrayList<>(Arrays.asList("canonical name", "full name", "chemViz Passthrough",
            "enhancedLabel Passthrough", "description", "disease score", "namespace", "sequence", "smiles", "species", "database identifier",
            "STRING style", "node type", "textmining foreground", "textmining background", "textmining score"));

    //public static Pattern cidmPattern = Pattern.compile("\\(CIDm\\)0*");
    public static Pattern cidmPattern = Pattern.compile("CIDm0*");
    // public static String DISEASEINFO =
    // "http://diseases.jensenlab.org/Entity?type1=9606&type2=-26";

    public static List<String> namespacedEdgeAttributes = new ArrayList<>(Arrays.asList("score", "interspecies", "experiments", "cooccurrence",
            "coexpression", "textmining", "databases", "neighborhood"));

    // Network information
    public static String CONFIDENCE = "confidence score";
    public static String DATABASE = "database";
    public static String NET_SPECIES = "species";
    public static String NET_DATAVERSION = "data version";
    public static String NET_URI = "uri";
    public static String NET_ENRICHMENT_SETTINGS = "enrichmentSettings";

    public static String showEnhancedLabelsFlag = "showEnhancedLabels";

    // Create network view size threshold
    // See https://github.com/cytoscape/cytoscape-impl/blob/develop/core-task-impl/
    // src/main/java/org/cytoscape/task/internal/loadnetwork/AbstractLoadNetworkTask.java
    public static int DEF_VIEW_THRESHOLD = 3000;
    public static String VIEW_THRESHOLD = "viewThreshold";

    // Other stuff
    public static String COMPOUND = "STITCH compounds";
    public static String EMPTYLINE = "--------";

    public static String REQUERY_MSG_USER =
            "<html>This action cannot be performed on the current network as it <br />"
                    + "appears to be an old STRING network. Would you like to get <br />"
                    + "the latest STRING network for the nodes in your network?</html>";
    public static String REQUERY_TITLE = "Re-query network?";

    private static IntactManager manager;

    public static boolean haveQueryTerms(CyNetwork network) {
        if (network == null) return false;
        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(QUERYTERM, String.class) != null)
                return true;
        }
        return false;
    }

    public static void selectQueryTerms(CyNetwork network) {
        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(QUERYTERM, String.class) != null)
                network.getRow(node).set(CyNetwork.SELECTED, true);
            else
                network.getRow(node).set(CyNetwork.SELECTED, false);
        }
    }

    public static List<String> getCompartmentList(CyNetwork network) {
        List<String> compartments = new ArrayList<>();
        if (network == null) {
            return compartments;
        }
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(COMPARTMENT_NAMESPACE);
        if (columns == null || columns.size() == 0) return compartments;
        for (CyColumn col : columns) {
            compartments.add(col.getNameOnly());
        }
        return compartments;
    }

    public static List<CyNode> augmentNetworkFromJSON(IntactManager manager, CyNetwork net,
                                                      List<CyEdge> newEdges, JsonNode object, Map<String, String> queryTermMap,
                                                      String useDATABASE) {
        //TODO Make augmentNetworkFromJson
//        JsonNode results = getResultsFromJSON(object);
//        if (results == null)
//            return null;
//
//        Map<String, CyNode> nodeMap = new HashMap<>();
//        Map<String, String> nodeNameMap = new HashMap<>();
//        String species = ModelUtils.getNetSpecies(net);
//        // TODO: Check if we really don't have to infer the database!
//
//        for (CyNode node : net.getNodeList()) {
//            if (species == null)
//                species = net.getRow(node).get(SPECIES, String.class);
//            String stringId = net.getRow(node).get(STRINGID, String.class);
//            if (stringId == null)
//                continue; // Could be merged from another network
//            String name = net.getRow(node).get(CyNetwork.NAME, String.class);
//            nodeMap.put(stringId, node);
//            nodeNameMap.put(stringId, name);
//            // TODO: Change network from string to stitch once we add compounds?
//            if (isCompound(net, node))
//                useDATABASE = Databases.STITCH.getAPIName();
//        }
//        setDatabase(net, useDATABASE);
//
//        List<CyNode> nodes = getJSON(manager, species, net, nodeMap, nodeNameMap, queryTermMap,
//                newEdges, results, useDATABASE);
//        return nodes;
        return null;
    }

    public static void setConfidence(CyNetwork network, double confidence) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), Double.class, CONFIDENCE);
        network.getRow(network).set(CONFIDENCE, confidence);
    }

    public static Double getConfidence(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(CONFIDENCE) == null)
            return null;
        return network.getRow(network).get(CONFIDENCE, Double.class);
    }

    public static String getDatabase(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(DATABASE) == null)
            return null;
        return network.getRow(network).get(DATABASE, String.class);
    }

    public static String getDataVersion(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(NET_DATAVERSION) == null)
            return null;
        return network.getRow(network).get(NET_DATAVERSION, String.class);
    }

    public static String getNetSpecies(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(NET_SPECIES) == null)
            return null;
        return network.getRow(network).get(NET_SPECIES, String.class);
    }

    public static List<String> getAllNetSpecies(CyNetwork net) {
        List<String> species = new ArrayList<>();
        for (CyNode node : net.getNodeList()) {
            String nSpecies = net.getRow(node).get(SPECIES, String.class);
            if (nSpecies != null && !nSpecies.equals("") && !species.contains(nSpecies))
                species.add(nSpecies);
        }
        return species;
    }

    public static String formatForColumnNamespace(String columnName) {
        String formattedColumnName = columnName;
        if (columnName.contains("::")) {
            if (columnName.startsWith(INTACTDB_NAMESPACE))
                formattedColumnName = columnName.substring(INTACTDB_NAMESPACE.length() + 2);
            else
                formattedColumnName = columnName.replaceFirst("::", " ");
        }
        return formattedColumnName;
    }

    public static boolean isMergedIntactNetwork(CyNetwork network) {
        CyTable nodeTable = network.getDefaultNodeTable();
        if (nodeTable.getColumn(INTACT_ID) == null)
            return false;
        // Enough to check for id in the node columns and score in the edge columns
        //if (nodeTable.getColumn(SPECIES) == null)
        //	return false;
        //if (nodeTable.getColumn(CANONICAL) == null)
        //	return false;
        CyTable edgeTable = network.getDefaultEdgeTable();
        return edgeTable.getColumn(MI_SCORE) != null;
    }

    public static boolean isIntactNetwork(CyNetwork network) {
        return isMergedIntactNetwork(network);
    }

    // This method will tell us if we have the new side panel functionality (i.e. namespaces)
    public static boolean ifHaveIntactNS(CyNetwork network) {
        if (network == null) return false;
        CyRow netRow = network.getRow(network);
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(INTACTDB_NAMESPACE);
        return columns != null && columns.size() > 0;
    }

    public static boolean isCurrentDataVersion(CyNetwork network) {
        return network != null && network.getRow(network).get(NET_DATAVERSION, String.class) != null
                && network.getRow(network).get(NET_DATAVERSION, String.class)
                .equals(IntactManager.DATAVERSION);
    }

    public static String getExisting(CyNetwork network) {
        StringBuilder str = new StringBuilder();
        for (CyNode node : network.getNodeList()) {
            String stringID = network.getRow(node).get(STRINGID, String.class);
            if (stringID != null && stringID.length() > 0)
                str.append(stringID).append("\n");
        }
        return str.toString();
    }

    public static void createColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;
        table.createColumn(columnName, clazz, false);
    }

    public static <T> void createColumnIfNeeded(CyTable table, Class<T> clazz, String columnName, T defaultValue) {
        if (table.getColumn(columnName) != null)
            return;
        table.createColumn(columnName, clazz, false, defaultValue);
    }

    public static void replaceColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createColumn(columnName, clazz, false);
    }

    public static void createListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;

        table.createListColumn(columnName, clazz, false);
    }

    public static void replaceListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createListColumn(columnName, clazz, false);
    }

    public static void deleteColumnIfExisting(CyTable table, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);
    }

    public static String getName(CyNetwork network, CyIdentifiable ident) {
        return getString(network, ident, CyNetwork.NAME);
    }

    public static String getString(CyNetwork network, CyIdentifiable ident, String column) {
        if (network.getRow(ident, CyNetwork.DEFAULT_ATTRS) != null)
            return network.getRow(ident, CyNetwork.DEFAULT_ATTRS).get(column, String.class);
        return null;
    }

    public static List<String> getAvailableInteractionPartners(CyNetwork network) {
        List<String> availableTypes = new ArrayList<>();
        List<String> species = ModelUtils.getAllNetSpecies(network);
        Collections.sort(species);
        String netSp = getNetSpecies(network);
        if (netSp != null && !species.contains(netSp)) {
            availableTypes.add(netSp);
        }
        availableTypes.addAll(species);
        availableTypes.add(COMPOUND);
        List<String> spPartners = new ArrayList<>();
        for (String sp : species) {
            List<String> partners = Species.getSpeciesPartners(sp);
            for (String spPartner : partners) {
                if (!species.contains(spPartner))
                    spPartners.add(spPartner);
            }
        }
        Collections.sort(spPartners);
        if (spPartners.size() > 0) {
            availableTypes.add(EMPTYLINE);
            availableTypes.addAll(spPartners);
        }
        return availableTypes;
    }


    public static JsonNode getResultsFromJSON(JsonNode json) {
        if (json == null || !json.has(IntactManager.RESULT))
            return null;

        return json.get(IntactManager.RESULT);
    }

    public static Integer getVersionFromJSON(JsonNode json) {
        if (json == null || !json.has(IntactManager.APIVERSION))
            return null;
        return json.get(IntactManager.APIVERSION).intValue();
    }


    public static void setStringProperty(CyProperty<Properties> properties,
                                         String propertyKey, Object propertyValue) {
        Properties p = properties.getProperties();
        p.setProperty(propertyKey, propertyValue.toString());
    }

    public static boolean hasProperty(CyProperty<Properties> properties, String propertyKey) {
        Properties p = properties.getProperties();
        return p.getProperty(propertyKey) != null;
    }

    public static String getStringProperty(CyProperty<Properties> properties, String propertyKey) {
        Properties p = properties.getProperties();
        if (p.getProperty(propertyKey) != null)
            return p.getProperty(propertyKey);
        return null;
    }

    public static Double getDoubleProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Double.valueOf(value);
    }

    public static Integer getIntegerProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Integer.valueOf(value);
    }

    public static Boolean getBooleanProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Boolean.valueOf(value);
    }

    public static String listToString(List<?> list) {
        StringBuilder str = new StringBuilder();
        if (list == null || list.size() == 0) return str.toString();
        for (int i = 0; i < list.size() - 1; i++) {
            str.append(list.get(i)).append(",");
        }
        return str + list.get(list.size() - 1).toString();
    }

    public static List<String> stringToList(String string) {
        if (string == null || string.length() == 0) return new ArrayList<>();
        String[] arr = string.split(",");
        return Arrays.asList(arr);
    }

    public static void updateEnrichmentSettings(CyNetwork network, Map<String, String> settings) {
        StringBuilder setting = new StringBuilder();
        int index = 0;
        for (String key : settings.keySet()) {
            if (index > 0) {
                setting.append(";");
            }
            setting.append(key).append("=").append(settings.get(key));
            index++;
        }
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, NET_ENRICHMENT_SETTINGS);
        network.getRow(network).set(NET_ENRICHMENT_SETTINGS, setting.toString());
    }

    public static Map<String, String> getEnrichmentSettings(CyNetwork network) {
        Map<String, String> settings = new HashMap<>();
        String setting = network.getRow(network).get(NET_ENRICHMENT_SETTINGS, String.class);
        if (setting == null || setting.length() == 0)
            return settings;

        String[] settingArray = setting.split(";");
        for (String s : settingArray) {
            String[] pair = s.split("=");
            if (pair.length == 2) {
                settings.put(pair[0], pair[1]);
            }
        }
        return settings;
    }

    public static CyProperty<Properties> getPropertyService(IntactManager manager,
                                                            SavePolicy policy) {
        String name = "stringApp";
        if (policy.equals(SavePolicy.SESSION_FILE)) {
            CyProperty<Properties> service = manager.getService(CyProperty.class, "(cyPropertyName=" + name + ")");
            // Do we already have a session with our properties
            if (service.getSavePolicy().equals(SavePolicy.SESSION_FILE))
                return service;

            // Either we have a null session or our properties aren't in this session
            Properties props = new Properties();
            service = new SimpleCyProperty<>(name, props, Properties.class, SavePolicy.SESSION_FILE);
            Properties serviceProps = new Properties();
            serviceProps.setProperty("cyPropertyName", service.getName());
            manager.registerAllServices(service, serviceProps);
            return service;
        } else if (policy.equals(SavePolicy.CONFIG_DIR) || policy.equals(SavePolicy.SESSION_FILE_AND_CONFIG_DIR)) {
            CyProperty<Properties> service = new ConfigPropsReader(policy, name);
            Properties serviceProps = new Properties();
            serviceProps.setProperty("cyPropertyName", service.getName());
            manager.registerAllServices(service, serviceProps);
            return service;
        }
        return null;
    }

    public static int getViewThreshold(IntactManager manager) {
        final Properties props = (Properties) manager
                .getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
        final String vts = props.getProperty(VIEW_THRESHOLD);
        int threshold;

        try {
            threshold = Integer.parseInt(vts);
        } catch (Exception e) {
            threshold = DEF_VIEW_THRESHOLD;
        }

        return threshold;
    }

    // Method to convert terms entered in search text to
    // appropriate newline-separated string to send to server
    public static String convertTerms(String terms, boolean splitComma, boolean splitSpaces) {
        String regexSp = "\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
        String regexComma = "[,]+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
        if (splitSpaces) {
            // Substitute newlines for space
            terms = terms.replaceAll(regexSp, "\n");
        }

        if (splitComma) {
            // Substitute newlines for commas
            terms = terms.replaceAll(regexComma, "\n");
        }

        // Strip off any blank lines
        terms = terms.replaceAll("(?m)^\\s*", "");
        return terms;
    }

    public static void copyRow(CyTable fromTable, CyTable toTable, CyIdentifiable from, CyIdentifiable to, List<String> columnsCreated) {
        for (CyColumn col : fromTable.getColumns()) {
            if (!columnsCreated.contains(col.getName()))
                continue;
            if (col.getName().equals(CyNetwork.SUID))
                continue;
            if (col.getName().equals(CyNetwork.NAME))
                continue;
            if (col.getName().equals(CyNetwork.SELECTED))
                continue;
            if (col.getName().equals(CyRootNetwork.SHARED_NAME))
                continue;
            if (from.getClass().equals(CyEdge.class) && col.getName().equals(CyRootNetwork.SHARED_INTERACTION))
                continue;
            if (from.getClass().equals(CyEdge.class) && col.getName().equals(CyEdge.INTERACTION))
                continue;
            Object v = fromTable.getRow(from.getSUID()).getRaw(col.getName());
            toTable.getRow(to.getSUID()).set(col.getName(), v);
        }
    }

    public static void createNodeMap(CyNetwork network, Map<String, CyNode> nodeMap, String column) {
        // Get all of the nodes in the network
        for (CyNode node : network.getNodeList()) {
            String key = network.getRow(node).get(column, String.class);
            nodeMap.put(key, node);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> copyColumns(CyTable fromTable, CyTable toTable) {
        List<String> columns = new ArrayList<>();
        for (CyColumn col : fromTable.getColumns()) {
            String fqn = col.getName();
            // Does that column already exist in our target?
            if (toTable.getColumn(fqn) == null) {
                // No, create it.
                if (col.getType().equals(List.class)) {
                    // There is no easy way to handle this, unfortunately...
                    // toTable.createListColumn(fqn, col.getListElementType(), col.isImmutable(), (List<?>)col.getDefaultValue());
                    if (col.getListElementType().equals(String.class))
                        toTable.createListColumn(fqn, String.class, col.isImmutable(),
                                (List<String>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Long.class))
                        toTable.createListColumn(fqn, Long.class, col.isImmutable(),
                                (List<Long>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Double.class))
                        toTable.createListColumn(fqn, Double.class, col.isImmutable(),
                                (List<Double>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Integer.class))
                        toTable.createListColumn(fqn, Integer.class, col.isImmutable(),
                                (List<Integer>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Boolean.class))
                        toTable.createListColumn(fqn, Boolean.class, col.isImmutable(),
                                (List<Boolean>) col.getDefaultValue());
                } else {
                    toTable.createColumn(fqn, col.getType(), col.isImmutable(), col.getDefaultValue());
                    columns.add(fqn);
                }
            }
        }
        return columns;
    }

    public static void copyNodePositions(IntactManager manager, CyNetwork from, CyNetwork to,
                                         Map<String, CyNode> nodeMap, String column) {
        CyNetworkView fromView = getNetworkView(manager, from);
        CyNetworkView toView = getNetworkView(manager, to);
        for (View<CyNode> nodeView : fromView.getNodeViews()) {
            // Get the to node
            String nodeKey = from.getRow(nodeView.getModel()).get(column, String.class);
            if (!nodeMap.containsKey(nodeKey))
                continue;
            View<CyNode> toNodeView = toView.getNodeView(nodeMap.get(nodeKey));
            // Copy over the positions
            Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            Double z = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION);
            toNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
            toNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
            if (z != null && z != 0.0)
                toNodeView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, z);
        }
    }

    public static void copyEdges(CyNetwork fromNetwork, CyNetwork toNetwork,
                                 Map<String, CyNode> nodeMap, String column) {
        List<String> columnsCreated = copyColumns(fromNetwork.getDefaultEdgeTable(), toNetwork.getDefaultEdgeTable());
        List<CyEdge> edgeList = fromNetwork.getEdgeList();
        for (CyEdge edge : edgeList) {
            CyNode sourceNode = edge.getSource();
            CyNode targetNode = edge.getTarget();
            boolean isDirected = edge.isDirected();

            String source = fromNetwork.getRow(sourceNode).get(column, String.class);
            String target = fromNetwork.getRow(targetNode).get(column, String.class);

            if (!nodeMap.containsKey(source) || !nodeMap.containsKey(target))
                continue;

            CyNode newSource = nodeMap.get(source);
            CyNode newTarget = nodeMap.get(target);

            CyEdge newEdge = toNetwork.addEdge(newSource, newTarget, isDirected);
            copyRow(fromNetwork.getDefaultEdgeTable(), toNetwork.getDefaultEdgeTable(), edge, newEdge, columnsCreated);
        }
    }

    public static CyNetworkView getNetworkView(IntactManager manager, CyNetwork network) {
        Collection<CyNetworkView> views =
                manager.getService(CyNetworkViewManager.class).getNetworkViews(network);

        Iterator<CyNetworkView> iterator = views.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static void copyNodeAttributes(CyNetwork from, CyNetwork to,
                                          Map<String, CyNode> nodeMap, String column) {
        // System.out.println("copyNodeAttributes");
        List<String> columnsCreated = copyColumns(from.getDefaultNodeTable(), to.getDefaultNodeTable());
        for (CyNode node : from.getNodeList()) {
            String nodeKey = from.getRow(node).get(column, String.class);
            if (!nodeMap.containsKey(nodeKey))
                continue;
            CyNode newNode = nodeMap.get(nodeKey);
            copyRow(from.getDefaultNodeTable(), to.getDefaultNodeTable(), node, newNode, columnsCreated);
        }
    }

    public static class ConfigPropsReader extends AbstractConfigDirPropsReader {
        ConfigPropsReader(SavePolicy policy, String name) {
            super(name, "stringApp.props", policy);
        }
    }


    /////////////////////////////////////////////////////////////////////

    public static CyNetwork createIntactNetworkFromJSON(IntactNetwork intactNetwork, JsonNode object, Map<String, String> queryTermMap, String netName) {
        intactNetwork.getManager().ignoreAdd();
        CyNetwork network = createIntactNetworkFromJSON(intactNetwork.getManager(), intactNetwork, object, queryTermMap, netName);
        intactNetwork.getManager().listenToAdd();
        return network;
    }

    private static CyNetwork createIntactNetworkFromJSON(IntactManager manager, IntactNetwork intactNetwork, JsonNode object, Map<String, String> queryTermMap, String netName) {
//        JsonNode results = getResultsFromJSON(object);
//        if (results == null)
//            return null;
        ModelUtils.manager = manager;

        // Get a network name
        String defaultName = "IntAct Network";
        if (netName != null && !netName.equals("")) {
            netName = defaultName + " - " + netName;
        } else if (queryTermMap != null && queryTermMap.size() == 1) {
            netName = defaultName + " - " + queryTermMap.values().iterator().next();
        } else {
            netName = defaultName;
        }

        // Create the network
        CyNetwork newNetwork = manager.createNetwork(netName);

        // Create a map to save the nodes
        Map<String, CyNode> nodeMap = new HashMap<>();

        // Create a map to save the node names
        Map<String, String> nodeNameMap = new HashMap<>();

        loadJSON(manager, intactNetwork, newNetwork, nodeMap, nodeNameMap, null, object);

//        manager.addNetwork(newNetwork);
        return newNetwork;
    }

    public static List<CyNode> loadJSON(IntactManager manager, IntactNetwork intactNetwork, CyNetwork network, Map<String, CyNode> nodeMap, Map<String, String> nodeNameMap, List<CyEdge> newEdges, JsonNode json) {
        try {
            List<CyNode> newNodes = new ArrayList<>();
            CyTable defaultNodeTable = network.getDefaultNodeTable();
            CyTable defaultEdgeTable = network.getDefaultEdgeTable();

            CyTableManager tableManager = manager.getService(CyTableManager.class);

            CyTableFactory tableFactory = manager.getService(CyTableFactory.class);
            String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);

            CyTable featuresTable = tableFactory.createTable("Features of " + networkName, FEATURE_AC, String.class, true, true);
            CyTable xRefsTable = tableFactory.createTable("Identifiers of " + networkName, IDENTIFIER_AC, String.class, true, true);

            initTables(intactNetwork, network, defaultNodeTable, defaultEdgeTable, tableManager, featuresTable, xRefsTable);

            JsonNode nodesJSON = json.get("nodes");
            JsonNode edgesJSON = json.get("edges");

            if (nodesJSON.size() > 0) {
                createColumnsFromIntactJSON(nodesJSON, defaultNodeTable);
                for (JsonNode node : nodesJSON) {
                    CyNode newNode = createIntactNode(network, node, nodeMap, nodeNameMap, xRefsTable);
                    if (newNode != null)
                        newNodes.add(newNode);
                }
            }
            if (edgesJSON.size() > 0) {
                createColumnsFromIntactJSON(edgesJSON, defaultEdgeTable);
                for (JsonNode edge : edgesJSON) {
                    createIntactEdge(network, edge, nodeMap, nodeNameMap, newEdges, featuresTable);
                }
            }

            return newNodes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void initTables(IntactNetwork intactNetwork, CyNetwork network, CyTable nodeTable, CyTable edgeTable, CyTableManager tableManager, CyTable featuresTable, CyTable xRefsTable) {
        CyTable networkTable = network.getDefaultNetworkTable();
        initNodeTable(nodeTable);
        initEdgeTable(edgeTable);
        initLowerTables(intactNetwork, network, tableManager, featuresTable, xRefsTable);
    }

    private static void initNodeTable(CyTable nodeTable) {
        for (String intactNodeColumn : Arrays.asList(INTACT_ID, PREFERRED_ID, PREFERRED_ID_DB, PREFERRED_ID_DB_MI_ID, TYPE, TYPE_MI_ID, SPECIES, FULL_NAME)) {
            createColumnIfNeeded(nodeTable, String.class, intactNodeColumn);
        }
        createColumnIfNeeded(nodeTable, Long.class, TAX_ID);
        createColumnIfNeeded(nodeTable, Boolean.class, MUTATION, false);
        createColumnIfNeeded(nodeTable, String.class, ELABEL_STYLE);
        createListColumnIfNeeded(nodeTable, String.class, FEATURES);
        createListColumnIfNeeded(nodeTable, String.class, IDENTIFIERS);
    }

    private static void initEdgeTable(CyTable edgeTable) {
        createColumnIfNeeded(edgeTable, Long.class, INTACT_ID);
        createColumnIfNeeded(edgeTable, String.class, INTERACTION_TYPE_MI_ID);
        createColumnIfNeeded(edgeTable, Double.class, MI_SCORE);
        for (String intactEdgeColumn : Arrays.asList(DETECTION_METHOD, DETECTION_METHOD_MI_ID, INTACT_AC, HOST_ORGANISM)) {
            createColumnIfNeeded(edgeTable, String.class, intactEdgeColumn);
        }
        createColumnIfNeeded(edgeTable, Long.class, HOST_ORGANISM_ID);

        for (String intactEdgeColumn : Arrays.asList(EXPANSION_TYPE, PUBMED_ID, SOURCE_BIOLOGICAL_ROLE, SOURCE_BIOLOGICAL_ROLE_MI_ID, TARGET_BIOLOGICAL_ROLE, TARGET_BIOLOGICAL_ROLE_MI_ID)) {
            createColumnIfNeeded(edgeTable, String.class, intactEdgeColumn);
        }

        createListColumnIfNeeded(edgeTable, String.class, SOURCE_FEATURES);
        createListColumnIfNeeded(edgeTable, String.class, TARGET_FEATURES);
        createColumnIfNeeded(edgeTable, Boolean.class, AFFECTED_BY_MUTATION, false);

        createColumnIfNeeded(edgeTable, Boolean.class, C_IS_COLLAPSED);
        createColumnIfNeeded(edgeTable, Integer.class, C_NB_EDGES);
        createListColumnIfNeeded(edgeTable, Long.class, C_INTACT_SUIDS);
        createListColumnIfNeeded(edgeTable, Long.class, C_INTACT_IDS);
    }

    private static void initLowerTables(IntactNetwork intactNetwork, CyNetwork network, CyTableManager tableManager, CyTable featuresTable, CyTable xRefsTable) {
        CyTable defaultNetworkTable = network.getDefaultNetworkTable();
        defaultNetworkTable.createColumn(FEATURES_TABLE_REF, Long.class, true);
        defaultNetworkTable.createColumn(IDENTIFIERS_TABLE_REF, Long.class, true);
        CyRow networkRow = defaultNetworkTable.getRow(network.getSUID());
        networkRow.set(FEATURES_TABLE_REF, featuresTable.getSUID());
        networkRow.set(IDENTIFIERS_TABLE_REF, xRefsTable.getSUID());

        initFeaturesTable(intactNetwork, tableManager, featuresTable);
        initIdentifierTable(intactNetwork, tableManager, xRefsTable);
    }

    private static void initFeaturesTable(IntactNetwork intactNetwork, CyTableManager tableManager, CyTable featuresTable) {
        tableManager.addTable(featuresTable);
        intactNetwork.setFeaturesTable(featuresTable);
        featuresTable.createColumn(NODE_REF, Long.class, true);
        for (String columnName : List.of(FEATURE_TYPE, FEATURE_TYPE_MI_ID, FEATURE_TYPE_MOD_ID, FEATURE_TYPE_PAR_ID, FEATURE_NAME)) {
            featuresTable.createColumn(columnName, String.class, true);
        }
        for (String columnName : List.of(FEATURE_EDGE_IDS, FEATURE_EDGE_SUIDS)) {
            featuresTable.createListColumn(columnName, Long.class, false);
        }
    }

    private static void initIdentifierTable(IntactNetwork intactNetwork, CyTableManager tableManager, CyTable xRefsTable) {
        tableManager.addTable(xRefsTable);
        intactNetwork.setIdentifiersTable(xRefsTable);
        xRefsTable.createColumn(NODE_REF, Long.class, true);
        for (String columnName : List.of(IDENTIFIER_ID, IDENTIFIER_DB_NAME, IDENTIFIER_DB_MI_ID, IDENTIFIER_QUALIFIER, IDENTIFIER_QUALIFIER_ID)) {
            xRefsTable.createColumn(columnName, String.class, true);
        }
    }

    public static void createColumnsFromIntactJSON(JsonNode nodes, CyTable table) {
        Map<String, Class<?>> jsonKeysClass = new HashMap<>();
        Set<String> listKeys = new HashSet<>();
        for (JsonNode nodeJSON : nodes) {
            nodeJSON.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (jsonKeysClass.containsKey(key)) {
                    return;
                }
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    jsonKeysClass.put(key, getJsonNodeValueClass(value.get(0)));
                    listKeys.add(key);
                } else {
                    jsonKeysClass.put(key, getJsonNodeValueClass(value));
                }
            });
        }
        List<String> jsonKeysSorted = new ArrayList<>(jsonKeysClass.keySet());
        Collections.sort(jsonKeysSorted);
        for (String jsonKey : jsonKeysSorted) {
            if (ignoreKeys.contains(jsonKey))
                continue;
            if (listKeys.contains(jsonKey)) {
                createListColumnIfNeeded(table, jsonKeysClass.get(jsonKey), jsonKey);
            } else {
                createColumnIfNeeded(table, jsonKeysClass.get(jsonKey), jsonKey);
            }
        }
    }

    private static Class<?> getJsonNodeValueClass(JsonNode valueNode) {
        if (valueNode.isBoolean())
            return Boolean.class;
        else if (valueNode.isDouble())
            return Double.class;
        else if (valueNode.isLong())
            return Long.class;
        else if (valueNode.isInt())
            return Integer.class;
        else if (valueNode.isTextual())
            return String.class;
        return String.class;
    }

    private static Object getJsonNodeValue(JsonNode valueNode) {
        if (valueNode.isBoolean())
            return valueNode.booleanValue();
        else if (valueNode.isDouble())
            return valueNode.booleanValue();
        else if (valueNode.isLong())
            return valueNode.longValue();
        else if (valueNode.isInt())
            return valueNode.intValue();
        else if (valueNode.isTextual())
            return valueNode.textValue();
        else if (valueNode.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode node : valueNode) {
                values.add(getJsonNodeValue(node));
            }
            return values;
        }
        return valueNode.asText();
    }


    private static CyNode createIntactNode(CyNetwork network, JsonNode nodeJSON, Map<String, CyNode> intactIdToNode, Map<String, String> nodeNameMap, CyTable xRefsTable) {

        String intactId = nodeJSON.get("id").textValue();

        if (intactIdToNode.containsKey(intactId))
            return null;

        CyNode newNode = network.addNode();
        CyRow row = network.getRow(newNode);
        String nodeName = nodeJSON.get("interactor_name").textValue();
        row.set(CyNetwork.NAME, nodeName);
        row.set(MUTATION, false);
        row.set(FEATURES, new ArrayList<>());
        row.set(IDENTIFIERS, new ArrayList<>());

        nodeJSON.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            switch (entry.getKey()) {
                case "id":
                    row.set(INTACT_ID, intactId);
                    break;
                case "type":
                    row.set(TYPE, value.textValue());
                    break;
                case "type_mi_identifier":
                    row.set(TYPE_MI_ID, value.textValue());
                    break;
                case "species":
                    row.set(SPECIES, value.textValue());
                    break;
                case "preferred_id":
                    row.set(PREFERRED_ID, value.textValue());
                    break;
                case "preferred_id_database_name":
                    row.set(PREFERRED_ID_DB, value.textValue());
                    break;
                case "preferred_id_database_mi_identifier":
                    row.set(PREFERRED_ID_DB_MI_ID, value.textValue());
                    break;
                case "full_name":
                    row.set(FULL_NAME, value.textValue());
                    break;
                case "taxId":
                    row.set(TAX_ID, value.longValue());
                    break;
                case "identifiers":
                    for (JsonNode xref : value) {
                        String xrefAc = xref.get("xref_ac").textValue();
                        String xrefId = xref.get("xref_id").textValue();
                        String primaryKey = xrefAc != null ? xrefAc : xrefId;
                        CyRow xRefRow = xRefsTable.getRow(primaryKey);
                        xRefRow.set(NODE_REF, newNode.getSUID());
                        xRefRow.set(IDENTIFIER_ID, xrefId);
                        xRefRow.set(IDENTIFIER_DB_NAME, xref.get("xref_database_name").textValue());
                        xRefRow.set(IDENTIFIER_DB_MI_ID, xref.get("xref_database_mi").textValue());

                        row.getList(IDENTIFIERS, String.class).add(primaryKey);
                    }
                case "interactor_name":
                case "label":
                case "type_mod_identifier":
                case "type_par_identifier":
                case "parent":
                    return;
                default:
                    row.set(entry.getKey(), getJsonNodeValue(value));

            }
        });

        row.set(ELABEL_STYLE, "label: attribute=\"name\" labelsize=12 labelAlignment=center outline=true outlineColor=black outlineTransparency=130 outlineWidth=5 background=false color=white dropShadow=false");

        intactIdToNode.put(intactId, newNode);
        nodeNameMap.put(intactId, nodeName);
        return newNode;
    }


    private static void createIntactEdge(CyNetwork network, JsonNode edgeJSON, Map<String, CyNode> nodeMap, Map<String, String> nodeNameMap, List<CyEdge> newEdges, CyTable featuresTable) {
        JsonNode source = edgeJSON.get("source");
        JsonNode target = edgeJSON.get("target");
        boolean selfInteracting = false;
        if (target == null || target.get("id").isNull()) {
            target = source;
            selfInteracting = true;
        }

        String sourceId = source.get("id").textValue();
        String targetId = target.get("id").textValue();

        CyNode sourceNode = nodeMap.get(sourceId);
        CyNode targetNode = nodeMap.get(targetId);

        CyEdge edge;
        edge = network.addEdge(sourceNode, targetNode, false);

        String type = edgeJSON.get("interaction_type").textValue();
        Long id = edgeJSON.get("id").longValue();

        CyRow row = network.getRow(edge);

        row.set(CyNetwork.NAME, nodeNameMap.get(sourceId) + " (" + type + ") " + nodeNameMap.get(targetId));
        row.set(CyEdge.INTERACTION, type);
        row.set(AFFECTED_BY_MUTATION, false);
        row.set(C_IS_COLLAPSED, false);
        row.set(INTACT_ID, id);
        fillParticipantData(featuresTable, network, source, sourceNode, edge, row, id, Position.SOURCE);
        if (!selfInteracting) {
            fillParticipantData(featuresTable, network, target, targetNode, edge, row, id, Position.TARGET);
        }

//        boolean isDisruptedByMutation = edgeJSON.get("disrupted_by_mutation").booleanValue();
//        if (isDisruptedByMutation) {
//            if (network.getRow(sourceNode).get(MUTATION, Boolean.class)) {
//                row.set(SOURCE_SHAPE, "Circle");
//            }
//            if (network.getRow(targetNode).get(MUTATION, Boolean.class)) {
//                row.set(TARGET_SHAPE, "Circle");
//            }
//        }

        if (newEdges != null)
            newEdges.add(edge);

        edgeJSON.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            switch (entry.getKey()) {
                case "source":
                case "target":
                case "interaction_type":
                case "id":
                    return;
                case "interaction_type_mi_identifier":
                    row.set(INTERACTION_TYPE_MI_ID, value.textValue());
                    break;
                case "interaction_detection_method":
                    row.set(DETECTION_METHOD, value.textValue());
                    break;
                case "interaction_detection_method_mi_identifier":
                    row.set(DETECTION_METHOD_MI_ID, value.textValue());
                    break;
                case "ac":
                    row.set(INTACT_AC, value.textValue());
                    break;
                case "mi_score":
                    row.set(MI_SCORE, value.doubleValue());
                    break;
                case "host_organism":
                    row.set(HOST_ORGANISM, value.textValue());
                    break;
                case "host_organism_tax_id":
                    row.set(HOST_ORGANISM_ID, value.longValue());
                    break;
                case "pubmed_id":
                    row.set(PUBMED_ID, value.textValue());
                    break;
                case "expansion_type":
                    row.set(EXPANSION_TYPE, value.textValue());
                    break;
                default:
                    row.set(entry.getKey(), getJsonNodeValue(value));
            }
        });
    }

    private enum Position {
        SOURCE(SOURCE_BIOLOGICAL_ROLE, SOURCE_BIOLOGICAL_ROLE_MI_ID),
        TARGET(TARGET_BIOLOGICAL_ROLE, TARGET_BIOLOGICAL_ROLE_MI_ID);

        final String biologicalRoleCol;
        final String biologicalRoleMIIdCol;

        Position(String biologicalRoleCol, String biologicalRoleMIIdCol) {
            this.biologicalRoleCol = biologicalRoleCol;
            this.biologicalRoleMIIdCol = biologicalRoleMIIdCol;
        }
    }

    private static void fillParticipantData(CyTable featuresTable, CyNetwork network, JsonNode participantJson, CyNode participantNode, CyEdge edge, CyRow edgeRow, Long edgeId, Position position) {
        edgeRow.set(position.biologicalRoleCol, participantJson.get("participant_biological_role_name").textValue());
        edgeRow.set(position.biologicalRoleMIIdCol, participantJson.get("participant_biological_role_mi_identifier").textValue());
        for (JsonNode feature : participantJson.get("participant_features")) {
            if (!feature.get("feature_type").isNull()) {
                String featureAc = feature.get("feature_ac").textValue();
                if (featureAc == null || featureAc.isBlank()) {
                    continue;
                }
                CyRow featureRow = featuresTable.getRow(featureAc);
                switch (position) {
                    case SOURCE:
                        addToListCellIfNotPresent(edgeRow, SOURCE_FEATURES, featureAc, String.class);
                        break;
                    case TARGET:
                        addToListCellIfNotPresent(edgeRow, TARGET_FEATURES, featureAc, String.class);
                        break;
                }
                addToListCellIfNotPresent(network.getRow(participantNode), FEATURES, featureAc, String.class);
                addToListCellIfNotPresent(featureRow, FEATURE_EDGE_IDS, edgeId, Long.class);
                addToListCellIfNotPresent(featureRow, FEATURE_EDGE_SUIDS, edge.getSUID(), Long.class);

                featureRow.set(NODE_REF, participantNode.getSUID());
                featureRow.set(FEATURE_NAME, feature.get("feature_name").textValue());
                featureRow.set(FEATURE_NAME, feature.get("feature_name").textValue());
                featureRow.set(FEATURE_TYPE, feature.get("feature_type").textValue());

                String feature_type_mi_identifier = feature.get("feature_type_mi_identifier").textValue();
                String feature_type_par_identifier = feature.get("feature_type_par_identifier").textValue();
                String feature_type_mod_identifier = feature.get("feature_type_mod_identifier").textValue();

                featureRow.set(FEATURE_TYPE_MI_ID, feature_type_mi_identifier);
                featureRow.set(FEATURE_TYPE_MOD_ID, feature_type_mod_identifier);
                featureRow.set(FEATURE_TYPE_PAR_ID, feature_type_par_identifier);

                OntologyIdentifier featureTypeId = TableUtil.getOntologyIdentifier(feature_type_mi_identifier, feature_type_mod_identifier, feature_type_par_identifier);
                if (featureTypeId.id != null) {
                    if (FeatureClassifier.mutation.contains(featureTypeId)) {
                        network.getRow(participantNode).set(MUTATION, true);
                        edgeRow.set(AFFECTED_BY_MUTATION, true);
                    }
                }
            }
        }
    }

    private static <E> List<E> getOrCreateList(CyRow row, String columnName, Class<E> elementsType) {
        List<E> list = row.getList(columnName, elementsType);
        if (list == null) {
            row.set(columnName, new ArrayList<>());
            list = row.getList(columnName, elementsType);
        }
        return list;
    }

    private static <E> boolean addToListCellIfNotPresent(CyRow row, String columnName, E elementToAdd, Class<E> elementsType) {
        List<E> list = getOrCreateList(row, columnName, elementsType);
        if (list.contains(elementToAdd)) {
            return false;
        } else {
            list.add(elementToAdd);
            return true;
        }
    }

    public static void buildIntactNetworkTableFromExistingOne(IntactNetwork iNetwork) {
        CyNetwork network = iNetwork.getNetwork();
        CyRow networkRow = network.getDefaultNetworkTable().getRow(network.getSUID());
        IntactManager manager = iNetwork.getManager();
        CyTableManager tableManager = manager.getService(CyTableManager.class);
        Long identifiersSUID = networkRow.get(IDENTIFIERS_TABLE_REF, Long.class);

        if (identifiersSUID != null) {
            iNetwork.setIdentifiersTable(tableManager.getTable(identifiersSUID));
            iNetwork.setFeaturesTable(tableManager.getTable(networkRow.get(FEATURES_TABLE_REF, Long.class)));
        } else {
            System.out.println("Identifiers and features SUID not found");
        }
    }


    //////////////////////////////////////////////////////////////////////

}
