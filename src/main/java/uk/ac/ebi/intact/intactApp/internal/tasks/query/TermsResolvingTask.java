package uk.ac.ebi.intact.intactApp.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.core.Interactor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;

import java.util.List;
import java.util.Map;

public class TermsResolvingTask extends AbstractTask implements ObservableTask {
    final IntactNetwork intactNetwork;
    final int taxon;
    final String terms;
    Map<String, List<Interactor>> resolvedInteractors = null;
    final boolean exactQuery;

    public TermsResolvingTask(IntactNetwork intactNetwork, int taxon, String terms, boolean exactQuery) {
        this.intactNetwork = intactNetwork;
        this.taxon = taxon;
        this.terms = terms;
        this.exactQuery = exactQuery;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.setTitle("Solving term ambiguity");
        if (terms.isBlank()) {
            monitor.showMessage(TaskMonitor.Level.WARN, "Empty query");
        } else {
            resolvedInteractors = intactNetwork.resolveTerms(terms, exactQuery);
            if (resolvedInteractors == null || resolvedInteractors.size() == 0) {
                monitor.showMessage(TaskMonitor.Level.ERROR, "Query returned no terms");
            }
        }
    }

    public int getTaxon() {
        return taxon;
    }


    @Override
    public <T> T getResults(Class<? extends T> type) {
        return null;
    }
}

