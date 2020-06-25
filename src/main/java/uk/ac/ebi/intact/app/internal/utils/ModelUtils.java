package uk.ac.ebi.intact.app.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

import java.util.*;
import java.util.stream.Collectors;

public class ModelUtils {

    // Namespaces
    public static String INTACTDB_NAMESPACE = "IntAct Database";
    public static String COLLAPSED_NAMESPACE = "Collapsed";
    public static String FEATURE_NAMESPACE = "Feature";
    public static String IDENTIFIER_NAMESPACE = "Identifier";
    public static String NAMESPACE_SEPARATOR = "::";

    // Network tables column
    public static final String NET_FEATURES_TABLE_REF = "Features.SUID";
    public static final String NET_IDENTIFIERS_TABLE_REF = "Identifiers.SUID";
    public static final String NET_UUID = "UUID";
    public static final String NET_VIEW_STATE = "View::Data";

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

    public static String SPECIES = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "species";
    public static String TYPE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Interactor type";
    public static String TYPE_MI_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "Interactor type mi id";

    public static List<String> ignoreKeys = new ArrayList<>(Arrays.asList("@id", "description",
            "id", "preferred_id", "preferred_id_database_name", "preferred_id_database_mi_identifier", "type", "type_mi_id", "species", "interactor_name", "label", "taxId",
            "source", "target", "ac", "interaction_detection_method", "interaction_type", "mi_score", "disrupted_by_mutation",
            "expansion_type", "host_organism", "host_organism_tax_id", "interaction_detection_method_mi_identifier", "interaction_type_mi_identifier",
            "type_mi_identifier", "type_mod_identifier", "type_par_identifier", "identifiers", "pubmed_id", "full_name"
    ));

    public static List<CyNode> augmentNetworkFromJSON(Manager manager, Network network, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, JsonNode json) {
        return loadJSON(manager, network, network.getCyNetwork(), idToNode, idToName, newEdges, json);
    }

    public static boolean isMergedIntactNetwork(CyNetwork network) {
        CyTable nodeTable = network.getDefaultNodeTable();
        if (nodeTable.getColumn(INTACT_ID) == null)
            return false;
        CyTable edgeTable = network.getDefaultEdgeTable();
        return edgeTable.getColumn(MI_SCORE) != null;
    }

    public static boolean isIntactNetwork(CyNetwork network) {
        return isMergedIntactNetwork(network);
    }

