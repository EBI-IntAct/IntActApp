package uk.ac.ebi.intact.app.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.details.factories.ShowDetailPanelTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.query.NoGUIQueryTask;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.AddTermsTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.FuzzySearchTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ExactQueryTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.settings.SettingsTask;
import uk.ac.ebi.intact.app.internal.tasks.version.factories.VersionTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.app.internal.utils.tables.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;


// TODO: [Optional] Improve non-gui mode
public class CyActivator extends AbstractCyActivator {
    String JSON_EXAMPLE = "{\"SUID\":1234}";

    public CyActivator() {
        super();
    }

    public void start(BundleContext bc) {

        // See if we have a graphics console or not
        boolean haveGUI = true;
        ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

        if (ref == null) {
            haveGUI = false;
            // Issue error and return
        }

        // Get a handle on the CyServiceRegistrar
        CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

        Manager manager = new Manager(registrar);


        // Get our version number
        Version v = bc.getBundle().getVersion();
        String version = v.toString(); // The full version

        {
            // Register our network added listener and session loaded listener
            registerService(bc, manager.data, SessionLoadedListener.class, new Properties());
            registerService(bc, manager.data, NetworkAddedListener.class, new Properties());
            registerService(bc, manager.data, NetworkViewAddedListener.class, new Properties());
            registerService(bc, manager.data, NetworkAboutToBeDestroyedListener.class, new Properties());
            registerService(bc, manager.data, NetworkViewAboutToBeDestroyedListener.class, new Properties());
        }

        {
            CollapseViewTaskFactory collapseViewTaskFactory = new CollapseViewTaskFactory(manager, false);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "collapse");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Collapse");
            properties.setProperty(MENU_GRAVITY, "1.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, collapseViewTaskFactory, TaskFactory.class, properties);
        }
        {
            ExpandViewTaskFactory expendTaskFactory = new ExpandViewTaskFactory(manager, false);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "expand");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Expand");
            properties.setProperty(MENU_GRAVITY, "2.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, expendTaskFactory, TaskFactory.class, properties);
        }
        {
            MutationViewTaskFactory mutationViewTaskFactory = new MutationViewTaskFactory(manager, false);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "mutation");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Mutation");
            properties.setProperty(MENU_GRAVITY, "3.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, mutationViewTaskFactory, TaskFactory.class, properties);
        }

        {
            VersionTaskFactory versionFactory = new VersionTaskFactory(version);
            Properties versionProps = new Properties();
            versionProps.setProperty(COMMAND_NAMESPACE, "intact");
            versionProps.setProperty(COMMAND, "version");
            versionProps.setProperty(COMMAND_DESCRIPTION,
                    "Returns the version of IntActApp");
            versionProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
            versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"2.1.0\"}");
            registerService(bc, versionFactory, TaskFactory.class, versionProps);
        }

        {
            AddTermsTaskFactory addTerms = new AddTermsTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Query for additional nodes");
            props.setProperty(MENU_GRAVITY, "3.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, addTerms, NetworkTaskFactory.class, props);
        }


        if (haveGUI) {
            ShowDetailPanelTaskFactory showResults = new ShowDetailPanelTaskFactory(manager);
            showResults.reregister();
            manager.utils.setShowDetailPanelTaskFactory(showResults);

            CyNetwork current = manager.data.getCurrentCyNetwork();
            if (ModelUtils.ifHaveIntactNS(current)) {
                manager.utils.execute(showResults.createTaskIterator(), true);
            }
        }

        {
            Properties propsQueryCommand = new Properties();
            propsQueryCommand.setProperty(COMMAND_NAMESPACE, "intact");
            propsQueryCommand.setProperty(COMMAND, "query");
            propsQueryCommand.setProperty(COMMAND_DESCRIPTION, "Search for interactors ids or names and build network around them");
            propsQueryCommand.setProperty(COMMAND_SUPPORTS_JSON, "false");
            AbstractTaskFactory intactCommandQueryFactory = new AbstractTaskFactory() {
                @Override
                public TaskIterator createTaskIterator() {
                    return new TaskIterator(new NoGUIQueryTask(manager));
                }
            };

            registerService(bc, intactCommandQueryFactory, TaskFactory.class, propsQueryCommand);
        }

        {
            Properties propsSettings = new Properties();
            propsSettings.setProperty(PREFERRED_MENU, "Apps.IntAct");
            propsSettings.setProperty(INSERT_SEPARATOR_BEFORE, "true");
            propsSettings.setProperty(TITLE, "Settings");
            propsSettings.setProperty(MENU_GRAVITY, "20.0");
            propsSettings.setProperty(IN_MENU_BAR, "true");
            AbstractTaskFactory intactSettingsFactory = new AbstractTaskFactory() {
                @Override
                public TaskIterator createTaskIterator() {
                    return new TaskIterator(new SettingsTask(manager));
                }
            };

            registerService(bc, intactSettingsFactory, TaskFactory.class, propsSettings);
        }

        // Register our Network search factories

        {
            ExactQueryTaskFactory intactQuery = new ExactQueryTaskFactory(manager);
            Properties propsSearch = new Properties();
            registerService(bc, intactQuery, NetworkSearchTaskFactory.class, propsSearch);
        }
        {
            FuzzySearchTaskFactory intactSearch = new FuzzySearchTaskFactory(manager);
            Properties propsSearch = new Properties();
            registerService(bc, intactSearch, NetworkSearchTaskFactory.class, propsSearch);
        }

        manager.utils.info("Intact App initialized");
    }
}
