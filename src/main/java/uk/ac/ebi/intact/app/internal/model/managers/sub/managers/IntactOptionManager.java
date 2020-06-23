package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.property.CyProperty;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.PropertyUtils.*;

public class IntactOptionManager {
    public final IntactManager manager;
    public final CyProperty<Properties> propertyService;
    public final List<Option<?>> options = new ArrayList<>();
    public final Map<Scope, List<Option<?>>> scopeOptions = new HashMap<>();

    public IntactOptionManager(IntactManager manager) {
        this.manager = manager;
        this.propertyService = getPropertyService(manager, CyProperty.SavePolicy.CONFIG_DIR);
    }

    public final NumericOption<Integer> MAX_INTERACTOR_PER_TERM = new NumericOption<>("maxInteractorPerTerm", "Maximal number of interactors shown per terms", Integer.class, 500, 1, 1000, List.of(Scope.SEARCH));
    public final Option<Boolean> INCLUDE_UNSEEN_INTERACTORS = new Option<>("includeUnseenInteractors", "DEFAULT: Include interactors after visible limit reached", Boolean.class, true, List.of(Scope.SEARCH));
    public final Option<Boolean> ADD_INTERACTING_PARTNERS = new Option<>("addingInteractingPartners", "Add interacting partners of seed interactors to network", Boolean.class, true, List.of(Scope.SEARCH, Scope.DISAMBIGUATION));

    public class Option<T> {
        public final String key;
        public final String label;
        public final Class<T> type;
        public final T defaultValue;
        public final List<Scope> scopes;

        Option(String key, String label, Class<T> type, T defaultValue, List<Scope> visibleAt) {
            this.key = key;
            this.label = label;
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
        }
    }

    public class NumericOption<N extends Number> extends Option<N> {
        public final N min;
        public final N max;

        public NumericOption(String key, String label, Class<N> type, N defaultValue, N min, N max, List<Scope> scopes) {
            super(key, label, type, defaultValue, scopes);
            this.min = min;
            this.max = max;
        }
    }

    public enum Scope {
        SEARCH,
        DISAMBIGUATION
    }
}
