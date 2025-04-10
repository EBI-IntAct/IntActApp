package uk.ac.ebi.intact.app.internal.ui.components.query;

import lombok.Getter;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.*;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RulePanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleSetPanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Rule;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleSet;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AdvancedSearchQueryComponent {
    static int frameWidth = 2000;
    private JFrame frame;

    @Getter
    private final JTextField queryTextField = new JTextField();

    public final JPanel rulesPanel = new JPanel();

    @Getter
    private final ArrayList<Object> panels = new ArrayList<>();

    private final QueryOperators queryOperators = new QueryOperators(this, panels);
    private final MIQLParser miqlParser = new MIQLParser();

    public static void main(String[] args) {
//        for test purposes

//        final String TEST_STRING = "NOT idA:IDA AND (interaction_id:ID AND pubid:PUBMEDID AND (source:DATABASE))";
//        final String TEST_STRING = "rdate:[12345 TO 6789] AND (taxidHost:12345)";
//        final String TEST_STRING = "(id:456 AND id:(758))";
        final String TEST_STRING = "NOT idA:IDA AND (interaction_id:(ID) AND pubid:PUBMEDID AND (source:DATABASE))"; //todo: check for the "in" which seems to create another ruleset?

        AdvancedSearchQueryComponent component = new AdvancedSearchQueryComponent();
//        component.getFrame(TEST_STRING);
    }

    public void getFrame(String input) {
        queryTextField.setText(input);
        frame = new JFrame("Advanced Search Query Builder");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(frameWidth, 1000);
        frame.setLayout(new BorderLayout());

        JPanel pageStartContainer = new JPanel(new BorderLayout());
        pageStartContainer.add(getQueryInputField(), BorderLayout.LINE_START);
        pageStartContainer.add(queryOperators.getButtons(rulesPanel), BorderLayout.LINE_END);

        frame.add(pageStartContainer, BorderLayout.PAGE_START);
        frame.add(getRuleScrollPane(), BorderLayout.CENTER);
        frame.add(getBuildQueryButtonContainer(), BorderLayout.PAGE_END);

        frame.setVisible(true);
    }

    private JPanel getBuildQueryButtonContainer(){
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton buildQueryButton = new JButton("Build query");

        setButtonIntactPurple(buildQueryButton);
        buildQueryButton.addActionListener(e ->{
            queryTextField.setText(getFullQuery());
            for (ActionListener actionListener : queryTextField.getActionListeners()) {
                actionListener.actionPerformed(new ActionEvent(queryTextField, ActionEvent.ACTION_PERFORMED, null));
            }
            frame.dispose();
        });

        buttonContainer.add(buildQueryButton);
        return buttonContainer;
    }

    public String getFullQuery() {
        StringBuilder fullQuery = new StringBuilder();
        for (int i = 0; i < panels.size(); i++) {
            Object panel = panels.get(i);
            if (panel instanceof RuleSetPanel) {
                fullQuery.append(((RuleSetPanel) panel).getQuery());
            }
            else if (panel instanceof RulePanel) {
                fullQuery.append(((RulePanel) panel).getQuery());
            }
            if (i < panels.size() - 1) {
                fullQuery.append(" ").append(queryOperators.getRuleOperator()).append(" ");
            }
        }
        return fullQuery.toString();
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

        queryTextField.addActionListener(e -> {
            RuleSet parsedQuery = miqlParser.parseMIQL(queryTextField.getText());

            if (parsedQuery != null && parsedQuery.rules != null && !parsedQuery.rules.isEmpty()) {
                modifyComboboxFromQuery(parsedQuery, 0);
                String builtQuery = getFullQuery();
                queryTextField.setText(builtQuery);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to parse query. Please check the syntax.");
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

        RuleSetPanel currentRuleSetPanel = (indentLevel == 0) ? null : new RuleSetPanel(this);
        if (currentRuleSetPanel != null) {
            currentRuleSetPanel.getPanels().clear();
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

}
