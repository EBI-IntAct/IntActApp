package uk.ac.ebi.intact.app.internal.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class IconUtils {

    private IconUtils() {
        // ...
    }

    public static URL getResourceURL(String path) {
        ClassLoader cl = IconUtils.class.getClassLoader();
        return cl.getResource(path);
    }

    public static ImageIcon createImageIcon(String path) {
        URL imgURL = getResourceURL(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, null);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static ImageIcon createImageIcon(String path, int w, int h) {
        URL imgURL = getResourceURL(path);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL)
                    .getImage()
                    .getScaledInstance(w, h, Image.SCALE_SMOOTH),
                    null);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static File createFile(String path) {
        URL url = getResourceURL(path);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                System.err.println("Couldn't convert to URI : " + url);
            }
        } else {
            System.err.println("Couldn't find file: " + path);
        }
        return null;
    }

}
