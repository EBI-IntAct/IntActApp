package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import java.util.*;

public class GetSpeciesTask extends AbstractTask implements ObservableTask {
    final IntactManager manager;

    public GetSpeciesTask(IntactManager manager) {
        this.manager = manager;
    }

    public void run(TaskMonitor monitor) throws Exception {
        monitor.setTitle(this.getTitle());
    }

    @ProvidesTitle
    public String getTitle() {
        return "List species";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R getResults(Class<? extends R> clzz) {
        List<Species> speciesList = Species.getSpecies();
        if (clzz.equals(List.class)) {
            List<Map<String, String>> speciesMap = new ArrayList<>();
            for (Species species : speciesList) {
                Map<String, String> map = new HashMap<>();
                map.put("taxonomyId", "" + species.getTaxId());
                map.put("scientificName", species.getOfficialName());
                map.put("abbreviatedName", species.getName());
                speciesMap.add(map);
            }
            return (R) speciesMap;
        } else if (clzz.equals(String.class)) {
            StringBuilder sb = new StringBuilder();
            for (Species species : speciesList) {
                sb.append("Species: ")
                        .append(species.getName())
                        .append(", Tax ID: ")
                        .append(species.getTaxId())
                        .append("\n");
            }
            return (R) sb.toString();
        } else if (clzz.equals(JSONResult.class)) {
            JSONResult res = () -> {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                int count = speciesList.size();
                int index = 0;
                for (Species species : speciesList) {
                    sb.append("{\"scientificName\":\"")
                            .append(species.getOfficialName())
                            .append("\", \"abbreviatedName\":\"")
                            .append(species.getName())
                            .append("\", \"taxonomyId\":")
                            .append(species.getTaxId())
                            .append("}");
                    index++;
                    if (index < count)
                        sb.append(",");
                }
                sb.append("]");
                return sb.toString();
            };
            return (R) res;
        }
        return null;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class, List.class);
    }
}
