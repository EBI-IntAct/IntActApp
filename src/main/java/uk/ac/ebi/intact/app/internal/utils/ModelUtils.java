package uk.ac.ebi.intact.app.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.*;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.TableUtil.*;

public class ModelUtils {

    public static List<CyNode> augmentNetworkFromJSON(Manager manager, Network network, Map<String, CyNode> idToNode, Map<String, String> idToName, List<CyEdge> newEdges, JsonNode json) {
        return loadJSON(manager, network, network.getCyNetwork(), idToNode, idToName, newEdges, json);
    }

    public static boolean isIntactNetwork(CyNetwork network) {
        return NodeFields.AC.isDefinedIn(network.getDefaultNodeTable()) && EdgeFields.MI_SCORE.isDefinedIn(network.getDefaultEdgeTable());
    }

    // This method will tell us if we have the new side panel functionality (i.e. namespaces)
    public static boolean ifHaveIntactNS(CyNetwork network) {
        if (network == null) return false;
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(Field.Namespace.INTACT.name);
        return columns != null && columns.size() > 0;
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
        if (json == null) {
            manager.utils.error("IntAct servers did not respond");
            return new ArrayList<>();
        }
        try {
            CyTable defaultNodeTable = cyNetwork.getDefaultNodeTable();
            CyTable defaultEdgeTable = cyNetwork.getDefaultEdgeTable();

            String networkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);

            CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
            CyTableFactory tableFactory = manager.utils.getService(CyTableFactory.class);

            CyTable featuresTable = network.getFeaturesTable();
            if (featuresTable == null) {
                featuresTable = tableFactory.createTable("Features of " + networkName, FeatureFields.AC.toString(), String.class, true, true);
            }

            CyTable identifiersTable = network.getIdentifiersTable();
            if (identifiersTable == null) {
                identifiersTable = tableFactory.createTable("Identifiers of " + networkName, IdentifierFields.AC.toString(), String.class, true, true);
            }

            initTables(network, cyNetwork, cyNetwork.getDefaultNetworkTable(), defaultNodeTable, defaultEdgeTable, tableManager, featuresTable, identifiersTable);

            JsonNode nodesJSON = json.get("nodes");
            JsonNode edgesJSON = json.get("edges");

            List<CyNode> nodes = new ArrayList<>();
            if (nodesJSON.size() > 0) {
                createUnknownColumnsFromIntactJSON(nodesJSON, defaultNodeTable);
                for (JsonNode node : nodesJSON) {
                    nodes.add(createNode(cyNetwork, node, idToNode, idToName, identifiersTable));
                }
            }
            if (edgesJSON.size() > 0) {
                createUnknownColumnsFromIntactJSON(edgesJSON, defaultEdgeTable);
                for (JsonNode edge : edgesJSON) {
                    createEdge(cyNetwork, edge, idToNode, idToName, newEdges, featuresTable);
                }
            }
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void initTables(Network network, CyNetwork cyNetwork, CyTable networkTable, CyTable nodeTable, CyTable edgeTable, CyTableManager tableManager, CyTable featuresTable, CyTable xRefsTable) {
        System.out.println(NetworkFields.UUID); //Do not suppress: Allow NetworkFields to be loaded and so to populate Table.NETWORK.fields
        Table.NETWORK.initTable(networkTable);
        Table.NODE.initTable(nodeTable);
        Table.EDGE.initTable(edgeTable);
        initLowerTables(network, cyNetwork, tableManager, networkTable, featuresTable, xRefsTable);
    }

    private static void initLowerTables(Network network, CyNetwork cyNetwork, CyTableManager tableManager, CyTable networkTable, CyTable featuresTable, CyTable identifiersTable) {
        CyRow networkRow = networkTable.getRow(cyNetwork.getSUID());
        String uuid = UUID.randomUUID().toString();
        NetworkFields.UUID.setValue(networkRow, uuid);
        NetworkFields.FEATURES_TABLE_REF.setValue(networkRow, featuresTable.getSUID());
        NetworkFields.IDENTIFIERS_TABLE_REF.setValue(networkRow, identifiersTable.getSUID());
        initFeaturesTable(network, uuid, tableManager, featuresTable);
        initIdentifierTable(network, uuid, tableManager, identifiersTable);
    }

    private static void initFeaturesTable(Network network, String networkUUID, CyTableManager tableManager, CyTable featuresTable) {
        tableManager.addTable(featuresTable);
        featuresTable.createColumn(NetworkFields.UUID.toString(), String.class, true, networkUUID);
        network.setFeaturesTable(featuresTable);
        Table.FEATURE.initTable(featuresTable);
    }

    private static void initIdentifierTable(Network network, String networkUUID, CyTableManager tableManager, CyTable identifiersTable) {
        tableManager.addTable(identifiersTable);
        identifiersTable.createColumn(NetworkFields.UUID.toString(), String.class, true, networkUUID);
        network.setIdentifiersTable(identifiersTable);
        Table.IDENTIFIER.initTable(identifiersTable);
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

        Long id = edgeJSON.get("id").longValue();
        String type = edgeJSON.get("interaction_type").textValue();

        CyRow edgeRow = network.getRow(edge);

        EdgeFields.NAME.setValue(edgeRow, idToName.get(sourceId) + " (" + type + ") " + idToName.get(targetId));
        edgeRow.set(CyEdge.INTERACTION, type);

        Table.EDGE.setRowFromJson(edgeRow, edgeJSON);

        edgeJSON.fields().forEachRemaining(entry -> {
            if (Table.EDGE.keysToIgnore.contains(entry.getKey())) return;
            edgeRow.set(entry.getKey(), getJsonNodeValue(entry.getValue()));
        });

        fillParticipantData(featuresTable, network, source, sourceNode, edge, edgeRow, id, Position.SOURCE);
        if (!selfInteracting) {
            fillParticipantData(featuresTable, network, target, targetNode, edge, edgeRow, id, Position.TARGET);
        }

        if (newEdges != null) newEdges.add(edge);
    }

    private enum Position {
        SOURCE(EdgeFields.SOURCE_BIOLOGICAL_ROLE, EdgeFields.SOURCE_BIOLOGICAL_ROLE_MI_ID, EdgeFields.SOURCE_EXPERIMENTAL_ROLE, EdgeFields.SOURCE_EXPERIMENTAL_ROLE_MI_ID),
        TARGET(EdgeFields.TARGET_BIOLOGICAL_ROLE, EdgeFields.TARGET_BIOLOGICAL_ROLE_MI_ID, EdgeFields.TARGET_EXPERIMENTAL_ROLE, EdgeFields.TARGET_EXPERIMENTAL_ROLE_MI_ID);

        final Field<String> biologicalRole;
        final Field<String> biologicalRoleMIId;
        final Field<String> experimentalRole;
        final Field<String> experimentalRoleMIId;

        Position(Field<String> biologicalRole, Field<String> biologicalRoleMIId, Field<String> experimentalRole, Field<String> experimentalRoleMIId) {
            this.biologicalRole = biologicalRole;
            this.biologicalRoleMIId = biologicalRoleMIId;
            this.experimentalRole = experimentalRole;
            this.experimentalRoleMIId = experimentalRoleMIId;
        }
    }

    private static void fillParticipantData(CyTable featuresTable, CyNetwork network, JsonNode participantJson, CyNode participantNode, CyEdge edge, CyRow edgeRow, Long edgeId, Position position) {
        position.biologicalRole.setValue(edgeRow, participantJson.get("participant_biological_role_name").textValue());
        position.biologicalRoleMIId.setValue(edgeRow, participantJson.get("participant_biological_role_mi_identifier").textValue());
        position.experimentalRole.setValue(edgeRow, participantJson.get("participant_experimental_role_name").textValue());
        position.experimentalRoleMIId.setValue(edgeRow, participantJson.get("participant_experimental_role_mi_identifier").textValue());
        for (JsonNode feature : participantJson.get("participant_features")) {
            if (!feature.get("feature_type").isNull()) {
                String featureAc = feature.get("feature_ac").textValue();
                if (featureAc == null || featureAc.isBlank()) {
                    continue;
                }
                CyRow featureRow = featuresTable.getRow(featureAc);
                switch (position) {
                    case SOURCE:
                        EdgeFields.SOURCE_FEATURES.addValue(edgeRow, featureAc);
                        break;
                    case TARGET:
                        EdgeFields.TARGET_FEATURES.addValue(edgeRow, featureAc);
                        break;
                }

                NodeFields.FEATURES.addValueIfAbsent(network.getRow(participantNode), featureAc);
                FeatureFields.EDGES_ID.addValue(featureRow, edgeId);
                FeatureFields.EDGES_SUID.addValue(featureRow, edge.getSUID());

                Table.FEATURE.setRowFromJson(featureRow, feature);

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
        Manager manager = network.getManager();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);

        Long identifiersSUID = NetworkFields.IDENTIFIERS_TABLE_REF.getValue(networkRow);
        if (identifiersSUID != null) network.setIdentifiersTable(tableManager.getTable(identifiersSUID));
        else System.out.println("Identifiers SUID not found");

        Long featuresSUID = NetworkFields.FEATURES_TABLE_REF.getValue(networkRow);
        if (featuresSUID != null) network.setFeaturesTable(tableManager.getTable(featuresSUID));
        else System.out.println("Features SUID not found");
    }


    public static void buildSubTablesForSubIntactNetwork(Network subNetwork, Network parentNetwork) {
        Manager manager = subNetwork.getManager();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        CyTableFactory tableFactory = manager.utils.getService(CyTableFactory.class);

        CyTable featuresTable = tableFactory.createTable("Features of " + subNetwork.toString(), FeatureFields.AC.toString(), String.class, true, true);
        CyTable identifiersTable = tableFactory.createTable("Identifiers of " + subNetwork.toString(), IdentifierFields.AC.toString(), String.class, true, true);

        CyNetwork cyNetwork = subNetwork.getCyNetwork();
        initLowerTables(subNetwork, cyNetwork, tableManager, cyNetwork.getDefaultNetworkTable(), featuresTable, identifiersTable);

        // Copy included features
        Set<String> featuresAcsToAdd = new HashSet<>();
        CyTable edgeTable = cyNetwork.getDefaultEdgeTable();
        EdgeFields.SOURCE_FEATURES.getColumn(edgeTable).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> featuresAcsToAdd.add((String) o));
        });
        EdgeFields.TARGET_FEATURES.getColumn(edgeTable).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> featuresAcsToAdd.add((String) o));
        });

        for (String featureAc : featuresAcsToAdd) {
            copyRow(parentNetwork.getFeaturesTable(), featuresTable, featureAc, featureAc, Set.of(NetworkFields.UUID.toString()));
        }

        // Copy included identifiers
        CyTable nodeTable = cyNetwork.getDefaultNodeTable();
        Set<String> identifierAcsToAdd = new HashSet<>();
        NodeFields.IDENTIFIERS.getColumn(nodeTable).getValues(List.class).forEach(list -> {
            if (list != null) ((List<?>) list).forEach(o -> identifierAcsToAdd.add((String) o));
        });

        for (String identifierAc : identifierAcsToAdd) {
            copyRow(parentNetwork.getIdentifiersTable(), identifiersTable, identifierAc, identifierAc, Set.of(NetworkFields.UUID.toString()));
        }

        // Remove excluded features and identifiers from nodes
        for (CyRow nodeRow : nodeTable.getAllRows()) {
            NodeFields.FEATURES.getValue(nodeRow).removeIf(ac -> !featuresTable.rowExists(ac));
            NodeFields.IDENTIFIERS.getValue(nodeRow).removeIf(ac -> !identifiersTable.rowExists(ac));
        }

        // Remove excluded edges from features
        for (CyRow featureRow : featuresTable.getAllRows()) {
            Set<Long> idsToKeep = new HashSet<>();
            FeatureFields.EDGES_SUID.getValue(featureRow).removeIf(suid -> {
                boolean edgeExistInSubNetwork = edgeTable.rowExists(suid);
                if (edgeExistInSubNetwork) idsToKeep.add(EdgeFields.ID.getValue(edgeTable.getRow(suid)));
                return !edgeExistInSubNetwork;
            });

            FeatureFields.EDGES_ID.getValue(featureRow).removeIf(id -> !idsToKeep.contains(id));
        }
    }
}
