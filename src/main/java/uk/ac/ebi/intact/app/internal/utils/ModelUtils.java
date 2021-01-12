package uk.ac.ebi.intact.app.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CySubNetwork;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.*;

import java.text.DateFormat;
import java.util.*;
import java.util.function.Supplier;

import static uk.ac.ebi.intact.app.internal.utils.TableUtil.*;

public class ModelUtils {

    public static final String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";


    public static List<CyNode> augmentNetworkFromJSON(Manager manager, Network network, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, JsonNode json) {
        return loadJSON(manager, network, network.getCyNetwork(), idToNode, idToName, newEdges, json, () -> false);
    }

    public static boolean isIntactNetwork(CyNetwork cyNetwork) {
        return cyNetwork != null &&
                NodeFields.AC.isDefinedIn(cyNetwork.getDefaultNodeTable()) &&
                EdgeFields.MI_SCORE.isDefinedIn(cyNetwork.getDefaultEdgeTable()) &&
                NetworkFields.EXPORTED.isDefinedIn(cyNetwork.getDefaultNetworkTable()) &&
                !NetworkFields.EXPORTED.getValue(cyNetwork.getRow(cyNetwork));
    }

    // This method will tell us if we have the new side panel functionality (i.e. namespaces)
    public static boolean ifHaveIntactNS(CyNetwork network) {
        if (network == null) return false;
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(Field.Namespace.INTACT.name);
        return columns != null && columns.size() > 0;
    }


    /////////////////////////////////////////////////////////////////////

    public static CyNetwork createIntactNetworkFromJSON(Network network, JsonNode object, String netName, Supplier<Boolean> isCancelled) {
        Manager manager = network.manager;

        // Get a network name
        netName = getNetworkName(netName);

        // Create the network
        CyNetwork cyNetwork = manager.data.createNetwork(netName);

        // Create a map to save the nodes
        Map<String, CyNode> nodeMap = new HashMap<>();

        // Create a map to save the node names
        Map<String, String> nodeNameMap = new HashMap<>();

        loadJSON(manager, network, cyNetwork, nodeMap, nodeNameMap, null, object, isCancelled);

        return cyNetwork;
    }

    private static String getNetworkName(String netName) {
        if (netName != null && !netName.isBlank()) return netName;
        Date date = new Date();
        return "IntAct Network - " + DateFormat.getDateInstance(DateFormat.SHORT).format(date) + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
    }

