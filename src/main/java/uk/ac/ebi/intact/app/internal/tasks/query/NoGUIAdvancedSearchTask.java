package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.*;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class NoGUIAdvancedSearchTask extends AbstractTask {

    @Tunable(context = "nogui", exampleStringValue = "negative:true and species:9606", gravity = 0,
            description = "Advanced query to be run. <br>" +
                    "Example: \"negative:true and species:9606\"",
            required = true)
    public String query;

    @Tunable(context = "nogui", gravity = 1,
            description = "Name of the network to build", longDescription = "Name of the network to build.<br> If not given, will be IntAct network")
    public String netName = null;

    @Tunable(context = "nogui", gravity = 4,
            description = "If true, apply force directed layout algorithm after the extraction on the new network<br>" +
                    "If false, do not apply any layout algorithm: All elements of the extracted network will be stacked on top of each others visually.<br>" +
                    "Default value : True")
    public Boolean applyLayout = true;


    private final Manager manager;

    public NoGUIAdvancedSearchTask(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        manager.utils.execute(new TaskIterator( new AdvancedSearchTask(manager, query, applyLayout, netName)));
    }
}
