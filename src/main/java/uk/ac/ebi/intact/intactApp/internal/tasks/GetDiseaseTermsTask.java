package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONObject;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.EntityIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDiseaseTermsTask extends AbstractTask implements ObservableTask {
    final IntactManager intactManager;
    final int taxon;
    final String term;
    List<EntityIdentifier> matches = null;

    public GetDiseaseTermsTask(IntactManager intactManager, int taxon, String term) {
        this.intactManager = intactManager;
        this.taxon = taxon;
        this.term = term;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.setTitle("Getting disease terms");
        String url = intactManager.getEntityQueryURL();

        Map<String, String> args = new HashMap<>();
        args.put("limit", "100");
        args.put("types", "-26");
        args.put("format", "json");
        args.put("query", term);
        // String response = "[[{"type":-26,"id":"DOID:1307","matched":"dementia","primary":"dementia"},{"type":-26,"id":"DOID:11870","matched":"Dementia in Pick's disease ","primary":"Pick's disease"},{"type":-26,"id":"DOID:12217","matched":"Dementia with Lewy bodies","primary":"Lewy body dementia"}],false]"
        //
        // Get the results
        JSONObject results = HttpUtils.getJSON(url, args, intactManager);
        // Object results = HttpUtils.testJSON(url, args, stringManager, response);
        if (results == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }

        matches = ModelUtils.getEntityIdsFromJSON(intactManager, results);

    }

    public List<EntityIdentifier> getMatchedTerms() {
        return matches;
    }

    public int getTaxon() {
        return taxon;
    }

    @Override
    public <T> T getResults(Class<? extends T> type) {
        return null;
    }
}