    public static List<CyNode> loadJSON(Manager manager, Network network, CyNetwork cyNetwork, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, JsonNode json, Supplier<Boolean> isCancelled) {
        if (json == null) {
            manager.utils.error("IntAct servers did not respond");
            return new ArrayList<>();
        }
        try {
            CyTable nodeTable = cyNetwork.getDefaultNodeTable();
            CyTable edgeTable = cyNetwork.getDefaultEdgeTable();

            String networkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);

            CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
            CyTableFactory tableFactory = manager.utils.getService(CyTableFactory.class);

            CyTable featuresTable = network.getFeaturesTable();
            if (featuresTable == null) {
                featuresTable = tableFactory.createTable("Features of " + networkName, FeatureFields.AC.toString(), String.class, true, true);
                tableManager.addTable(featuresTable);
            }

            CyTable identifiersTable = network.getIdentifiersTable();
            if (identifiersTable == null) {
                identifiersTable = tableFactory.createTable("Identifiers of " + networkName, IdentifierFields.AC.toString(), String.class, true, true);
                tableManager.addTable(identifiersTable);
            }

            initTables(network, cyNetwork, cyNetwork.getDefaultNetworkTable(), nodeTable, edgeTable, featuresTable, identifiersTable);

            if (isCancelled.get()) {
                tableManager.deleteTable(featuresTable.getSUID());
                tableManager.deleteTable(identifiersTable.getSUID());
                return new ArrayList<>();
            }

            JsonNode nodesJSON = json.get("nodes");
            JsonNode edgesJSON = json.get("edges");

            List<CyNode> nodes = new ArrayList<>();
            if (nodesJSON.size() > 0) {
                createUnknownColumnsFromIntactJSON(nodesJSON, nodeTable);
                for (JsonNode node : nodesJSON) {
                    nodes.add(createNode(cyNetwork, node, idToNode, idToName, identifiersTable));
                    if (isCancelled.get()) {
                        tableManager.deleteTable(featuresTable.getSUID());
                        tableManager.deleteTable(identifiersTable.getSUID());
                        return nodes;
                    }
                }
            }
            if (edgesJSON.size() > 0) {
                createUnknownColumnsFromIntactJSON(edgesJSON, edgeTable);
                for (JsonNode edge : edgesJSON) {
                    createEdge(cyNetwork, edge, idToNode, idToName, newEdges, featuresTable);
                    if (isCancelled.get()) {
                        tableManager.deleteTable(featuresTable.getSUID());
                        tableManager.deleteTable(identifiersTable.getSUID());
                        return nodes;
                    }
                }
            }
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void initTables(Network network, CyNetwork cyNetwork, CyTable networkTable, CyTable nodeTable, CyTable edgeTable, CyTable featuresTable, CyTable xRefsTable) {
        Table.NETWORK.initColumns(networkTable, cyNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));
        Table.NODE.initColumns(nodeTable, cyNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
        Table.EDGE.initColumns(edgeTable, cyNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
        initLowerTables(network, cyNetwork, networkTable, featuresTable, xRefsTable);
    }

    private static void initLowerTables(Network network, CyNetwork cyNetwork, CyTable networkTable, CyTable featuresTable, CyTable identifiersTable) {
        CyRow networkRow = networkTable.getRow(cyNetwork.getSUID());
        String uuid = UUID.randomUUID().toString();
        NetworkFields.EXPORTED.setValue(networkRow, false);
        NetworkFields.UUID.setValue(networkRow, uuid);
        NetworkFields.FEATURES_TABLE_REF.setValue(networkRow, featuresTable.getSUID());
        NetworkFields.IDENTIFIERS_TABLE_REF.setValue(networkRow, identifiersTable.getSUID());
        initFeaturesTable(network, uuid, featuresTable);
        initIdentifierTable(network, uuid, identifiersTable);
    }

    private static void initFeaturesTable(Network network, String networkUUID, CyTable featuresTable) {
        featuresTable.createColumn(NetworkFields.UUID.toString(), String.class, true, networkUUID);
        network.setFeaturesTable(featuresTable);
        Table.FEATURE.initColumns(featuresTable, featuresTable);
    }

    private static void initIdentifierTable(Network network, String networkUUID, CyTable identifiersTable) {
        identifiersTable.createColumn(NetworkFields.UUID.toString(), String.class, true, networkUUID);
        network.setIdentifiersTable(identifiersTable);
        Table.IDENTIFIER.initColumns(identifiersTable, identifiersTable);
    }

    public static void createUnknownColumnsFromIntactJSON(JsonNode elements, CyTable table) {
        Map<String, Class<?>> columnToType = new HashMap<>();
        Set<String> listKeys = new HashSet<>();
        for (JsonNode element : elements) {
            element.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (columnToType.containsKey(key)) return;
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    columnToType.put(key, getJsonNodeValueClass(value.get(0)));
                    listKeys.add(key);
                } else {
                    columnToType.put(key, getJsonNodeValueClass(value));
                }
            });
        }

