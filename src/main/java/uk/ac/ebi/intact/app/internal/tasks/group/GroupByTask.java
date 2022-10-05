package uk.ac.ebi.intact.app.internal.tasks.group;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.*;
import java.util.stream.Collectors;

public class GroupByTask extends AbstractNetworkTask {

    @Tunable(description = "Columns to merge by",
            longDescription = "Nodes will be grouped and collapsed if they share a common value amongst the selected columns")
    public ListMultipleSelection<CyColumn> columnsToMergeBy;

    @Tunable(description = "Flatten lists",
            longDescription = "If True, treat each values of lists as a potential match to group by (e.g. 'a' would be merged with ['a','b'])<br>" +
                    "If False, treat the full list as a potential match to group by, regardless of their order (e.g. ['b','a'] would be merged with ['a','b'])<br>" +
                    "Default value : True")
    public boolean flattenLists = true;

    @Tunable(description = "Allow groups of one node",
            longDescription = "If True, all nodes will be in a group, no matter the size of the group<br>" +
                    "If False, will only group nodes if more than one node share the same value in the selected column<br>" +
                    "Default value : False")
    public boolean allowSingleElementGroups = false;

    private final CyGroupFactory groupFactory;
    private final CyGroupManager groupManager;
    private final CyGroupSettingsManager groupSettingsManager;
    private final CyGroupAggregationManager groupAggregationManager;
    private final Map<Object, Set<CyNode>> commonValueToNodes = new HashMap<>();


    public GroupByTask(Manager manager, CyNetwork network) {
        super(network);
        this.groupFactory = manager.utils.getService(CyGroupFactory.class);
        this.groupManager = manager.utils.getService(CyGroupManager.class);
        this.groupSettingsManager = manager.utils.getService(CyGroupSettingsManager.class);
        this.groupAggregationManager = manager.utils.getService(CyGroupAggregationManager.class);
        this.columnsToMergeBy = new ListMultipleSelection<>(new ArrayList<>(network.getDefaultNodeTable().getColumns()));
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Group nodes of " + network.getRow(network).get(CyNetwork.NAME, String.class));
        taskMonitor.setStatusMessage("Discriminate List columns from Simple columns");
        Map<Boolean, List<CyColumn>> columnsGroupedByIsList = columnsToMergeBy.getSelectedValues().stream()
                .collect(Collectors.groupingBy(col -> List.class.isAssignableFrom(col.getType())));
        taskMonitor.setStatusMessage("Collect similar values");
        List<CyNode> nodes = network.getNodeList();
        float i = 0, n = nodes.size();
        for (CyNode node : nodes) {
            CyRow row = network.getRow(node);
            if (columnsGroupedByIsList.get(false) != null) {
                columnsGroupedByIsList.get(false).stream()
                        .map(cyColumn -> row.getRaw(cyColumn.getName()))
                        .forEach(value -> commonValueToNodes.computeIfAbsent(value, o -> new HashSet<>()).add(node));
            }
            if (columnsGroupedByIsList.get(true) != null) {
                if (flattenLists) {
                    columnsGroupedByIsList.get(true).stream()
                            .map(cyColumn -> row.getList(cyColumn.getName(), cyColumn.getListElementType()))
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .forEach(value -> commonValueToNodes.computeIfAbsent(value, o -> new HashSet<>()).add(node));
                } else {
                    columnsGroupedByIsList.get(true).stream()
                            .map(cyColumn -> row.getList(cyColumn.getName(), cyColumn.getListElementType()))
                            .filter(Objects::nonNull)
                            .map(values -> values.stream()
                                    .map(Objects::toString)
                                    .sorted()
                                    .collect(Collectors.joining(" | "))
                            )
                            .forEach(value -> commonValueToNodes.computeIfAbsent(value, o -> new HashSet<>()).add(node));
                }
            }
            taskMonitor.setProgress(i++ / n);
        }

        taskMonitor.setStatusMessage("Merge nodes with common values");
        taskMonitor.setProgress(0);
        List<Map.Entry<Object, Set<CyNode>>> entries = allowSingleElementGroups ?
                new ArrayList<>(commonValueToNodes.entrySet()) :
                commonValueToNodes.entrySet().stream().filter(entry -> entry.getValue().size() > 1).collect(Collectors.toList());
        i = 0;
        n = entries.size();
        for (Map.Entry<Object, Set<CyNode>> entry : entries) {
            CyGroup group = groupFactory.createGroup(network, new ArrayList<>(entry.getValue()), null, true);
            group.collapse(network);
            network.getRow(group.getGroupNode()).set(CyNetwork.NAME, entry.getKey());
            taskMonitor.setStatusMessage("Grouping group " + i + " made of " + entry.getValue().size() + " nodes");
            taskMonitor.setProgress(i++ / n);
        }

        groupFactory.createGroup(network, true);

    }
}
