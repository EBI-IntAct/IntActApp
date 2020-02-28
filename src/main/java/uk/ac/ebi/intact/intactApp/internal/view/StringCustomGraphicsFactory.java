package uk.ac.ebi.intact.intactApp.internal.view;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import java.net.URL;

public class StringCustomGraphicsFactory implements CyCustomGraphicsFactory<StringLayer> {
    IntactManager manager;

    public StringCustomGraphicsFactory(IntactManager manager) {
        this.manager = manager;
    }

    public CyCustomGraphics<StringLayer> getInstance(String input) {
        return new StringCustomGraphics(manager, input);
    }

    public CyCustomGraphics<StringLayer> getInstance(URL url) {
        return null;
    }

    public Class<? extends CyCustomGraphics> getSupportedClass() {
        return StringCustomGraphics.class;
    }

    public CyCustomGraphics<StringLayer> parseSerializableString(String string) {
        return null;
    }

    public boolean supportsMime(String mimeType) {
        return false;
    }

    public String getPrefix() {
        return "string";
    }

}
