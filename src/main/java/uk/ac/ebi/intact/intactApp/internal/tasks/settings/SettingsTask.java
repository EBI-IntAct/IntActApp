package uk.ac.ebi.intact.intactApp.internal.tasks.settings;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.options.OptionsPanel;

import javax.swing.*;
import java.awt.*;

public class SettingsTask extends AbstractTask {
    private final IntactManager manager;

    public SettingsTask(IntactManager manager) {
        this.manager = manager;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog();
            d.setTitle("IntAct Settings");
            d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            OptionsPanel panel = new OptionsPanel(manager);
            d.setContentPane(panel);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.pack();
            d.setVisible(true);
        });
    }
}
