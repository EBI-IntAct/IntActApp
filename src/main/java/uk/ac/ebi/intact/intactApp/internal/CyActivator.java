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
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
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
import uk.ac.ebi.intact.intactApp.internal.ui.DiseaseNetworkWebServiceClient;
import uk.ac.ebi.intact.intactApp.internal.ui.IntactWebServiceClient;
import uk.ac.ebi.intact.intactApp.internal.ui.StitchWebServiceClient;
import uk.ac.ebi.intact.intactApp.internal.ui.TextMiningWebServiceClient;
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
            // Register our text mining web service client
            TextMiningWebServiceClient client = new TextMiningWebServiceClient(manager);
            registerAllServices(bc, client, new Properties());
        }

        {
            // Register our disease network web service client
            DiseaseNetworkWebServiceClient client = new DiseaseNetworkWebServiceClient(manager);
            registerAllServices(bc, client, new Properties());
        }

        {
            // Register our stitch network web service client
            StitchWebServiceClient client = new StitchWebServiceClient(manager);
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
            CollapseViewTaskFactory collapseViewTaskFactory = new CollapseViewTaskFactory(manager, null);
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
            ExpandViewTaskFactory expendTaskFactory = new ExpandViewTaskFactory(manager, null);
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
            MutationViewTaskFactory mutationViewTaskFactory = new MutationViewTaskFactory(manager, null);
            Properties properties = new Properties();
            properties.setProperty(COMMAND_NAMESPACE, "intact");
            properties.setProperty(COMMAND, "mutation");

            properties.setProperty(PREFERRED_MENU, "Apps.IntAct");
            properties.setProperty(TITLE, "Mutation view");
            properties.setProperty(MENU_GRAVITY, "3.0");
            properties.setProperty(IN_MENU_BAR, "true");
            registerService(bc, mutationViewTaskFactory, TaskFactory.class, properties);
        }

