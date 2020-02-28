package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.ui.EnrichmentCytoPanel;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class ShowChartsTask extends AbstractTask implements ObservableTask {

    private IntactManager manager;
    private EnrichmentCytoPanel cytoPanel;

    public ShowChartsTask(IntactManager manager, EnrichmentCytoPanel cytoPanel) {
        this.manager = manager;
        this.cytoPanel = cytoPanel;
    }

    @Override
    public void run(TaskMonitor arg0) throws Exception {
        // Filter the current list
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cytoPanel.drawCharts();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R getResults(Class<? extends R> clzz) {
        if (clzz.equals(String.class)) {
            return (R) "";
        } else if (clzz.equals(JSONResult.class)) {
            JSONResult res = () -> "{}";
            return (R) res;
        }
        return null;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class);
    }

}
