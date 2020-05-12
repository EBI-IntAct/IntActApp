package uk.ac.ebi.intact.intactApp.internal.ui.components;

import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CollapsablePanel extends JPanel {
    private static ImageIcon RIGHT_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/right_arrow.png");
    private static ImageIcon DOWN_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/down_arrow.png");

    protected JPanel content;
    private final HeaderPanel headerPanel;
    private final JSeparator separator;

    public CollapsablePanel(String text, boolean collapsed) {
        this(text, new JPanel(), collapsed, 20);
    }

    public CollapsablePanel(String text, JPanel panel, boolean collapsed) {
        this(text, panel, collapsed, 14);
    }

    public CollapsablePanel(String text, JPanel panel, boolean collapsed, int fontSize) {
        super();
        setLayout(new BorderLayout());

        content = panel;
        headerPanel = new HeaderPanel(text, collapsed, fontSize);

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
    }

    public void setLabel(String label) {
        headerPanel.setText(label);
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

    public void addContent(JComponent panel) {
        content.add(panel);
    }

    public JPanel getContent() {
        return content;
    }

    public void toggleSelection() {
        if (content.isShowing()) {
            headerPanel.setButton(RIGHT_ARROW);
            content.setVisible(false);
            separator.setVisible(false);
        } else {
            headerPanel.setButton(DOWN_ARROW);
            content.setVisible(true);
            separator.setVisible(true);
        }

        validate();

        headerPanel.repaint();
    }

    public void collapse() {
        if (content.isShowing())
            toggleSelection();
    }

    public void expand() {
        if (!content.isShowing())
            toggleSelection();
    }

    private class HeaderPanel extends JPanel implements ActionListener {
        Font font;
        JButton expandButton;
        JLabel label;
        boolean expanded = false;

        public HeaderPanel(String text, boolean collapsed, int fontSize) {
            font = new Font("sans-serif", Font.BOLD, fontSize);

            this.setLayout(new GridBagLayout());
            this.expanded = !collapsed;

            EasyGBC c = new EasyGBC();

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
            this.add(expandButton, c.anchor("west").noExpand());
            label = new JLabel(text);
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            this.add(label, c.right().anchor("west").expandHoriz());
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        public void setText(String text) {
            label.setText("<html>" + text + "</html>");
        }

        public void actionPerformed(ActionEvent e) {
            toggleSelection();
        }

        public void setButton(Icon buttonState) {
            expandButton.setIcon(buttonState);
        }

    }

}
