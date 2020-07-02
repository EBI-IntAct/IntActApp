package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;
import static uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel.labelFont;

public abstract class AbstractSelectedElementPanel extends JPanel {
    protected final OpenBrowser openBrowser;
    protected final JPanel content = new JPanel();
    private final EasyGBC layoutHelper = new EasyGBC();
    protected boolean titled;

    public AbstractSelectedElementPanel(String title, OpenBrowser openBrowser) {
        this.openBrowser = openBrowser;
        this.titled = title != null && !title.isBlank();
        setLayout(new GridBagLayout());
        setBackground(lightBackground);
        setAlignmentX(LEFT_ALIGNMENT);
        if (titled) {
            initTitle(title);
        }
        initContent();
    }

    private void initTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(labelFont);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(lbl, layoutHelper.anchor("west").down().noExpand());
    }

    private void initContent() {
        if (titled) {
            content.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, lightBackground));
        }
        content.setAlignmentX(LEFT_ALIGNMENT);
        content.setAlignmentY(TOP_ALIGNMENT);
        content.setBackground(lightBackground);
        add(content, layoutHelper.anchor("west").down().expandBoth());
    }

    protected abstract void fillContent();

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (content != null) {
            content.setBackground(bg);
            if (titled)
                content.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, bg));
        }

    }
}
