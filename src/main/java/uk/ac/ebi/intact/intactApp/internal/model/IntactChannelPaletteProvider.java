package uk.ac.ebi.intact.intactApp.internal.model;

import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntactChannelPaletteProvider implements PaletteProvider {
    Palette[] palettes = null;

    public IntactChannelPaletteProvider() {
        palettes = new Palette[]{new StringChannelPalette()};
    }

    public Palette getPalette(Object paletteIdentifier) {
        for (Palette palette : palettes) {
            if (palette.getIdentifier().equals(paletteIdentifier))
                return palette;
        }
        return null;
    }

    public Palette getPalette(Object paletteIdentifier, int size) {
        for (Palette palette : palettes) {
            if (palette.getIdentifier().equals(paletteIdentifier))
                return palette;
        }
        return null;
    }

    public Palette getPalette(String paletteName) {
        for (Palette palette : palettes) {
            if (palette.getName().equals(paletteName))
                return palette;
        }
        return null;
    }

    public Palette getPalette(String paletteName, int size) {
        for (Palette palette : palettes) {
            if (palette.getName().equals(paletteName))
                return palette;
        }
        return null;
    }

    public List<PaletteType> getPaletteTypes() {
        return Collections.singletonList(BrewerType.QUALITATIVE);
    }

    public String getProviderName() {
        return "STRING";
    }

    public List<Object> listPaletteIdentifiers(PaletteType type, boolean colorBlindSafe) {
        List<Object> paletteIds = new ArrayList<>();
        if (type != BrewerType.QUALITATIVE) return paletteIds;
        for (Palette palette : palettes) {
            paletteIds.add(palette.getIdentifier());
        }
        return paletteIds;
    }

    public List<String> listPaletteNames(PaletteType type, boolean colorBlindSave) {
        List<String> paletteIds = new ArrayList<>();
        if (type != BrewerType.QUALITATIVE) return paletteIds;
        for (Palette palette : palettes) {
            paletteIds.add(palette.getName());
        }
        return paletteIds;
    }

    static class StringChannelPalette implements Palette {
        Color[] colors = new Color[]{
                Color.CYAN, Color.MAGENTA, Color.GREEN, Color.RED, Color.BLUE,
                new Color(199, 234, 70), Color.BLACK};

        public Color[] getColors() {
            return colors;
        }

        public Color[] getColors(int nColors) {
            return colors;
        }

        public Object getIdentifier() {
            return "default channel colors";
        }

        public PaletteType getType() {
            return BrewerType.QUALITATIVE;
        }

        public boolean isColorBlindSafe() {
            return false;
        }

        public int size() {
            return 7;
        }

        public String toString() {
            return "default channel colors";
        }

        public String getName() {
            return "default";
        }
    }

}
