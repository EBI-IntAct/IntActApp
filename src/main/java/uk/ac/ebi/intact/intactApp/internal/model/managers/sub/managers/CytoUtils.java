package uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowDetailPanelTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.DetailPanel;

import javax.swing.*;
import java.util.Map;
import java.util.Properties;

public class CytoUtils {
    final CyServiceRegistrar registrar;
    final CyEventHelper cyEventHelper;
    final Logger logger;
    final TaskManager<?, ?> dialogTaskManager;
    final SynchronousTaskManager<?> synchronousTaskManager;
    final CommandExecutorTaskFactory commandExecutorTaskFactory;
    final AvailableCommands availableCommands;
    ShowDetailPanelTaskFactory detailPanelTaskFactory;
    DetailPanel detailPanel;

    public CytoUtils(CyServiceRegistrar registrar) {
        this.logger = Logger.getLogger(CyUserLog.NAME);
        this.registrar = registrar;
        // Get our task managers
        this.dialogTaskManager = registrar.getService(TaskManager.class);
        this.synchronousTaskManager = registrar.getService(SynchronousTaskManager.class);
        availableCommands = registrar.getService(AvailableCommands.class);
        this.commandExecutorTaskFactory = registrar.getService(CommandExecutorTaskFactory.class);
        this.cyEventHelper = registrar.getService(CyEventHelper.class);
    }

    public void flushEvents() {
        cyEventHelper.flushPayloadEvents();
    }

    public void execute(TaskIterator iterator) {
        execute(iterator, false);
    }

    public void execute(TaskIterator iterator, TaskObserver observer) {
        execute(iterator, observer, false);
    }

    public void execute(TaskIterator iterator, boolean synchronous) {
        if (synchronous) {
            synchronousTaskManager.execute(iterator);
        } else {
            dialogTaskManager.execute(iterator);
        }
    }

    public void execute(TaskIterator iterator, TaskObserver observer, boolean synchronous) {
        if (synchronous) {
            synchronousTaskManager.execute(iterator, observer);
        } else {
            dialogTaskManager.execute(iterator, observer);
        }
    }

    public void executeCommand(String namespace, String command,
                               Map<String, Object> args, TaskObserver observer) {
        TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(namespace, command, args, observer);
        execute(ti, true);
    }

    public void info(String info) {
        logger.info(info);
    }

    public void warn(String warn) {
        logger.warn(warn);
    }

    public void error(String error) {
        logger.error(error);
    }

    public void critical(String criticalError) {
        logger.error(criticalError);
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(null, "<html><p style=\"width:200px;\">" + criticalError + "</p></html>", "Critical IntActApp error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void setShowDetailPanelTaskFactory(ShowDetailPanelTaskFactory factory) {
        detailPanelTaskFactory = factory;
    }

    public void setDetailPanel(DetailPanel detailPanel) {
        this.detailPanel = detailPanel;
    }

    public void showResultsPanel() {
        if (detailPanel == null) {
            execute(detailPanelTaskFactory.createTaskIterator(), true);
        } else {
            // Make sure we show it
            detailPanel.showCytoPanel();
        }
    }

    public void hideResultsPanel() {
        if (detailPanel != null) {
            detailPanel.hideCytoPanel();
        }
    }

    public boolean haveEnhancedGraphics() {
        return availableCommands.getNamespaces().contains("enhancedGraphics");
    }

    public ShowDetailPanelTaskFactory getShowDetailPanelTaskFactory() {
        return detailPanelTaskFactory;
    }

    public <T> T getService(Class<? extends T> clazz) {
        return registrar.getService(clazz);
    }

    public <T> T getService(Class<? extends T> clazz, String filter) {
        return registrar.getService(clazz, filter);
    }

    public void registerService(Object service, Class<?> clazz, Properties props) {
        registrar.registerService(service, clazz, props);
    }

    public void registerAllServices(CyProperty<Properties> service, Properties props) {
        registrar.registerAllServices(service, props);
    }

    public void unregisterService(Object service, Class<?> clazz) {
        registrar.unregisterService(service, clazz);
    }
}