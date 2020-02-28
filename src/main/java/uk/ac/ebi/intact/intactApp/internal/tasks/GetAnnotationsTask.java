package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.Annotation;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;

import java.util.List;
import java.util.Map;

public class GetAnnotationsTask extends AbstractTask implements ObservableTask {
    final IntactNetwork intactNetwork;
    final int taxon;
    final String terms;
    final String useDATABASE;
    Map<String, List<Annotation>> annotations = null;

    public GetAnnotationsTask(IntactNetwork intactNetwork, int taxon, String terms, String useDATABASE) {
        this.intactNetwork = intactNetwork;
        this.taxon = taxon;
        this.terms = terms;
        this.useDATABASE = useDATABASE;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.setTitle("Getting annotations");
        annotations = intactNetwork.getAnnotations(taxon, terms, useDATABASE, true);
        if (annotations == null || annotations.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "Query returned no terms");
        }
    }

    public Map<String, List<Annotation>> getAnnotations() {
        return annotations;
    }

    public int getTaxon() {
        return taxon;
    }

    public IntactNetwork getIntactNetwork() {
        return intactNetwork;
    }

    @Override
    public <T> T getResults(Class<? extends T> type) {
        return null;
    }
}

