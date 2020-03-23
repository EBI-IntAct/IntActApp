package uk.ac.ebi.intact.intactApp.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.*;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.IntactWebServiceClient;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.view.StringCustomGraphicsFactory;
import uk.ac.ebi.intact.intactApp.internal.view.StringLayer;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

// import uk.ac.ebi.intact.intactApp.internal.tasks.FindProteinsTaskFactory;
// import uk.ac.ebi.intact.intactApp.internal.tasks.OpenEvidenceTaskFactory;

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

//        final FunctionalMappingFactory fnFactory = new FunctionalMappingFactory(registrar);
//        {
//            final Properties props = new Properties();
//            props.setProperty("service.type", "factory");
//            props.setProperty("mapping.type", "functional");
//            registerService(bc, fnFactory, VisualMappingFunctionFactory.class, props);
//        }

        IntactManager manager = new IntactManager(registrar);


        // Get our version number
        Version v = bc.getBundle().getVersion();
        String version = v.toString(); // The full version

        // Only look at the .0 version for our internal purposes
        String minorVersion = new Version(v.getMajor(), v.getMinor(), 0).toString();
        manager.setVersion(minorVersion);

        // Get configuration and messages for user from server
        manager.updateURIsFromConfig();

        {
            // Register our network added listener and session loaded listener
            registerService(bc, manager, NetworkAddedListener.class, new Properties());
            registerService(bc, manager, SessionLoadedListener.class, new Properties());
            registerService(bc, manager, NetworkAboutToBeDestroyedListener.class, new Properties());
        }

        {
            // Register our web service client
            IntactWebServiceClient client = new IntactWebServiceClient(manager);
            registerAllServices(bc, client, new Properties());
        }





        {
            GetNetworkTaskFactory getNetwork = new GetNetworkTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "protein query");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Create a STRING network from multiple protein names/identifiers");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Enter protein names or identifiers to query the STRING " +
                            "database for protein-protein interactions.\n" +
                            "<br/>STRING is a database of known and predicted protein " +
                            "interactions.  The interactions include direct (physical) " +
                            "and indirect (functional) associations; they are derived from " +
                            "four sources: \n" +
                            "* Genomic Context\n" +
                            "* High-throughput Experiments\n" +
                            "* (Conserved) Coexpression\n" +
                            "* Previous Knowledge\n\n" +
                            "STRING quantitatively integrates " +
                            "interaction data from these sources for a large number " +
                            "of organisms, and transfers information between these " +
                            "organisms where applicable.");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);

            registerService(bc, getNetwork, TaskFactory.class, props);
        }
        {
            CollapseViewTaskFactory collapseViewTaskFactory = new CollapseViewTaskFactory(manager);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "collapse");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Collapsed edges view");
            properties.setProperty(MENU_GRAVITY, "1.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, collapseViewTaskFactory, TaskFactory.class, properties);
        }
        {
            ExpandViewTaskFactory expendTaskFactory = new ExpandViewTaskFactory(manager);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "expend");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Expanded edges view");
            properties.setProperty(MENU_GRAVITY, "2.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, expendTaskFactory, TaskFactory.class, properties);
        }
        {
            MutationViewTaskFactory mutationViewTaskFactory = new MutationViewTaskFactory(manager);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "mutation");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Mutation view");
            properties.setProperty(MENU_GRAVITY, "3.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, mutationViewTaskFactory, TaskFactory.class, properties);
        }



        {
            VersionTaskFactory versionFactory = new VersionTaskFactory(version);
            Properties versionProps = new Properties();
            versionProps.setProperty(COMMAND_NAMESPACE, "string");
            versionProps.setProperty(COMMAND, "version");
            versionProps.setProperty(COMMAND_DESCRIPTION,
                    "Returns the version of StringApp");
            versionProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
            versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"2.1.0\"}");
            registerService(bc, versionFactory, TaskFactory.class, versionProps);
        }

        {
            SettingsTaskFactory settingsFactory =
                    new SettingsTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Settings");
            props.setProperty(MENU_GRAVITY, "100.0");
            props.setProperty(IN_MENU_BAR, "true");
            props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "settings");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Adjust various settings");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, settingsFactory, TaskFactory.class, props);
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


            {
                ShowResultsPanelTaskFactory showResults = new ShowResultsPanelTaskFactory(manager);
                showResults.reregister();
                manager.setShowResultsPanelTaskFactory(showResults);

                // Now bring up the side panel if the current network is a STRING network
                CyNetwork current = manager.getCurrentNetwork();
                if (ModelUtils.ifHaveIntactNS(current)) {
                    // It's the current network.  Bring up the results panel
                    manager.execute(showResults.createTaskIterator(), true);
                }
            }
        }

        {
            // Register our "show enhanced labels" toggle
            ShowEnhancedLabelsTaskFactory showEnhancedLabelsTF = new ShowEnhancedLabelsTaskFactory(manager);
            showEnhancedLabelsTF.reregister();
            manager.setShowEnhancedLabelsTaskFactory(showEnhancedLabelsTF);
        }

        {
            // Register our custom graphics
            CyCustomGraphicsFactory<StringLayer> stringLookFactory = new StringCustomGraphicsFactory(manager);
            Properties stringProps = new Properties();
            registerService(bc, stringLookFactory, CyCustomGraphicsFactory.class, stringProps);
        }

        // Register our Network search factories
        {
            IntactSearchTaskFactory stringSearch = new IntactSearchTaskFactory(manager);
            Properties propsSearch = new Properties();
            registerService(bc, stringSearch, NetworkSearchTaskFactory.class, propsSearch);
        }

        manager.info("Intact App initialized");
    }

}
