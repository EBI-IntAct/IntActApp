package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.OrthologyDatabaseUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.filters.RadioButtonFilter;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.tasks.view.parameters.OrthologyViewParameterTask;

public class OrthologyGroupingDatabaseFilter extends RadioButtonFilter<Node> {

    public OrthologyGroupingDatabaseFilter(NetworkView view) {
        super(view,
                "Ortholog Group database",
                "Select which database to use for orthology grouping",
                Node.class,
                NodeFields.ORTHOLOG_GROUP_ID,
                OrthologyViewParameterTask.DEFAULT_ORTHOLOGY_DB
        );
    }

    @Override
    public boolean isEnabled() {
        return getNetwork().getOrthologyDatabases(getGroupingField()).size() > 1;
    }

    public void setCurrentSelectedProperty(String currentSelectedProperty) {
        if (!currentSelectedProperty.equals(getCurrentSelectedProperty())) {
            super.setCurrentSelectedProperty(currentSelectedProperty);
            manager.utils.fireEvent(new OrthologyDatabaseUpdatedEvent(manager, currentSelectedProperty));
        }
    }

    @Override
    public void setProperties() {
        setProperties(getNetwork().getOrthologyDatabases(getGroupingField()));
    }

}
