package uk.ac.ebi.intact.intactApp.internal.utils;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.json.JSONResult;
import uk.ac.ebi.intact.intactApp.internal.model.CyNetworkJSONResult;

import java.util.Arrays;
import java.util.List;

public class StringResults {
    @SuppressWarnings("unchecked")
    public static <R> R getResults(Class<? extends R> clzz, CyNetwork loadedNetwork) {
        // System.out.println("Getresults called with "+clzz+" and "+loadedNetwork);
        // Return the network we created
        if (clzz.equals(CyNetwork.class)) {
            return (R) loadedNetwork;
        } else if (clzz.equals(Long.class)) {
            if (loadedNetwork == null)
                return null;
            return (R) loadedNetwork.getSUID();
            // We need to use the actual class rather than the interface so that
            // CyREST can inspect it to find the annotations
        } else if (clzz.equals(JSONResult.class)) {
            return (R) new CyNetworkJSONResult(loadedNetwork);
        } else if (clzz.equals(String.class)) {
            if (loadedNetwork == null)
                return (R) "No network was loaded";
            String resp = "Loaded network '"
                    + loadedNetwork.getRow(loadedNetwork).get(CyNetwork.NAME, String.class);
            resp += "' with " + loadedNetwork.getNodeCount() + " nodes and "
                    + loadedNetwork.getEdgeCount() + " edges";
            return (R) resp;
        }
        return null;
    }

    public static List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class, Long.class, CyNetwork.class);
    }

}
