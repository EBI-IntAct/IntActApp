package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowEnrichmentPanelTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowPublicationsPanelTaskFactory;

import javax.swing.*;

public class GetEnrichmentTaskSwing extends GetEnrichmentTask {

    public GetEnrichmentTaskSwing(IntactManager manager, CyNetwork network, CyNetworkView netView,
                                  ShowEnrichmentPanelTaskFactory showFactory, ShowPublicationsPanelTaskFactory showFactoryPubl, boolean publOnly) {
        super(manager, network, netView, showFactory, showFactoryPubl, publOnly);
    }

    @Override
    protected void showError(String msg) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(null, msg, "Unable to get enrichment", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

}
