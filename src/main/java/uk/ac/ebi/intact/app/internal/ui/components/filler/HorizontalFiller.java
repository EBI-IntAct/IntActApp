package uk.ac.ebi.intact.app.internal.ui.components.filler;

import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class HorizontalFiller extends JComponent {

//    public static final Color BEGIN_COLOR = new Color(161, 135, 184);
//    public static final Color END_COLOR = new Color(208, 196, 214);
    public static final Color BEGIN_COLOR = new Color(47, 33, 89);
    public static final Color END_COLOR = new Color(134, 56, 148);

    public HorizontalFiller() {
        setOpaque(false);
    }
    public static final ImageIcon WHITE_LOGO = IconUtils.createImageIcon("/IntAct/DIGITAL/ICON_PNG/Logo_White_250x82_TransparentBG_300dpi.png");

    private static BufferedImage logo;
    private static float widthHeightRatio;

    static {
        try {
            URL url = IconUtils.getResourceURL("/IntAct/DIGITAL/ICON_PNG/Logo_White_250x82_TransparentBG_300dpi.png");
            if (url != null) {
                logo = ImageIO.read(url);
                widthHeightRatio = (float) logo.getWidth() / logo.getHeight();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(logo.getWidth(), logo.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);

        g2.setPaint(new GradientPaint(0f, 0f, BEGIN_COLOR, getWidth(), 0f, END_COLOR));
        g2.fillRect(insets.left, insets.top, getWidth(), getHeight());

        int newHeight = getHeight() - insets.top - insets.right;
        BufferedImage resized = resizeImage(logo, Math.round(widthHeightRatio * newHeight), newHeight);
//        final TexturePaint texture = new TexturePaint(resized, new Rectangle(resized.getWidth(), resized.getHeight()));
//        g2.setPaint(texture);
//        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.drawImage(resized, AffineTransform.getTranslateInstance(getWidth() - resized.getWidth() - insets.right - 5, insets.top), null);

//        g2.setPaint(new GradientPaint(0f, 0f, new Color(255,255,255,0), getWidth(), 0f, END_COLOR));
//        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public BufferedImage resizeImage(BufferedImage img, int newWidth, int newHeight) {
        Image tmp = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}

