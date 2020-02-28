package uk.ac.ebi.intact.intactApp.internal.utils;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import java.util.HashMap;
import java.util.Map;

public class OpenCyBrowser {
    public static boolean openURL(IntactManager manager, String id, String url) {
        if (!manager.haveCyBrowser())
            return false;

        if (id == null)
            id = "String";
        Map<String, Object> args = new HashMap<>();
        args.put("url", url);
        args.put("id", id);
        args.put("newTab", "true");
        manager.executeCommand("cybrowser", "dialog", args, null);
        return true;
    }

}
