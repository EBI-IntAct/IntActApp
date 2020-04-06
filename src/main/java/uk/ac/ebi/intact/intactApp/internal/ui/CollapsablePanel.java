package uk.ac.ebi.intact.intactApp.internal.ui;

import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CollapsablePanel extends JPanel {
    private static ImageIcon RIGHT_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/right_arrow.png");
    private static ImageIcon DOWN_ARROW = IconUtils.createImageIcon("/IntAct/DIGITAL/arrows/down_arrow.png");

    JPanel contentPanel_;
    HeaderPanel headerPanel_;

    public CollapsablePanel(Font iconFont, String text, JPanel panel, boolean collapsed) {
        this(iconFont, text, panel, collapsed, 14);
    }

    public CollapsablePanel(Font iconFont, String text, JPanel panel, boolean collapsed, int fontSize) {
        super();
        setLayout(new BorderLayout());

        headerPanel_ = new HeaderPanel(iconFont, text, collapsed, fontSize);

        setBackground(new Color(255, 255, 255));
        contentPanel_ = panel;
        contentPanel_.setBackground(new Color(0,0,0, 0));

        add(headerPanel_, BorderLayout.NORTH);

        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(30,1));
        separator.setUI(new BasicSeparatorUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Dimension s = c.getSize();
                int halfWidth = s.width / 2 - 1;
                g.setColor( c.getForeground() );
                g.drawLine( halfWidth, 0, halfWidth, s.height );

                g.setColor( c.getBackground() );
                g.drawLine( halfWidth + 1, 0, halfWidth + 1, s.height );
            }
        });

        add(separator, BorderLayout.WEST);
        add(contentPanel_, BorderLayout.CENTER);
        contentPanel_.setVisible(!collapsed);
    }

    public void setLabel(String label) {
        headerPanel_.setText(label);
    }

    public void addContent(JComponent panel) {
        contentPanel_.add(panel);
    }

    public JPanel getContent() {
        return contentPanel_;
    }

    public void toggleSelection() {
        if (contentPanel_.isShowing()) {
            headerPanel_.setButton(RIGHT_ARROW);
            contentPanel_.setVisible(false);
        } else {
            contentPanel_.setVisible(true);
            headerPanel_.setButton(DOWN_ARROW);
        }

        validate();

        headerPanel_.repaint();
    }

    public void collapse() {
        if (contentPanel_.isShowing())
            toggleSelection();
    }

    public void expand() {
        if (!contentPanel_.isShowing())
            toggleSelection();
    }

    private class HeaderPanel extends JPanel implements ActionListener {
        Font font;
        JButton expandButton;
        JLabel label;
        boolean expanded = false;

        public HeaderPanel(Font iconFont, String text, boolean collapsed, int fontSize) {
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
            expandButton.setFont(iconFont);
            expandButton.setPreferredSize(new Dimension(30,20));
            this.add(expandButton, c.anchor("west").noExpand());
            label = new JLabel(text);
//            label.setFont(font);
            this.add(label, c.right().expandHoriz());
            this.setBackground(new Color(255, 255, 255));
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setPreferredSize(new Dimension(200, 20));
        }

        public void setText(String text) {
            // System.out.println("Setting label text to: "+text);
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
