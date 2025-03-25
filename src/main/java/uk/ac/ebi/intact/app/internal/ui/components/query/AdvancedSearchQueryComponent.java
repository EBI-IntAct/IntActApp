package uk.ac.ebi.intact.app.internal.ui.components.query;

import lombok.Getter;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.*;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;

public class AdvancedSearchQueryComponent {
    static JFrame frame = new JFrame("Advanced Search Query Builder");
    static int frameWidth = 2000;

    @Getter
    private final JTextField queryTextField = new JTextField("Query: ");

    public final JPanel rulesPanel = new JPanel();

    private final ArrayList<OneRuleBuilderPanel> rules = new ArrayList<>();
    private final ArrayList<RuleSetBuilder> ruleSetBuilders = new ArrayList<>();

    private final QueryOperators queryOperators = new QueryOperators(this, rules, ruleSetBuilders);

    public static void main(String[] args) {
        AdvancedSearchQueryComponent component = new AdvancedSearchQueryComponent();
        component.getFrame();
    }

    public JFrame getFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameWidth, 400);
        frame.setLayout(new GridLayout(4,1));

        frame.add(getQueryInputField());
        frame.add(queryOperators.getButtons(rulesPanel));
        frame.add(getRuleScrollPane());
        frame.add(getBuildQueryButtonContainer());

        frame.setVisible(true);
        return frame;
    }

    private JPanel getBuildQueryButtonContainer(){
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton buildQueryButton = new JButton("Build query");

        setButtonIntactPurple(buildQueryButton);
        buildQueryButton.addActionListener(e ->{
            System.out.println(getQueriesFromRuleBuilders(rules, queryOperators.getRuleOperator()));
        });

        buttonContainer.add(buildQueryButton);
        return buttonContainer;
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

    public void addRule(){
        JPanel rulePanel = new JPanel();
        OneRuleBuilderPanel oneRuleBuilderPanel = new OneRuleBuilderPanel();
        rules.add(oneRuleBuilderPanel);

        rulePanel.add(oneRuleBuilderPanel.getOneRuleBuilderPanel());
        rulePanel.add(getDeleteRuleButton(rulesPanel));

        queryTextField.setText(getQueriesFromRuleBuilders(rules, queryOperators.getRuleOperator()));
        rulesPanel.add(rulePanel);
        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    private JButton getDeleteRuleButton(JPanel rulePanel) {
        JButton deleteRuleButton = new JButton("Delete rule");
        setButtonIntactPurple(deleteRuleButton);
        deleteRuleButton.addActionListener(e -> {
            deleteRule(rulePanel);
        });
        return deleteRuleButton;
    }

    public void deleteRule(JPanel oneRuleBuilder) {
        rulesPanel.remove(oneRuleBuilder);

        queryTextField.setText(getQueriesFromRuleBuilders(rules, queryOperators.getRuleOperator()));
        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    private JPanel getQueryInputField(){
        JPanel queryInputFieldContainer = new JPanel();

        queryTextField.setMinimumSize(new Dimension(frameWidth, 25));
        queryTextField.setPreferredSize(new Dimension(frameWidth/2, 25));
        queryTextField.setVisible(true);

        queryInputFieldContainer.add(queryTextField);

        return queryInputFieldContainer;
    }

    public String getQueryFromTextField(){
        return queryTextField.getText();
    }

    private void modifyComboBoxesFromTextField(String queryFromTextField){
        //todo: remove every combo boxes and build from scratch

        String[] queries = getQueriesFromTextField(queryFromTextField);
        modifyQueryBuildersNumberFromTextField(queries);

        for (int i = 0; i < queries.length; i++) {
            for (Field field : Field.FIELD_MAP.values()) {
                if (queryFromTextField.contains(field.getMiqlQuery())) {
                    QueryComponents queryComponents = parseQuery(queries[i].trim());
                }
            }
        }
    }

    private void modifyQueryBuildersNumberFromTextField(String[] queries){
        while (queries.length > rules.size()) {
            addRule();
        }
        while (queries.length < rules.size()) {
            Component lastRule = rulesPanel.getComponents()[rules.size() - 1];
            deleteRule((JPanel) lastRule);
        }

        rulesPanel.revalidate();
        rulesPanel.repaint();
        queryTextField.setText(getQueriesFromRuleBuilders(rules, queryOperators.getRuleOperator()));
    }

    private String[] getQueriesFromTextField(String queryFromTextField){
        String[] queries;

        if (queryFromTextField.contains("AND")) {
            queries = queryFromTextField.split("AND");
        } else if (queryFromTextField.contains("OR")) {
            queries = queryFromTextField.split("OR");
        } else {
            queries = new String[]{queryFromTextField};
        }

        return queries;
    }

    private String getOperatorFromTextField(String queryFromTextField){
        if (queryFromTextField.contains("NOT")){
            if (queryFromTextField.contains(":[")){
                return "∉";
            } else if (queryFromTextField.contains(":(")){
                return "not in";
            } else if (queryFromTextField.contains(":")){
                return "≠";
            }
        } else {
            if (queryFromTextField.contains(":[")){
                return "∈";
            } else if (queryFromTextField.contains(":(")){
                return "in";
            } else if (queryFromTextField.contains(":")){
                return "=";
            } else if (queryFromTextField.contains("TRUE")){
                return "TRUE";
            } else if (queryFromTextField.contains("FALSE")){
                return "FALSE";
            }
        }
        return "";
    }

    public QueryComponents parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        QueryComponents components = new QueryComponents();

        if (query.startsWith("NOT ")) {
            query = query.substring(4);
            components.setNegated(true);
        }

        String[] parts;

        if (query.contains(":[")) {

            components.setOperator(components.isNegated() ? "∉" : "∈");
            parts = query.split(":\\[");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                String[] rangeValues = parts[1].replace("]", "").split(" TO ");
                if (rangeValues.length == 2) {
                    components.setUserInput(rangeValues[0]);
                    components.setUserInput2(rangeValues[1]);
                }
            }
        } else if (query.contains(":(")) {
            components.setOperator(components.isNegated() ? "not in" : "in");
            parts = query.split(":\\(");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                components.setUserInput(parts[1].replace(")", ""));
            }
        } else if (query.contains(":")) {
            parts = query.split(":");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                components.setUserInput(parts[1]);

                if (components.isNegated()) {
                    components.setOperator("≠");
                } else {
                    components.setOperator("=");
                }
            }
        }
        return components;
    }

}