//        {
//            ExpandNetworkTaskFactory expandFactory = new ExpandNetworkTaskFactory(manager);
//            Properties expandProps = new Properties();
//            expandProps.setProperty(COMMAND_NAMESPACE, "string");
//            expandProps.setProperty(COMMAND, "expand");
//            expandProps.setProperty(COMMAND_DESCRIPTION, "Expand a STRING network by more interactors");
//            expandProps.setProperty(COMMAND_LONG_DESCRIPTION,
//                    "Expand an already exisitng STRING network by more interactors such as STITCH compounds, "
//                            + "proteins of the network species as well as proteins interacting with avaialble viruses or host species proteins");
//            expandProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
//            expandProps.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);
//            registerService(bc, expandFactory, TaskFactory.class, expandProps);
//        }

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
            FilterEnrichmentTableTaskFactory filterFactory =
                    new FilterEnrichmentTableTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "filter enrichment");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Filter the terms in the enrichment table");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, filterFactory, TaskFactory.class, props);
        }

        {
            ShowChartsTaskFactory showChartsFactory =
                    new ShowChartsTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "show charts");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Show the enrichment charts");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, showChartsFactory, TaskFactory.class, props);
        }

        {
            HideChartsTaskFactory hideChartsFactory =
                    new HideChartsTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "hide charts");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Hide the enrichment charts");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, hideChartsFactory, TaskFactory.class, props);
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
            // Register our "Add Nodes" factory
            ExpandNetworkTaskFactory addNodes = new ExpandNetworkTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Expand network");
            props.setProperty(MENU_GRAVITY, "1.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, addNodes, NetworkTaskFactory.class, props);

            Properties props2 = new Properties();
            props2.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props2.setProperty(TITLE, "Expand network");
            props2.setProperty(MENU_GRAVITY, "1.0");
            props2.setProperty(IN_MENU_BAR, "false");
            registerService(bc, addNodes, NetworkViewTaskFactory.class, props2);

            Properties props3 = new Properties();
            props3.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props3.setProperty(TITLE, "Expand network");
            props3.setProperty(MENU_GRAVITY, "1.0");
            props3.setProperty(IN_MENU_BAR, "false");
            registerService(bc, addNodes, NodeViewTaskFactory.class, props3);
        }

        {
            ChangeConfidenceTaskFactory changeConfidence = new ChangeConfidenceTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Change confidence");
            props.setProperty(MENU_GRAVITY, "2.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, changeConfidence, NetworkTaskFactory.class, props);

            Properties props2 = new Properties();
            props2.setProperty(COMMAND_NAMESPACE, "string");
            props2.setProperty(COMMAND, "change confidence");
            props2.setProperty(COMMAND_DESCRIPTION,
                    "Change confidence of the network");
            props2.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Changes the confidence of the network. If increased, some edges will disapear. "
                            + "If decreased, new edges might be added to the network.");
            props2.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props2.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, changeConfidence, TaskFactory.class, props2);
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

        {
            SetLabelAttributeTaskFactory setLabel = new SetLabelAttributeTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Set STRING label attribute");
            props.setProperty(MENU_GRAVITY, "10.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, setLabel, NetworkTaskFactory.class, props);
        }

        {
            SetConfidenceTaskFactory setConfidence = new SetConfidenceTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "Set as STRING network");
            props.setProperty(MENU_GRAVITY, "6.0");
            props.setProperty(IN_MENU_BAR, "true");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "make string");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Set the network as a STRING network");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Sets the network as a STRING network.  This assumes that the network " +
                            "was originally derived from STRING and has all of the necessary STRING " +
                            "columns.");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\": 123}");
            registerService(bc, setConfidence, NetworkTaskFactory.class, props);
        }

        {
            StringifyTaskFactory stringify = new StringifyTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct");
            props.setProperty(TITLE, "STRINGify network");
            props.setProperty(MENU_GRAVITY, "7.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, stringify, NetworkTaskFactory.class, props);

            props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "stringify");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Create a new STRING network from the current network");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Creates a new network from the nodes and edges of the specified network," +
                            "by querying STRING for all of the nodes and then copying over the edges " +
                            "from the original network.");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\": 123}");
            registerService(bc, stringify, TaskFactory.class, props);
        }

        {
            ExportEnrichmentTaskFactory exportEnrichment = new ExportEnrichmentTaskFactory(manager);
            // Properties props = new Properties();
            // props.setProperty(PREFERRED_MENU, "File.Export");
            // props.setProperty(TITLE, "STRING Enrichment");
            // props.setProperty(MENU_GRAVITY, "4.0");
            // props.setProperty(IN_MENU_BAR, "true");
            // registerService(bc, exportEnrichment, NetworkTaskFactory.class, props);

            Properties props2 = new Properties();
            props2.setProperty(PREFERRED_MENU, "Apps.IntAct Enrichment");
            props2.setProperty(TITLE, "Export enrichment results");
            props2.setProperty(MENU_GRAVITY, "3.0");
            props2.setProperty(IN_MENU_BAR, "true");
            registerService(bc, exportEnrichment, NetworkTaskFactory.class, props2);
        }

        {
            ExportPublicationsTaskFactory exportPublications = new ExportPublicationsTaskFactory(manager);
            Properties props = new Properties();
            props.setProperty(PREFERRED_MENU, "Apps.IntAct Enrichment");
            props.setProperty(TITLE, "Export publications results");
            props.setProperty(MENU_GRAVITY, "6.0");
            props.setProperty(IN_MENU_BAR, "true");
            registerService(bc, exportPublications, NetworkTaskFactory.class, props);
        }

        if (haveGUI) {
            GetEnrichmentTaskFactory getEnrichment = new GetEnrichmentTaskFactory(manager, true);
            {
                Properties propsEnrichment = new Properties();
                propsEnrichment.setProperty(PREFERRED_MENU, "Apps.IntAct Enrichment");
                propsEnrichment.setProperty(TITLE, "Retrieve functional enrichment");
                propsEnrichment.setProperty(MENU_GRAVITY, "1.0");
                propsEnrichment.setProperty(IN_MENU_BAR, "true");
                // propsEnrichment.setProperty(INSERT_SEPARATOR_BEFORE, "true");
                registerService(bc, getEnrichment, NetworkTaskFactory.class, propsEnrichment);

                ShowEnrichmentPanelTaskFactory showEnrichment = new ShowEnrichmentPanelTaskFactory(manager);
                showEnrichment.reregister();
                getEnrichment.setShowEnrichmentPanelFactory(showEnrichment);
                manager.setShowEnrichmentPanelTaskFactory(showEnrichment);
            }

            GetPublicationsTaskFactory getPublications = new GetPublicationsTaskFactory(manager, true);
            {
                Properties propsPublications = new Properties();
                propsPublications.setProperty(PREFERRED_MENU, "Apps.IntAct Enrichment");
                propsPublications.setProperty(TITLE, "Retrieve enriched publications");
                propsPublications.setProperty(MENU_GRAVITY, "4.0");
                propsPublications.setProperty(IN_MENU_BAR, "true");
                propsPublications.setProperty(INSERT_SEPARATOR_BEFORE, "true");
                registerService(bc, getPublications, NetworkTaskFactory.class, propsPublications);

                ShowPublicationsPanelTaskFactory showPublications = new ShowPublicationsPanelTaskFactory(manager);
                showPublications.reregister();
                getPublications.setShowPublicationsPanelFactory(showPublications);
                manager.setShowPublicationsPanelTaskFactory(showPublications);
            }

            {
                ShowResultsPanelTaskFactory showResults = new ShowResultsPanelTaskFactory(manager);
                showResults.reregister();
                manager.setShowResultsPanelTaskFactory(showResults);

                // Now bring up the side panel if the current network is a STRING network
                CyNetwork current = manager.getCurrentNetwork();
                if (ModelUtils.ifHaveStringNS(current)) {
                    // It's the current network.  Bring up the results panel
                    manager.execute(showResults.createTaskIterator(), true);
                }
            }
        }

        GetEnrichmentTaskFactory getCommandEnrichment = new GetEnrichmentTaskFactory(manager, false);
        {
            Properties propsEnrichment = new Properties();
            propsEnrichment.setProperty(COMMAND_NAMESPACE, "string");
            propsEnrichment.setProperty(COMMAND, "retrieve enrichment");
            propsEnrichment.setProperty(COMMAND_DESCRIPTION,
                    "Retrieve functional enrichment for the current String network");
            propsEnrichment.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Retrieve the functional enrichment for the current String network." +
                            "This includes enrichment for GO Process, GO Component, GO Function, " +
                            "InterPro, KEGG Pathways, and PFAM.");
            propsEnrichment.setProperty(COMMAND_SUPPORTS_JSON, "true");
            propsEnrichment.setProperty(COMMAND_EXAMPLE_JSON, GetEnrichmentTaskFactory.EXAMPLE_JSON);
            // propsEnrichment.setProperty(INSERT_SEPARATOR_BEFORE, "true");
            registerService(bc, getCommandEnrichment, NetworkTaskFactory.class, propsEnrichment);
        }

        GetPublicationsTaskFactory getCommandPublications = new GetPublicationsTaskFactory(manager, false);
        {
            Properties propsPubl = new Properties();
            propsPubl.setProperty(COMMAND_NAMESPACE, "string");
            propsPubl.setProperty(COMMAND, "retrieve publications");
            propsPubl.setProperty(COMMAND_DESCRIPTION,
                    "Retrieve enriched publications for the current String network");
            propsPubl.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Retrieve the enriched PubMed publications for the current String network.");
            propsPubl.setProperty(COMMAND_SUPPORTS_JSON, "true");
            propsPubl.setProperty(COMMAND_EXAMPLE_JSON, GetPublicationsTaskFactory.EXAMPLE_JSON);
            registerService(bc, getCommandPublications, NetworkTaskFactory.class, propsPubl);
        }

        GetSpeciesTaskFactory getSpecies = new GetSpeciesTaskFactory(manager);
        {
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "list species");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Retrieve a list of the species for string.");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Retrieve the list of species known to string, including the texonomy ID.");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "[{\"taxonomyId\": 9606, \"scientificName\": \"Homo sapiens\", \"abbreviatedName\":\"Homo sapiens\"}]");
            // propsEnrichment.setProperty(INSERT_SEPARATOR_BEFORE, "true");
            registerService(bc, getSpecies, TaskFactory.class, props);
        }

		/*
		{
			OpenEvidenceTaskFactory openEvidence = new OpenEvidenceTaskFactory(manager);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps.IntAct");
			props.setProperty(TITLE, "Show evidence for association (if available)");
			props.setProperty(MENU_GRAVITY, "2.0");
			props.setProperty(IN_MENU_BAR, "true");
			registerService(bc, openEvidence, NodeViewTaskFactory.class, props);
		}
		*/
		
		/*
		{
			FindProteinsTaskFactory findProteins = new FindProteinsTaskFactory(manager);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps.IntAct");
			props.setProperty(TITLE, "Find proteins using text mining");
			props.setProperty(MENU_GRAVITY, "4.0");
			props.setProperty(IN_MENU_BAR, "true");
			registerService(bc, findProteins, TaskFactory.class, props);
		}
		*/

        {
            // Register our "show image" toggle
            ShowImagesTaskFactory showImagesTF = new ShowImagesTaskFactory(manager);
            showImagesTF.reregister();
            manager.setShowImagesTaskFactory(showImagesTF);
        }

        {
            // Register our show image commands
            ShowImagesTaskFactory showImagesTF = new ShowImagesTaskFactory(manager, true);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "show images");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Show the structure images on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Show the structure images on the nodes");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, showImagesTF, TaskFactory.class, props);
        }

        {
            // Register our hide image commands
            ShowImagesTaskFactory showImagesTF = new ShowImagesTaskFactory(manager, false);
            Properties props = new Properties();
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "hide images");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Hide the structure images on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Hide the structure images on the nodes");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
            registerService(bc, showImagesTF, TaskFactory.class, props);
        }

        {
            // Register our "show enhanced labels" toggle
            ShowEnhancedLabelsTaskFactory showEnhancedLabelsTF = new ShowEnhancedLabelsTaskFactory(manager);
            showEnhancedLabelsTF.reregister();
            manager.setShowEnhancedLabelsTaskFactory(showEnhancedLabelsTF);
        }

        {
            // Register our "show glass ball effect" toggle
            ShowGlassBallEffectTaskFactory showGlassBallEffectTF = new ShowGlassBallEffectTaskFactory(manager);
            showGlassBallEffectTF.reregister();
            manager.setShowGlassBallEffectTaskFactory(showGlassBallEffectTF);
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

        manager.info("String APP initialized");
    }

}
