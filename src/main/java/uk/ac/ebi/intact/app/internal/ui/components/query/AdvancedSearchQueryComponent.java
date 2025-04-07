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

    @Getter
    private final JTextField queryTextField = new JTextField("Query");

//    private final String TEST_STRING = "NOT idA:IDA AND (interaction_id:ID AND pubid:PUBMEDID AND (source:DATABASE))";
    private final String TEST_STRING = "NOT idA:IDA AND (interaction_id:(ID) AND pubid:PUBMEDID AND (source:DATABASE))"; //todo: check for the "in" which seems to create another ruleset?

    public final JPanel rulesPanel = new JPanel();

    private final ArrayList<RulePanel> rules = new ArrayList<>();
    private final ArrayList<RuleSetPanel> ruleSetPanels = new ArrayList<>();

    private final QueryOperators queryOperators = new QueryOperators(this, rules, ruleSetPanels);
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
            RuleSet parsedQuery = miqlParser.parseMIQL(TEST_STRING);//todo: remove once done with the modifyComboboxFromQuery
            modifyComboboxFromQuery(parsedQuery, 0);
            System.out.println(getFullQuery()); //todo: return the query in the main cytoscapeApp
            //todo: add a close on built query

        });

        buttonContainer.add(buildQueryButton);
        return buttonContainer;
    }

    public String getFullQuery(){
        //todo: an update seems to be needed here
        StringBuilder fullQuery = new StringBuilder();
        String queryFromRuleBuilders = getQueriesFromRuleBuilders(rules, queryOperators.getRuleOperator());
        if (queryFromRuleBuilders != null) {
            fullQuery = new StringBuilder(queryFromRuleBuilders);
            fullQuery.append(" ")
                     .append(queryOperators.getRuleOperator())
                     .append(" ");
        }

        for (RuleSetPanel ruleSetPanel : ruleSetPanels) {
            fullQuery.append(ruleSetPanel.getQuery());
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
            modifyComboboxFromQuery(parsedQuery, 0);
        });

        queryInputFieldContainer.add(queryTextField);
        return queryInputFieldContainer;
    }

    private RuleSetPanel modifyComboboxFromQuery(RuleSet ruleSet, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        rulesPanel.removeAll();

        rules.clear();
        ruleSetPanels.clear();

        RuleSetPanel currentRuleSetPanel = new RuleSetPanel(this);

        for (Object ruleComponent : ruleSet.rules) {
            if (ruleComponent instanceof RuleSet) {
                RuleSet nestedRuleSet = (RuleSet) ruleComponent;

                RuleSetPanel nestedPanel = modifyComboboxFromQuery(nestedRuleSet, indentLevel + 1);
                currentRuleSetPanel.addRuleSetPanel(nestedPanel);

                ruleSetPanels.add(nestedPanel);

            } else if (ruleComponent instanceof Rule) {
                Rule rule = (Rule) ruleComponent;

                System.out.println(indent + "  Field: " + rule.getField() +
                        ", Operator: " + rule.getOperator() +
                        ", Entity: " + rule.getEntity() +
                        ", name: " + rule.getName() +
                        ", Value: " + (rule.getValue() != null ? rule.getValue() : "null"));

                RulePanel rulePanel = new RulePanel(this);

                rulePanel.entityComboBox.setSelectedItem(rule.getEntity());
                rulePanel.entityPropertiesCombobox.setSelectedItem(rule.getName());
                rulePanel.operatorsComboBox.setSelectedItem(rule.getOperator());
                rulePanel.userInputProperty.setText(rule.getValue());

                rules.add(rulePanel);
                currentRuleSetPanel.addRulePanel(rulePanel);
            }
        }

        if (!currentRuleSetPanel.getRules().isEmpty()) {
            rulesPanel.add(currentRuleSetPanel.getRuleSetPanel());
            ruleSetPanels.add(currentRuleSetPanel);
        }

        rulesPanel.revalidate();
        rulesPanel.repaint();
        queryTextField.setText(getFullQuery());
        return currentRuleSetPanel;
    }
}