        List<String> jsonKeysSorted = new ArrayList<>(columnToType.keySet());
        Collections.sort(jsonKeysSorted);
        for (String jsonKey : jsonKeysSorted) {
            if (Field.keys.contains(jsonKey)) continue;
            if (listKeys.contains(jsonKey)) {
                createListColumnIfNeeded(table, columnToType.get(jsonKey), jsonKey);
            } else {
                createColumnIfNeeded(table, columnToType.get(jsonKey), jsonKey);
            }
        }
    }

    private static Class<?> getJsonNodeValueClass(JsonNode valueNode) {
        if (valueNode.isBoolean()) return Boolean.class;
        else if (valueNode.isDouble()) return Double.class;
        else if (valueNode.isLong()) return Long.class;
        else if (valueNode.isInt()) return Integer.class;
        else if (valueNode.isTextual()) return String.class;
        return String.class;
    }

    private static Object getJsonNodeValue(JsonNode valueNode) {
        if (valueNode.isBoolean()) return valueNode.booleanValue();
        else if (valueNode.isDouble()) return valueNode.booleanValue();
        else if (valueNode.isLong()) return valueNode.longValue();
        else if (valueNode.isInt()) return valueNode.intValue();
        else if (valueNode.isTextual()) return valueNode.textValue();
        else if (valueNode.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode node : valueNode) {
                values.add(getJsonNodeValue(node));
            }
            return values;
        }
        return valueNode.asText();
    }


    private static CyNode createNode(CyNetwork cyNetwork, JsonNode nodeJSON, Map<String, CyNode> idToNode, Map<String, String> idToName, CyTable xRefsTable) {
        String intactId = nodeJSON.get("id").textValue();

        if (idToNode.containsKey(intactId)) return idToNode.get(intactId);

        CyNode newNode = cyNetwork.addNode();
        CyRow nodeRow = cyNetwork.getRow(newNode);

        Table.NODE.setRowFromJson(nodeRow, nodeJSON);

        for (JsonNode xref : nodeJSON.get("identifiers")) {
            String xrefAc = xref.get("xref_ac").textValue();
            String primaryKey = xrefAc != null ? xrefAc : xref.get("xref_id").textValue();

            Table.IDENTIFIER.setRowFromJson(xRefsTable.getRow(primaryKey), xref);
            NodeFields.IDENTIFIERS.addValue(nodeRow, primaryKey);
        }

        nodeJSON.fields().forEachRemaining(entry -> {
            if (Table.NODE.keysToIgnore.contains(entry.getKey())) return;
            nodeRow.set(entry.getKey(), getJsonNodeValue(entry.getValue()));
        });

        idToNode.put(intactId, newNode);
        idToName.put(intactId, NodeFields.NAME.getValue(nodeRow));
        return newNode;
    }


    private static void createEdge(CyNetwork network, JsonNode edgeJSON, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, CyTable featuresTable) {
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

        CyRow edgeRow = network.getRow(edge);

        EdgeFields.NAME.setValue(edgeRow, idToName.get(sourceId) + " (" + type + ") " + idToName.get(targetId));
        edgeRow.set(CyEdge.INTERACTION, type);

        Table.EDGE.setRowFromJson(edgeRow, edgeJSON);

        edgeJSON.fields().forEachRemaining(entry -> {
            if (Table.EDGE.keysToIgnore.contains(entry.getKey())) return;
            edgeRow.set(entry.getKey(), getJsonNodeValue(entry.getValue()));
        });

        buildFeatures(featuresTable, network, source, sourceNode, edge, edgeRow, Position.SOURCE);
        if (!selfInteracting) {
            buildFeatures(featuresTable, network, target, targetNode, edge, edgeRow, Position.TARGET);
        }

        if (newEdges != null) newEdges.add(edge);
    }

    public enum Position {
        SOURCE,
        TARGET;

    }

    private static void buildFeatures(CyTable featuresTable, CyNetwork network, JsonNode participantJson, CyNode participantNode, CyEdge edge, CyRow edgeRow, Position position) {
        for (JsonNode feature : participantJson.get("participant_features")) {
            if (!feature.get("feature_type").isNull()) {
                String featureAc = feature.get("feature_ac").textValue();
                if (featureAc == null || featureAc.isBlank()) {
                    continue;
                }

                CyRow featureRow = featuresTable.getRow(featureAc);
                Table.FEATURE.setRowFromJson(featureRow, feature);
                FeatureFields.EDGES_SUID.addValue(featureRow, edge.getSUID());

                switch (position) {
                    case SOURCE:
                        EdgeFields.FEATURES.SOURCE.addValue(edgeRow, featureAc);
                        break;
                    case TARGET:
                        EdgeFields.FEATURES.TARGET.addValue(edgeRow, featureAc);
                        break;
                }
                NodeFields.FEATURES.addValueIfAbsent(network.getRow(participantNode), featureAc);


                OntologyIdentifier featureTypeId = getOntologyIdentifier(featureRow, FeatureFields.TYPE_MI_ID, FeatureFields.TYPE_MOD_ID, FeatureFields.TYPE_PAR_ID);
                if (featureTypeId.id != null && FeatureClassifier.mutation.contains(featureTypeId)) {
                    NodeFields.MUTATED.setValue(network.getRow(participantNode), true);
                    EdgeFields.AFFECTED_BY_MUTATION.setValue(edgeRow, true);
                }
            }
        }
    }

    public static void linkNetworkTablesFromTableData(Network network) {
        CyNetwork cyNetwork = network.getCyNetwork();
        CyRow networkRow = cyNetwork.getDefaultNetworkTable().getRow(cyNetwork.getSUID());
        Manager manager = network.manager;
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);

        Long identifiersSUID = NetworkFields.IDENTIFIERS_TABLE_REF.getValue(networkRow);
        if (identifiersSUID != null) {
            CyTable identifierTable = tableManager.getTable(identifiersSUID);
            if (identifierTable != null) network.setIdentifiersTable(identifierTable);
            else System.out.println("Identifier table not found");
        } else System.out.println("Identifiers SUID not found");

        Long featuresSUID = NetworkFields.FEATURES_TABLE_REF.getValue(networkRow);
        if (featuresSUID != null) {
            CyTable featureTable = tableManager.getTable(featuresSUID);
            if (featureTable != null) network.setFeaturesTable(featureTable);
            else System.out.println("Feature table not found");
        } else System.out.println("Features SUID not found");
    }

    public static void handleSubNetworkEdges(CySubNetwork subCyNetwork, Network parentNetwork) {
        CyTable edgeTable = subCyNetwork.getDefaultEdgeTable();

        Map<Boolean, List<CyRow>> isSummaryRows = EdgeFields.IS_SUMMARY.groupRows(edgeTable);
        if (isSummaryRows.containsKey(true)) {
            for (CyRow summaryEdgeRow : isSummaryRows.get(true)) {
                for (Long summarizedEdgeSUID : EdgeFields.SUMMARIZED_EDGES_SUID.getValue(summaryEdgeRow)) {
                    if (!edgeTable.rowExists(summarizedEdgeSUID)) {
                        CyEdge edge = parentNetwork.getCyNetwork().getEdge(summarizedEdgeSUID);
                        if (edge != null) subCyNetwork.addEdge(edge);
                    }
                }
            }
        }

        if (isSummaryRows.containsKey(false)) {
            for (CyRow evidenceEdgeRow : isSummaryRows.get(false)) {
                subCyNetwork.addEdge(getSummaryCyEdge(parentNetwork, evidenceEdgeRow));
            }
        }

    }

    public static CyEdge getSummaryCyEdge(Network network, CyRow evidenceEdgeRow) {
        return network.getSummaryEdge(network.getCyEdge(network.getSUID(evidenceEdgeRow))).cyEdge;
    }

    public static CySubNetwork getParentCyNetwork(final CySubNetwork net, final Manager manager) {
        final CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
        final CyRow row = hiddenTable != null ? hiddenTable.getRow(net.getSUID()) : null;
        final Long suid = row != null ? row.get(PARENT_NETWORK_COLUMN, Long.class) : null;

        if (suid != null) {
            final CyNetwork parent = manager.utils.getService(CyNetworkManager.class).getNetwork(suid);
            if (parent instanceof CySubNetwork) return (CySubNetwork) parent;
        }

        return null;
    }
}
