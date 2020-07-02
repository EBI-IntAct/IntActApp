package uk.ac.ebi.intact.app.internal.ui.components.panels;

import uk.ac.ebi.intact.app.internal.ui.components.buttons.CollapseAllButton;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;

public class CollapsablePanel extends JPanel implements ContainerListener {
    private static final ImageIcon RIGHT_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/right_arrow.png");
    private static final ImageIcon DOWN_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/down_arrow.png");

    protected JPanel content;
    private final HeaderPanel headerPanel;
    private final JSeparator separator;
    private final List<CollapsablePanel> subCollapsablePanels = new ArrayList<>();
    public final CollapseAllButton collapseAllButton = new CollapseAllButton(true, subCollapsablePanels);
    private boolean collapsed;

    public CollapsablePanel(String text, boolean collapsed) {
        this(text, new VerticalPanel(), collapsed);
    }

    public CollapsablePanel(String text, JPanel panel, boolean collapsed) {
        super();
        setLayout(new BorderLayout());
        this.collapsed = collapsed;
        content = panel;
        headerPanel = new HeaderPanel(text, collapsed);

        add(headerPanel, BorderLayout.NORTH);

        separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(20, 1));
        separator.setUI(new BasicSeparatorUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Dimension s = c.getSize();
                int halfWidth = s.width / 2 - 1;
                g.setColor(c.getForeground());
                g.drawLine(halfWidth, 0, halfWidth, s.height);

                g.setColor(c.getBackground());
                g.drawLine(halfWidth + 1, 0, halfWidth + 1, s.height);
            }
        });

        add(separator, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        content.setVisible(!collapsed);
        separator.setVisible(!collapsed);
        setBackground(panel.getBackground());

        for (Component component : content.getComponents()) {
            if (component instanceof CollapsablePanel) {
                addSubCollapsablePanel((CollapsablePanel) component);
            }
        }
        content.addContainerListener(this);
    }

    public void setHeader(JComponent component) {
        headerPanel.setHeaderComponent(component);
    }

    public boolean isExpanded() {
        return !collapsed;
    }

    public boolean isCollapsed() {
        return collapsed;
    }


    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (headerPanel != null) {
            headerPanel.setBackground(bg);
            separator.setBackground(bg);
            content.setBackground(bg);
        }
    }

    public void addContent(JComponent component) {
        content.add(component);
    }


    public JPanel getContent() {
        return content;
    }

    public void toggleSelection() {
        if (content.isShowing()) {
            headerPanel.setButton(RIGHT_ARROW);
            content.setVisible(false);
            separator.setVisible(false);
            collapseAllButton.setEnabled(false);
        } else {
            headerPanel.setButton(DOWN_ARROW);
            content.setVisible(true);
            separator.setVisible(true);
            collapseAllButton.setEnabled(true);
        }
        collapseAllButton.setVisible(subCollapsablePanels.size() >= 2);
        collapsed = !collapsed;

        validate();

        headerPanel.repaint();
    }

    public void collapse() {
        if (content.isShowing()) {
            toggleSelection();
        }
    }

    public void expand() {
        if (!content.isShowing()) {
            toggleSelection();
        }
    }

    @Override
    public void componentAdded(ContainerEvent e) {
        Component component = e.getChild();
        if (component instanceof CollapsablePanel) {
            addSubCollapsablePanel((CollapsablePanel) component);
        }
    }

    @Override
    public void componentRemoved(ContainerEvent e) {
        Component component = e.getChild();
        if (component instanceof CollapsablePanel) {
            removeSubCollapsablePanel((CollapsablePanel) component);
        }
    }

    private void addSubCollapsablePanel(CollapsablePanel panel) {
        subCollapsablePanels.add(panel);
        if (subCollapsablePanels.size() >= 2) {
            collapseAllButton.setVisible(true);
        }
        setInitCollapseAllButtonPosition(panel);
    }

    private void removeSubCollapsablePanel(CollapsablePanel panel) {
        subCollapsablePanels.remove(panel);
        if (subCollapsablePanels.size() < 2) {
            collapseAllButton.setVisible(false);
        }
    }

    private boolean initialButtonPositionSet = false;

    private void setInitCollapseAllButtonPosition(CollapsablePanel panel) {
        if (!initialButtonPositionSet) {
            collapseAllButton.setOnExpandAll(panel.isCollapsed());
            initialButtonPositionSet = true;
        }
    }

    private class HeaderPanel extends LinePanel implements ActionListener {
        JButton expandButton;
        JComponent headerComponent;
        boolean expanded;
        public final EmptyBorder BORDER = new EmptyBorder(0, 4, 0, 0);


        public HeaderPanel(String text, boolean collapsed) {
            this.expanded = !collapsed;

            if (collapsed)
                expandButton = new JButton(RIGHT_ARROW);
            else
                expandButton = new JButton(DOWN_ARROW);
            expandButton.addActionListener(this);
            expandButton.setBorderPainted(false);
            expandButton.setContentAreaFilled(false);
            expandButton.setOpaque(false);
            expandButton.setFocusPainted(false);

            expandButton.setPreferredSize(new Dimension(20, 20));
            this.add(expandButton);
            headerComponent = new JLabel(text);
            headerComponent.setBorder(BORDER);
            this.add(headerComponent);
            collapseAllButton.setBorder(BORDER);
            collapseAllButton.setVisible(false);
            collapseAllButton.setEnabled(!collapsed);
            this.add(collapseAllButton);
            this.add(Box.createHorizontalGlue());
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        public void setHeaderComponent(JComponent component) {
            component.setBorder(BORDER);
            this.remove(1);
            this.add(component, 1);
        }

        public void actionPerformed(ActionEvent e) {
            toggleSelection();
        }

        public void setButton(Icon buttonState) {
            expandButton.setIcon(buttonState);
        }
    }
}
