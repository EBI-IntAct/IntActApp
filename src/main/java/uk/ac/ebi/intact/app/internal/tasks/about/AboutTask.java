package uk.ac.ebi.intact.app.internal.tasks.about;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.panels.about.AboutPanel;

import javax.swing.*;
import java.awt.*;

public class AboutTask extends AbstractTask {
    private Manager manager;

    public AboutTask(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run(TaskMonitor taskMonitor)  {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog();
            d.setTitle("About IntAct");
            d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            d.setContentPane(new AboutPanel(manager));
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.pack();
            d.setVisible(true);
        });
    }
}
