package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Range;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Rule;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleSet;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.StackFrame;

import java.util.*;

public class MIQLParser {

    public RuleSet parseMIQL(String miql) {
        miql = "(" + miql + ")"; //todo: check how to avoid to add a new ruleset everytime
        RuleSet out = null;
        Deque<StackFrame> stack = new ArrayDeque<>();
        int end, stackLevel;
        String value;
        Map<Integer, List<Range>> levelMap = new HashMap<>();

        char[] array = miql.toCharArray();
        for (int index = 0; index < array.length; index++) {
            char c = array[index];
            switch (c) {
                case '(':
                    stack.push(new StackFrame(index, new RuleSet()));
                    break;
                case ')':
                    StackFrame frame = stack.pop();
                    RuleSet ruleSet = frame.getRuleSet();
                    out = ruleSet;
                    end = index;
                    int start = frame.getStart();
                    value = miql.substring(start + 1, end);
                    if (frame.getStart() > 0 && array[start - 1] == ':') {
                        Rule rule = extractSetRule(miql, start, value);
                        if (!stack.isEmpty()) {
                            stack.peek().getRuleSet().rules.add(rule);
                        }
                    } else {
                        stackLevel = stack.size();
                        Range range = new Range(start, end - 1);
                        setupLevelMap(levelMap, stackLevel, range);
                        String trimmedValue = removeSuperiorRules(value, start, end, stackLevel, levelMap);
                        fillRuleSet(ruleSet, trimmedValue);
                        if (!stack.isEmpty()) {
                            stack.peek().getRuleSet().rules.add(ruleSet);
                        }
                    }
                    break;
            }
        }
        return out;
    }


    private Rule extractSetRule(String input, int start, String value) {
        int previousSpaceIndex = input.lastIndexOf(" ", start - 2);
        if (previousSpaceIndex < 0) previousSpaceIndex = 0;

        String potentialNot = input.substring(Math.max(previousSpaceIndex - 3, 0), previousSpaceIndex).trim().toLowerCase();
        String operator = potentialNot.equals("not") ? "not in" : "in";

        String miql = input.substring(previousSpaceIndex + 1, start - 1);
        String name = input.substring(previousSpaceIndex + 1, start - 2);

        String cleanValue = value == null || value.trim().equals("undefined") ? null : value;

        Field field = Field.getFieldsFromMiQL(miql);

        if (field != null) {
            String entity = field.getEntity();
            return new Rule(miql, operator, entity, cleanValue, null, name);
        }

        System.out.println("No field found for: " + miql);
        return null;
    }


    private void setupLevelMap(Map<Integer, List<Range>> levelMap, int stackLevel, Range range) {
        levelMap.computeIfAbsent(stackLevel, k -> new ArrayList<>()).add(range);
    }

    private String removeSuperiorRules(String value, int start, int end, int stackLevel, Map<Integer, List<Range>> levelMap) {
        int deleted = start;
        List<Range> superiorRanges = levelMap.get(stackLevel + 1);
        if (superiorRanges != null) {
            for (Range superiorRange : superiorRanges) {
                int startSuperiorRange = superiorRange.getStart();
                int endSuperiorRange = superiorRange.getEnd();
                if (startSuperiorRange > start && endSuperiorRange < end) {
                    value = value.substring(0, startSuperiorRange - deleted) + value.substring(endSuperiorRange - deleted);
                    deleted += endSuperiorRange - startSuperiorRange;
                }
            }
        }
        return value;
    }

    private void fillRuleSet(RuleSet ruleSet, String value) {
        ruleSet.condition = value.matches(".*\\sOR\\s.*") ? "or" : "and";
        String[] ruleStrings = value.split("\\sAND\\s|\\sOR\\s");
        for (String ruleStr : ruleStrings) {
            ruleStr = ruleStr.trim();
            if (!ruleStr.isEmpty()) {
                boolean isNot = ruleStr.startsWith("NOT ") || ruleStr.startsWith("not ");
                String ruleOperator = isNot ? "≠" : "=";
                int indexOfColon = ruleStr.indexOf(':');
                if (indexOfColon == -1) continue;
                String miql = ruleStr.substring(isNot ? 4 : 0, indexOfColon);

                String userInput = ruleStr.substring(indexOfColon + 1);

                userInput = userInput.trim().replaceAll("[\\[\\]]", "");

                String userInput1;
                String userInput2;

                if (userInput.contains(" TO ")) {
                    String[] parts = userInput.split(" TO ");
                    userInput1 = parts[0].trim();
                    userInput2 = parts[1].trim();
                } else {
                    userInput1 = userInput.trim();
                    userInput2 = "";
                }

                String operator = userInput.startsWith("[") ? (isNot ? "∉" : "∈") : ruleOperator;
                String ruleEntity = Field.getFieldsFromMiQL(miql).getEntity();
                String ruleName = Field.getFieldsFromMiQL(miql).getName();
                ruleSet.rules.add(new Rule(miql, operator, ruleEntity, userInput1.equals("undefined") ? null : userInput1, userInput2.equals("undefined") ? null : userInput2, ruleName));
            }
        }
    }

}

