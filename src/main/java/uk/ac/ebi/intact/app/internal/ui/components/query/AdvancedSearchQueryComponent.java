package uk.ac.ebi.intact.app.internal.ui.components.query;

import lombok.Getter;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.*;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RulePanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleSetPanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Rule;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleSet;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdvancedSearchQueryComponent {
    static int frameWidth = 2000;
    private JFrame frame;

    @Getter
    private final JTextPane queryTextField = new JTextPane();

    public final JPanel rulesPanel = new JPanel();

    @Getter
    private final ArrayList<Object> panels = new ArrayList<>();

    @Getter
    private final JButton buildQueryButton = new JButton("Build query");

    private final QueryOperators queryOperators = new QueryOperators(this, panels);
    private final MIQLParser miqlParser = new MIQLParser();

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

        JPanel buttonsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsContainer.add(queryOperators.getButtons(rulesPanel), BorderLayout.LINE_END);

        pageStartContainer.add(queryContainer);
        pageStartContainer.add(buttonsContainer);

        frame.add(pageStartContainer, BorderLayout.PAGE_START);
        frame.add(getRuleScrollPane(), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JPanel getBuildQueryButtonContainer(){
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER));

        setButtonIntactPurple(buildQueryButton);
        buildQueryButton.addActionListener(e -> {

            String fullQuery = getFullQuery();
            queryTextField.setText(fullQuery);
            highlightQuery(fullQuery);

            Action submitAction = queryTextField.getActionMap().get("submitQuery");
            if (submitAction != null) {
                submitAction.actionPerformed(new ActionEvent(queryTextField, ActionEvent.ACTION_PERFORMED, null));
            }

            frame.dispose();
        });


        buttonContainer.add(buildQueryButton);
        return buttonContainer;
    }

    public String getFullQuery() {
        return panels.stream()
                .map(panel -> {
                    if (panel instanceof RuleSetPanel) {
                        return ((RuleSetPanel) panel).getQuery();
                    } else if (panel instanceof RulePanel) {
                        return ((RulePanel) panel).getQuery();
                    }
                    logger.warn("Panel type does not return query: " + panel.getClass().getName());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" " + queryOperators.getRuleOperator() + " "));
    }

    private JScrollPane getRuleScrollPane() {
        JScrollPane scrollPane = new JScrollPane();

        rulesPanel.setAutoscrolls(true);
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(rulesPanel);

        return scrollPane;
    }

    private JPanel getQueryInputField(){
        JPanel queryInputFieldContainer = new JPanel();

        queryTextField.setMinimumSize(new Dimension(frameWidth, 25));
        queryTextField.setPreferredSize(new Dimension(frameWidth/2, 25));
        queryTextField.setVisible(true);

        queryTextField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "submitQuery");
        queryTextField.getActionMap().put("submitQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String input = queryTextField.getText();
                highlightQuery(input);

                RuleSet parsedQuery = miqlParser.parseMIQL(input);
                if (parsedQuery != null && parsedQuery.rules != null && !parsedQuery.rules.isEmpty()) {
                    queryOperators.setRuleOperator(parsedQuery.condition);
                    queryOperators.updateAndOrButtons();

                    modifyComboboxFromQuery(parsedQuery, 0);
                    String builtQuery = getFullQuery();
                    highlightQuery(builtQuery);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to parse query. Please check the syntax.");
                }
            }
        });

        queryInputFieldContainer.add(queryTextField);
        return queryInputFieldContainer;
    }

    private RuleSetPanel modifyComboboxFromQuery(RuleSet ruleSet, int indentLevel) {
        if (indentLevel == 0) {
            rulesPanel.removeAll();
            panels.clear();
        }

        RuleSetPanel currentRuleSetPanel = null;

        if (indentLevel != 0) {
            currentRuleSetPanel = new RuleSetPanel(this);
            currentRuleSetPanel.getPanels().clear();
            currentRuleSetPanel.getQueryOperators().setRuleOperator(ruleSet.condition);
            currentRuleSetPanel.getQueryOperators().updateAndOrButtons();
        }

        for (Object ruleComponent : ruleSet.rules) {
            if (ruleComponent instanceof RuleSet) {
                RuleSet nestedRuleSet = (RuleSet) ruleComponent;
                RuleSetPanel nestedPanel = modifyComboboxFromQuery(nestedRuleSet, indentLevel + 1);


                if (nestedPanel != null) {
                    if (indentLevel == 0) {
                        rulesPanel.add(nestedPanel.getRuleSetPanel());
                        panels.add(nestedPanel);
                    } else {
                        currentRuleSetPanel.addRuleSetPanel(nestedPanel);
                    }
                }

            } else if (ruleComponent instanceof Rule) {
                Rule rule = (Rule) ruleComponent;
                RulePanel rulePanel = new RulePanel(this);

                rulePanel.entityComboBox.setSelectedItem(rule.getEntity());
                rulePanel.entityPropertiesCombobox.setSelectedItem(rule.getFieldName());
                rulePanel.operatorsComboBox.setSelectedItem(rule.getOperator());
                rulePanel.userInputProperty.setText(rule.getUserInput1());
                rulePanel.userInputProperty2.setText(rule.getUserInput2());

                if (indentLevel == 0) {
                    rulesPanel.add(rulePanel.getOneRuleBuilderPanel());
                    panels.add(rulePanel);
                } else {
                    currentRuleSetPanel.addRulePanel(rulePanel);
                }
            }
        }

        if (indentLevel == 0) {
            rulesPanel.revalidate();
            rulesPanel.repaint();
            return null;
        } else {
            return currentRuleSetPanel;
        }
    }

    public void highlightQuery(String query) {
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

            if (token.equals("AND") || token.equals("OR") || token.equals("NOT")) {
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
