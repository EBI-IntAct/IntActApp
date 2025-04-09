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
import java.util.ArrayList;

public class AdvancedSearchQueryComponent {
    static int frameWidth = 2000;

    //    private final String TEST_STRING = "NOT idA:IDA AND (interaction_id:ID AND pubid:PUBMEDID AND (source:DATABASE))";
//    private final String TEST_STRING = "rdate:[12345 TO 6789] AND (taxidHost:12345)";
    private final String TEST_STRING = "NOT idA:IDA AND (interaction_id:(ID) AND pubid:PUBMEDID AND (source:DATABASE))"; //todo: check for the "in" which seems to create another ruleset?


    @Getter
    private final JTextField queryTextField = new JTextField(TEST_STRING);

    public final JPanel rulesPanel = new JPanel();

    private final ArrayList<Object> panels = new ArrayList<>();

    private final QueryOperators queryOperators = new QueryOperators(this, panels);
    private final MIQLParser miqlParser = new MIQLParser();

    public static void main(String[] args) {
        AdvancedSearchQueryComponent component = new AdvancedSearchQueryComponent();
        component.getFrame();
    }

    public void getFrame() {
        JFrame frame = new JFrame("Advanced Search Query Builder");
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
            System.out.println(getFullQuery());

            //todo: return query to main App
            //todo: add a close on built query
        });

        buttonContainer.add(buildQueryButton);
        return buttonContainer;
    }

    public String getFullQuery() {
        StringBuilder fullQuery = new StringBuilder();
        for (int i = 0; i < panels.size(); i++) {
            Object panel = panels.get(i);
            if (panel instanceof RulePanel) {
                fullQuery.append(((RulePanel) panel).getQuery());
            } else if (panel instanceof RuleSetPanel) {
                fullQuery.append(((RuleSetPanel) panel).getQuery());
            }
            if (i < panels.size() - 1) {
                fullQuery.append(" ").append(queryOperators.getRuleOperator()).append(" ");
            }
        }
        System.out.println(fullQuery);
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
                System.out.println("Built Query: " + builtQuery); //todo: check why an additional ruleset is added everytime
                queryTextField.setText(builtQuery);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to parse query. Please check the syntax.");
            }
        });

        queryInputFieldContainer.add(queryTextField);
        return queryInputFieldContainer;
    }

    private RuleSetPanel modifyComboboxFromQuery(RuleSet ruleSet, int indentLevel) {
        rulesPanel.removeAll();
        panels.clear();

        RuleSetPanel currentRuleSetPanel = new RuleSetPanel(this);
        currentRuleSetPanel.getPanels().clear();

        if (ruleSet.rules.size() == 1 && ruleSet.rules.get(0) instanceof Rule) {
            Rule rule = (Rule) ruleSet.rules.get(0);

            RulePanel rulePanel = new RulePanel(this);

            rulePanel.entityComboBox.setSelectedItem(rule.getEntity());
            rulePanel.entityPropertiesCombobox.setSelectedItem(rule.getName());
            rulePanel.operatorsComboBox.setSelectedItem(rule.getOperator());
            rulePanel.userInputProperty.setText(rule.getUserInput1());
            rulePanel.userInputProperty2.setText(rule.getUserInput2());

            currentRuleSetPanel.addRulePanel(rulePanel);
        } else {
            for (Object ruleComponent : ruleSet.rules) {
                if (ruleComponent instanceof RuleSet) {
                    RuleSet nestedRuleSet = (RuleSet) ruleComponent;

                    RuleSetPanel nestedPanel = modifyComboboxFromQuery(nestedRuleSet, indentLevel + 1);
                    currentRuleSetPanel.addRuleSetPanel(nestedPanel);

                } else if (ruleComponent instanceof Rule) {
                    Rule rule = (Rule) ruleComponent;
                    RulePanel rulePanel = new RulePanel(this);

                    rulePanel.entityComboBox.setSelectedItem(rule.getEntity());
                    rulePanel.entityPropertiesCombobox.setSelectedItem(rule.getName());
                    rulePanel.operatorsComboBox.setSelectedItem(rule.getOperator());
                    rulePanel.userInputProperty.setText(rule.getUserInput1());
                    rulePanel.userInputProperty2.setText(rule.getUserInput2());

                    currentRuleSetPanel.addRulePanel(rulePanel);
                }
            }
        }

        if (!currentRuleSetPanel.getPanels().isEmpty()) {
            if (indentLevel == 0) {
                rulesPanel.add(currentRuleSetPanel.getRuleSetPanel());
                panels.add(currentRuleSetPanel);
            }
        }

        if (indentLevel == 0) {
            rulesPanel.revalidate();
            rulesPanel.repaint();
        }

        return currentRuleSetPanel;
    }

}
