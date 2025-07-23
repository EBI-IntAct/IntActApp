package uk.ac.ebi.intact.app.internal.ui.components.query;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.MIQLParser;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RulePanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleSetPanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Rule;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleSet;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.setButtonIntactPurple;

public class AdvancedSearchQueryComponent {
    static int frameWidth = 2000;
    private JFrame frame;

    @Getter
    private final JTextPane queryTextField = new JTextPane();

    public final RuleSetPanel ruleSetPanel = new RuleSetPanel(this, null);

    @Getter
    private final JButton buildQueryButton = new JButton("Build query");

    private final MIQLParser miqlParser = new MIQLParser();

    @Setter
    Runnable onBuildQuery;

    private final Logger logger = Logger.getLogger(AdvancedSearchQueryComponent.class);

    public void getFrame(String input) {
        queryTextField.setText(input);
        frame = new JFrame("Advanced Search Query Builder");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(frameWidth, 1000);
        frame.setLayout(new BorderLayout());

        JPanel pageStartContainer = new JPanel(new GridLayout(2, 1));
        pageStartContainer.setSize(frameWidth, 50);

        JPanel queryContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        queryContainer.add(getQueryInputField(), BorderLayout.LINE_START);
        queryContainer.add(getBuildQueryButtonContainer(), BorderLayout.CENTER);

        pageStartContainer.add(queryContainer);

        frame.add(pageStartContainer, BorderLayout.PAGE_START);
        frame.add(getRuleScrollPane(), BorderLayout.CENTER);

        frame.setVisible(true);

        // When we first create this component, if it is initialized with a valid query,
        // then we parse it and build the buttons from the query.
        if (validateQueryText()) {
            buildButtonsFromQueryText();
        }
    }

    private JPanel getBuildQueryButtonContainer() {
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER));

        setButtonIntactPurple(buildQueryButton);
        buildQueryButton.addActionListener(e -> {
            Action submitAction = queryTextField.getActionMap().get("submitQuery");
            if (submitAction != null) {
                submitAction.actionPerformed(new ActionEvent(queryTextField, ActionEvent.ACTION_PERFORMED, null));
            }
        });


        buttonContainer.add(buildQueryButton);
        return buttonContainer;
    }

    public void setQueryText(String query) {
        queryTextField.setText(query);
        highlightQuery(queryTextField.getText());
    }

    public String getFullQuery() {
        return ruleSetPanel.getQuery().substring(1, ruleSetPanel.getQuery().length() - 1);
    }

    private Container getRuleScrollPane() {
        JPanel container = ruleSetPanel.getContainer();
        container.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane();

        container.setAutoscrolls(true);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(container);

        return scrollPane;
    }

    private boolean validateQueryText() {
        String input = queryTextField.getText();
        if (input != null && !input.isEmpty()) {
            highlightQuery(input);
            RuleSet parsedQuery = miqlParser.parseMIQL(input);
            return parsedQuery != null && parsedQuery.rules != null && !parsedQuery.rules.isEmpty();
        }
        return false;
    }

    private void buildButtonsFromQueryText() {
        String input = queryTextField.getText();
        RuleSet parsedQuery = miqlParser.parseMIQL(input);

        modifyComboboxFromQuery(parsedQuery, 0, ruleSetPanel);
        String builtQuery = getFullQuery();
        highlightQuery(builtQuery);
    }

    private JPanel getQueryInputField() {
        JPanel queryInputFieldContainer = new JPanel();

        queryTextField.setMinimumSize(new Dimension(frameWidth, 30));
        queryTextField.setPreferredSize(new Dimension(frameWidth / 2, 30));
        queryTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        queryTextField.setVisible(true);

        queryTextField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "validateQuery");
        queryTextField.getActionMap().put("validateQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateQueryText()) {
                    buildButtonsFromQueryText();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to parse query. Please check the syntax.");
                }
            }
        });
        queryTextField.getActionMap().put("submitQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateQueryText()) {
                    buildButtonsFromQueryText();
                    if (onBuildQuery != null) {
                        onBuildQuery.run();
                    }
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to parse query. Please check the syntax.");
                }
            }
        });

        queryInputFieldContainer.add(queryTextField);
        return queryInputFieldContainer;
    }

    private RuleSetPanel modifyComboboxFromQuery(RuleSet ruleSet, int indentLevel, RuleSetPanel currentPanel) {
        if (indentLevel == 0) {
            currentPanel = ruleSetPanel;
            currentPanel.clearContent();
        } else {
            currentPanel = new RuleSetPanel(this, currentPanel);
        }

        currentPanel.getQueryOperators().setRuleOperator(ruleSet.condition);
        currentPanel.getQueryOperators().updateAndOrButtons();

        for (RuleComponent ruleComponent : ruleSet.rules) {
            if (ruleComponent instanceof RuleSet) {
                RuleSet nestedRuleSet = (RuleSet) ruleComponent;
                RuleSetPanel nestedPanel = modifyComboboxFromQuery(nestedRuleSet, indentLevel + 1, currentPanel);

                currentPanel.addRuleSetPanel(nestedPanel);

            } else if (ruleComponent instanceof Rule) {
                Rule rule = (Rule) ruleComponent;
                RulePanel rulePanel = new RulePanel(this, currentPanel);

                rulePanel.entityComboBox.setSelectedItem(rule.getEntity());
                rulePanel.entityPropertiesCombobox.setSelectedItem(rule.getFieldName());
                if (rulePanel.isUserInputNeeded()) {
                    rulePanel.operatorsComboBox.setSelectedItem(rule.getOperator());
                    rulePanel.userInputProperty.setText(rule.getUserInput1());
                } else {
                    rulePanel.operatorsComboBox.setSelectedItem(rule.getUserInput1().toUpperCase());
                }
                rulePanel.userInputProperty2.setText(rule.getUserInput2());


                currentPanel.addRulePanel(rulePanel);
            }
        }

        if (indentLevel == 0) {
            ruleSetPanel.getContainer().revalidate();
            ruleSetPanel.getContainer().repaint();
            return null;
        } else {
            return currentPanel;
        }
    }

    private void highlightQuery(String query) {
        StyledDocument doc = queryTextField.getStyledDocument();
        doc.removeUndoableEditListener(null);

        queryTextField.setText("");

        Style defaultStyle = queryTextField.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);

        Style operatorStyle = queryTextField.addStyle("operator", null);
        StyleConstants.setForeground(operatorStyle, new Color(229, 191, 86));

        Style miQLStyle = queryTextField.addStyle("keyword", null);
        StyleConstants.setForeground(miQLStyle, new Color(165, 87, 202));

        Style ruleStyle = queryTextField.addStyle("rule", null);
        StyleConstants.setForeground(ruleStyle, new Color(58, 132, 176));

        String[] tokens = query.split("((?<=\\W)|(?=\\W))");

        for (String token : tokens) {
            Style styleToUse = defaultStyle;

            if (token.equals("AND") || token.equals("OR") || token.equals("NOT") ||
                    token.equals("and") || token.equals("or") || token.equals("not")) {
                styleToUse = operatorStyle;
            } else if (token.matches(Field.getMiQlRegex())) {
                styleToUse = miQLStyle;
            } else if (token.matches("[(:)]")) {
                styleToUse = ruleStyle;
            }

            try {
                doc.insertString(doc.getLength(), token, styleToUse);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
