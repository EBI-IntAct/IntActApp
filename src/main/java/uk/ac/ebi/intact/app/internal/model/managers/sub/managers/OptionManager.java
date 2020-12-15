package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.property.CyProperty;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.PropertyUtils.*;

public class OptionManager {
    public final Manager manager;
    public final CyProperty<Properties> propertyService;
    private final Properties propertyServiceProperties = new Properties();
    public static final String DIR_PROPERTIES = "intactApp";
    public final List<Option<?>> options = new ArrayList<>();
    public final Map<Scope, List<Option<?>>> scopeOptions = new HashMap<>();

    public OptionManager(Manager manager) {
        this.manager = manager;
        this.propertyService = getPropertyService(manager, CyProperty.SavePolicy.CONFIG_DIR, DIR_PROPERTIES);
        propertyServiceProperties.setProperty("cyPropertyName", DIR_PROPERTIES);
    }

    public final NumericOption<Integer> MAX_INTERACTOR_PER_TERM = new NumericOption<>("maxInteractorPerTerm","Max #interactors/term", "Maximum number of matching interactors shown", Integer.class, 25, 1, 1000, List.of(Scope.SEARCH));
    public final Option<Boolean> DEFAULT_INCLUDE_ALL_INTERACTORS = new Option<>("includeUnseenInteractors", "Add extra interactors by default", "Include extra choices in search when maximum exceeded", Boolean.class, true, List.of(Scope.SEARCH));
//    public final Option<Boolean> SHOW_HIGHLIGHTS = new Option<>("showHighlights", title,  "Highlight matching columns", Boolean.class, true, List.of(Scope.DISAMBIGUATION));
    public final Option<Boolean> ADD_INTERACTING_PARTNERS = new Option<>("addingInteractingPartners", "Include first neighbours", "Add interacting partners of seed interactors to network", Boolean.class, true, List.of(Scope.SEARCH, Scope.DISAMBIGUATION));
    public final NumericOption<Integer> MAX_SELECTED_NODE_INFO_SHOWN = new NumericOption<>("maxSelectedNodeInfoShown","Max selected nodes", "Maximum number of selected nodes shown in details", Integer.class, 15, 0, 100, new ArrayList<>());
    public final NumericOption<Integer> MAX_SELECTED_EDGE_INFO_SHOWN = new NumericOption<>("maxSelectedEdgeInfoShown","Max selected edges", "Maximum number of selected edges shown in details", Integer.class, 15, 0, 100, new ArrayList<>());

    public class Option<T> {
        public final String key;
        public final String title;
        public final String description;
        public final Class<T> type;
        public final T defaultValue;
        public final List<Scope> scopes;

        Option(String key, String title, String description, Class<T> type, T defaultValue, List<Scope> visibleAt) {
            this.key = key;
            this.title = title;
            this.description = description;
            this.type = type;
            this.defaultValue = defaultValue;
            this.scopes = visibleAt;
            options.add(this);
            visibleAt.forEach(scope -> CollectionUtils.addToGroups(scopeOptions, this, option -> scope));
        }

        public T getValue() {
            if (hasProperty(propertyService, key)) {
                if (type == Integer.class) {
                    return type.cast(getIntegerProperty(propertyService, key));
                } else if (type == Boolean.class) {
                    return type.cast(getBooleanProperty(propertyService, key));
                } else if (type == Double.class) {
                    return type.cast(getDoubleProperty(propertyService, key));
                } else if (type == String.class) {
                    return type.cast(getStringProperty(propertyService, key));
                }
                throw new IllegalStateException(type + " is not supported by properties");
            } else {
                setValue(defaultValue);
                return defaultValue;
            }
        }

        public void setValue(T value) {
            setStringProperty(propertyService, key, value.toString());
            manager.utils.registerAllServices(propertyService, propertyServiceProperties);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option<?> option = (Option<?>) o;
            return key.equals(option.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public class NumericOption<N extends Number> extends Option<N> {
        public final N min;
        public final N max;

        public NumericOption(String key, String title, String description, Class<N> type, N defaultValue, N min, N max, List<Scope> scopes) {
            super(key, title, description, type, defaultValue, scopes);
            this.min = min;
            this.max = max;
        }
    }

    public enum Scope {
        SEARCH,
        DISAMBIGUATION
    }
}