    // This method will tell us if we have the new side panel functionality (i.e. namespaces)
    public static boolean ifHaveIntactNS(CyNetwork network) {
        if (network == null) return false;
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(INTACTDB_NAMESPACE);
        return columns != null && columns.size() > 0;
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


    public static void copyRow(CyTable fromTable, CyTable toTable, java.io.Serializable fromPrimaryKey, java.io.Serializable toPrimaryKey, Set<String> columnNamesToExclude) {
        Map<String, Class<?>> fromColumnNames = fromTable.getColumns().stream().filter(column -> !columnNamesToExclude.contains(column.getName())).collect(Collectors.toMap(CyColumn::getName, CyColumn::getType));
        Map<String, Class<?>> toColumnNames = toTable.getColumns().stream().filter(column -> !columnNamesToExclude.contains(column.getName())).collect(Collectors.toMap(CyColumn::getName, CyColumn::getType));
        if (!toColumnNames.keySet().containsAll(fromColumnNames.keySet())) return;
        if (fromTable.getPrimaryKey().getType() != fromPrimaryKey.getClass()) return;
        if (toTable.getPrimaryKey().getType() != toPrimaryKey.getClass()) return;
        CyRow fromRow = fromTable.getRow(fromPrimaryKey);
        CyRow toRow = toTable.getRow(toPrimaryKey);
        fromColumnNames.forEach((columnName, type) -> toRow.set(columnName, type != List.class ? fromRow.get(columnName, type) : fromRow.getList(columnName, fromTable.getColumn(columnName).getListElementType())));
    }

    /////////////////////////////////////////////////////////////////////

    public static CyNetwork createIntactNetworkFromJSON(Network network, JsonNode object, String netName) {
        Manager manager = network.getManager();

        // Get a network name
        netName = getNetworkName(network, netName);

        // Create the network
        CyNetwork newNetwork = manager.data.createNetwork(netName);

        // Create a map to save the nodes
        Map<String, CyNode> nodeMap = new HashMap<>();

        // Create a map to save the node names
        Map<String, String> nodeNameMap = new HashMap<>();

        loadJSON(manager, network, newNetwork, nodeMap, nodeNameMap, null, object);

        return newNetwork;
    }

    private static String getNetworkName(Network network, String netName) {
        String defaultName = "IntAct Network";

        if (netName != null && !netName.isBlank()) return netName;
        Map<String, List<Interactor>> interactorsToResolve = network.getInteractorsToResolve();

        if (interactorsToResolve == null) return defaultName;
        Set<String> terms = interactorsToResolve.keySet();

        if (terms.size() == 1) return defaultName + " - " + terms.iterator().next();

        return defaultName;
    }

    public static List<CyNode> loadJSON(Manager manager, Network network, CyNetwork cyNetwork, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, JsonNode json) {
        try {
            CyTable defaultNodeTable = cyNetwork.getDefaultNodeTable();
            CyTable defaultEdgeTable = cyNetwork.getDefaultEdgeTable();

            String networkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);

            CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
            CyTableFactory tableFactory = manager.utils.getService(CyTableFactory.class);

            CyTable featuresTable = network.getFeaturesTable();
            if (featuresTable == null) {
                featuresTable = tableFactory.createTable("Features of " + networkName, FEATURE_AC, String.class, true, true);
            }

            CyTable identifiersTable = network.getIdentifiersTable();
            if (identifiersTable == null) {
                identifiersTable = tableFactory.createTable("Identifiers of " + networkName, IDENTIFIER_AC, String.class, true, true);
            }

            initTables(network, cyNetwork, defaultNodeTable, defaultEdgeTable, tableManager, featuresTable, identifiersTable);

            JsonNode nodesJSON = json.get("nodes");
            JsonNode edgesJSON = json.get("edges");

            List<CyNode> nodes = new ArrayList<>();
            if (nodesJSON.size() > 0) {
                createColumnsFromIntactJSON(nodesJSON, defaultNodeTable);
                for (JsonNode node : nodesJSON) {
                    nodes.add(createIntactNode(cyNetwork, node, idToNode, idToName, identifiersTable));
                }
            }
            if (edgesJSON.size() > 0) {
                createColumnsFromIntactJSON(edgesJSON, defaultEdgeTable);
                for (JsonNode edge : edgesJSON) {
                    createIntactEdge(cyNetwork, edge, idToNode, idToName, newEdges, featuresTable);
                }
            }
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void initTables(Network network, CyNetwork cyNetwork, CyTable nodeTable, CyTable edgeTable, CyTableManager tableManager, CyTable featuresTable, CyTable xRefsTable) {
        initNetworkTable(cyNetwork.getDefaultNetworkTable());
        initNodeTable(nodeTable);
        initEdgeTable(edgeTable);
        initLowerTables(network, cyNetwork, tableManager, featuresTable, xRefsTable);
    }

    private static void initNetworkTable(CyTable networkTable) {
        createColumnIfNeeded(networkTable, String.class, NET_VIEW_STATE);
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

    private static void initLowerTables(Network network, CyNetwork cyNetwork, CyTableManager tableManager, CyTable featuresTable, CyTable identifiersTable) {
        CyTable networkTable = cyNetwork.getDefaultNetworkTable();
        createColumnIfNeeded(networkTable, Long.class, NET_FEATURES_TABLE_REF);
        createColumnIfNeeded(networkTable, Long.class, NET_IDENTIFIERS_TABLE_REF);
        createColumnIfNeeded(networkTable, String.class, NET_UUID);
        CyRow networkRow = networkTable.getRow(cyNetwork.getSUID());
        UUID uuid = UUID.randomUUID();
        networkRow.set(NET_UUID, uuid.toString());
        networkRow.set(NET_FEATURES_TABLE_REF, featuresTable.getSUID());
        networkRow.set(NET_IDENTIFIERS_TABLE_REF, identifiersTable.getSUID());
        initFeaturesTable(network, uuid, tableManager, featuresTable);
        initIdentifierTable(network, uuid, tableManager, identifiersTable);
    }

    private static void initFeaturesTable(Network network, UUID networkUUID, CyTableManager tableManager, CyTable featuresTable) {
        tableManager.addTable(featuresTable);
        featuresTable.createColumn(NET_UUID, String.class, true, networkUUID.toString());
        network.setFeaturesTable(featuresTable);
        for (String columnName : List.of(FEATURE_TYPE, FEATURE_TYPE_MI_ID, FEATURE_TYPE_MOD_ID, FEATURE_TYPE_PAR_ID, FEATURE_NAME)) {
            createColumnIfNeeded(featuresTable, String.class, columnName);
        }
        for (String columnName : List.of(FEATURE_EDGE_IDS, FEATURE_EDGE_SUIDS)) {
            createListColumnIfNeeded(featuresTable, Long.class, columnName);
        }
    }

    private static void initIdentifierTable(Network network, UUID networkUUID, CyTableManager tableManager, CyTable identifiersTable) {
        tableManager.addTable(identifiersTable);
        identifiersTable.createColumn(NET_UUID, String.class, true, networkUUID.toString());
        network.setIdentifiersTable(identifiersTable);
        for (String columnName : List.of(IDENTIFIER_ID, IDENTIFIER_DB_NAME, IDENTIFIER_DB_MI_ID, IDENTIFIER_QUALIFIER, IDENTIFIER_QUALIFIER_ID)) {
            createColumnIfNeeded(identifiersTable, String.class, columnName);
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


    private static CyNode createIntactNode(CyNetwork cyNetwork, JsonNode nodeJSON, Map<String, CyNode> idToNode, Map<String, String> idToName, CyTable xRefsTable) {

        String intactId = nodeJSON.get("id").textValue();

        if (idToNode.containsKey(intactId))
            return idToNode.get(intactId);

        CyNode newNode = cyNetwork.addNode();
        CyRow row = cyNetwork.getRow(newNode);
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

        idToNode.put(intactId, newNode);
        idToName.put(intactId, nodeName);
        return newNode;
    }


    private static void createIntactEdge(CyNetwork network, JsonNode edgeJSON, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, CyTable featuresTable) {
        JsonNode source = edgeJSON.get("source");
        JsonNode target = edgeJSON.get("target");
        boolean selfInteracting = false;
        if (target == null || target.get("id").isNull()) {
            target = source;
            selfInteracting = true;
        }

        String sourceId = source.get("id").textValue();
        String targetId = target.get("id").textValue();

        CyNode sourceNode = idToNode.get(sourceId);
        CyNode targetNode = idToNode.get(targetId);

        if (sourceNode == null || targetNode == null) {
            return;
        }
        CyEdge edge = network.addEdge(sourceNode, targetNode, false);

        String type = edgeJSON.get("interaction_type").textValue();
        Long id = edgeJSON.get("id").longValue();

        CyRow row = network.getRow(edge);

        row.set(CyNetwork.NAME, idToName.get(sourceId) + " (" + type + ") " + idToName.get(targetId));
        row.set(CyEdge.INTERACTION, type);
        row.set(AFFECTED_BY_MUTATION, false);
        row.set(C_IS_COLLAPSED, false);
        row.set(INTACT_ID, id);
        fillParticipantData(featuresTable, network, source, sourceNode, edge, row, id, Position.SOURCE);
        if (!selfInteracting) {
            fillParticipantData(featuresTable, network, target, targetNode, edge, row, id, Position.TARGET);
        }

        if (newEdges != null) newEdges.add(edge);

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

    private static <E> void addToListCellIfNotPresent(CyRow row, String columnName, E elementToAdd, Class<E> elementsType) {
        List<E> list = getOrCreateList(row, columnName, elementsType);
        if (!list.contains(elementToAdd)) {
            list.add(elementToAdd);
        }
    }

    public static void buildIntactNetworkTableFromExistingOne(Network network) {
        CyNetwork cyNetwork = network.getCyNetwork();
        CyRow networkRow = cyNetwork.getDefaultNetworkTable().getRow(cyNetwork.getSUID());
        Manager manager = network.getManager();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        Long identifiersSUID = networkRow.get(NET_IDENTIFIERS_TABLE_REF, Long.class);

        if (identifiersSUID != null) {
            network.setIdentifiersTable(tableManager.getTable(identifiersSUID));
            network.setFeaturesTable(tableManager.getTable(networkRow.get(NET_FEATURES_TABLE_REF, Long.class)));
        } else {
            System.out.println("Identifiers and features SUID not found");
        }
    }

    public static void buildSubTablesForSubIntactNetwork(Network subNetwork, Network parentNetwork) {
        Manager manager = subNetwork.getManager();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        CyTableFactory tableFactory = manager.utils.getService(CyTableFactory.class);

        CyTable featuresTable = tableFactory.createTable("Features of " + subNetwork.toString(), FEATURE_AC, String.class, true, true);
        CyTable identifiersTable = tableFactory.createTable("Identifiers of " + subNetwork.toString(), IDENTIFIER_AC, String.class, true, true);

        initLowerTables(subNetwork, subNetwork.getCyNetwork(), tableManager, featuresTable, identifiersTable);

        // Copy included features
        Set<String> featuresAcsToAdd = new HashSet<>();
        CyTable edgeTable = subNetwork.getCyNetwork().getDefaultEdgeTable();
        edgeTable.getColumn(SOURCE_FEATURES).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> featuresAcsToAdd.add((String) o));
        });
        edgeTable.getColumn(TARGET_FEATURES).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> featuresAcsToAdd.add((String) o));
        });

        for (String featureAc : featuresAcsToAdd) {
            copyRow(parentNetwork.getFeaturesTable(), featuresTable, featureAc, featureAc, Set.of(NET_UUID));
        }

        // Copy included identifiers
        CyTable nodeTable = subNetwork.getCyNetwork().getDefaultNodeTable();
        Set<String> identifierAcsToAdd = new HashSet<>();
        nodeTable.getColumn(IDENTIFIERS).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> identifierAcsToAdd.add((String) o));
        });

        for (String identifierAc : identifierAcsToAdd) {
            copyRow(parentNetwork.getIdentifiersTable(), identifiersTable, identifierAc, identifierAc, Set.of(NET_UUID));
        }

        // Remove excluded features and identifiers from nodes
        for (CyRow nodeRow : nodeTable.getAllRows()) {
            nodeRow.getList(FEATURES, String.class).removeIf(ac -> !featuresTable.rowExists(ac));
            nodeRow.getList(IDENTIFIERS, String.class).removeIf(ac -> !identifiersTable.rowExists(ac));
        }

        // Remove excluded edges from features
        for (CyRow featureRow : featuresTable.getAllRows()) {
            Set<Long> idsToKeep = new HashSet<>();
            featureRow.getList(FEATURE_EDGE_SUIDS, Long.class).removeIf(suid -> {
                boolean edgeExistInSubNetwork = edgeTable.rowExists(suid);
                if (edgeExistInSubNetwork) idsToKeep.add(edgeTable.getRow(suid).get(INTACT_ID, Long.class));
                return !edgeExistInSubNetwork;
            });

            featureRow.getList(FEATURE_EDGE_IDS, Long.class).removeIf(id -> !idsToKeep.contains(id));
        }
    }
}
