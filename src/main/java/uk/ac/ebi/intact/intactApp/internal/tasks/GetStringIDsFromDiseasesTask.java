package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONObject;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.*;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetStringIDsFromDiseasesTask extends AbstractTask implements ObservableTask {
    final IntactNetwork intactNetwork;
    final IntactManager manager;
    final Species species;
    final int limit;
    final int confidence;
    final String query;
    final String diseaseName;
    private List<TextMiningResult> tmResults;

    public GetStringIDsFromDiseasesTask(final IntactNetwork intactNetwork, final Species species, final int limit,
                                        final int confidence, final String query, final String diseaseName) {
        this.intactNetwork = intactNetwork;
        manager = intactNetwork.getManager();
        this.species = species;
        this.limit = limit;
        this.confidence = confidence;
        this.query = query;
        this.diseaseName = diseaseName;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Loading STRING network with disease associated proteins");
        monitor.setTitle("Querying to get proteins associated with disease based on text mining");
        Map<String, String> args = new HashMap<>();
        args.put("type1", "-26");
        args.put("id1", query);
        args.put("format", "json");
        args.put("limit", Integer.toString(limit));
        args.put("type2", Integer.toString(species.getTaxId()));
        JSONObject tmobject = HttpUtils.postJSON(manager.getIntegrationURL(), args, manager);
        if (tmobject == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }

        tmResults = ModelUtils.getIdsFromJSON(manager, species.getTaxId(), tmobject, query, true);
        if (tmResults == null || tmResults.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }
        monitor.showMessage(TaskMonitor.Level.INFO, "Found " + tmResults.size() + " associated proteins");

        Map<String, String> queryTermMap = new HashMap<>();
        List<String> stringIds = new ArrayList<>();
        for (TextMiningResult tm : tmResults) {
            stringIds.add(tm.getID());
        }

        // OK, if we got any results, fetch the network
        LoadInteractions liTask = new LoadInteractions(intactNetwork, species.getName(), species.getTaxId(),
                confidence, 0, stringIds, queryTermMap, diseaseName, Databases.STRING.getAPIName());
        AddTextMiningResultsTask atmTask = new AddTextMiningResultsTask(intactNetwork, tmResults);
        insertTasksAfterCurrentTask(liTask, atmTask);
    }

    @ProvidesTitle
    public String getTitle() {
        return "Find proteins from text mining";
    }

    public List<TextMiningResult> getTextMiningResults() {
        return tmResults;
    }

    public <R> R getResults(Class<? extends R> type) {
        return null;
    }
}
