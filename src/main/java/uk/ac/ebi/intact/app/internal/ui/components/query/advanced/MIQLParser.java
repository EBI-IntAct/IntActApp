package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.*;

import java.util.*;

public class MIQLParser {

    public RuleSet parseMIQL(String miql) {
        miql = "(" + miql + ")";
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

                    if (start > 0 && array[start - 1] == ':') {
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
            }
        }
        return out;
    }

    private void setupLevelMap(Map<Integer, List<Range>> levelMap, int stackLevel, Range range) {
        levelMap.computeIfAbsent(stackLevel, k -> new ArrayList<>()).add(range);
    }

    private String removeSuperiorRules(String value, int start, int end, int stackLevel, Map<Integer, List<Range>> levelMap) {
        int deleted = start;
        List<Range> superiorRanges = levelMap.get(stackLevel + 1);

        if (superiorRanges != null) {
            for (Range superiorRange : superiorRanges) {
                if (superiorRange.getStart() > start && superiorRange.getEnd() < end) {
                    value = value.substring(0, superiorRange.getStart() - deleted) +
                            value.substring(superiorRange.getEnd() - deleted);
                    deleted += superiorRange.getEnd() - superiorRange.getStart();
                }
            }
        }
        return value;
    }

    public void fillRuleSet(RuleSet ruleSet, String value) {
        ruleSet.setCondition(value.contains("OR") ? "or" : "and");

        List<RuleComponent> superiorRuleSets = ruleSet.getRules();
        int i = 0;
        ruleSet.setRules(new ArrayList<>());

        String[] ruleStrings = value.split("\\sAND\\s|\\sOR\\s");
        for (String ruleStr : ruleStrings) {
            ruleStr = ruleStr.trim();

            if (!ruleStr.isEmpty()) {
                if ("()".equals(ruleStr)) {
                    ruleSet.getRules().add(superiorRuleSets.get(i++));
                } else {
                    ruleStr = ruleStr.trim();
                    boolean different = ruleStr.toUpperCase().startsWith("NOT ");
                    String ruleOperator = different ? "≠" : "=";

                    int indexOfColon = ruleStr.indexOf(":");
                    if (indexOfColon == -1) indexOfColon = ruleStr.length();

                    String ruleFieldKeyword = ruleStr.substring(different ? 4 : 0, indexOfColon);
                    Field ruleField = Field.getFieldsFromMiQL(ruleFieldKeyword);

                    if (ruleField != null) {
                        String ruleValue = ruleStr.substring(indexOfColon + 1).trim();
                        String operator = ruleValue.startsWith("[") ? (different ? "∉" : "∈") : ruleOperator;

                        String userInput1 = ruleValue;
                        String userInput2 = null;

                        if (ruleValue.startsWith("[") && ruleValue.endsWith("]") && ruleValue.contains("TO")) {
                            ruleValue = ruleValue.replace("[", "").replace("]", "");
                            String[] userInputs = ruleValue.split("TO");
                            userInput1 = userInputs[0].trim();
                            userInput2 = userInputs[1].trim();
                        }


                        if (ruleValue.startsWith("(")) {
                            ruleSet.getRules().add(superiorRuleSets.remove(superiorRuleSets.size() - 1));
                        } else if ("undefined".equals(ruleValue)) {
                            ruleSet.getRules().add(new Rule(ruleFieldKeyword, operator, ruleField.getEntity(), userInput1, userInput2, ruleField.getName()));
                        } else {
                            ruleSet.getRules().add(new Rule(ruleFieldKeyword, operator, ruleField.getEntity(), userInput1, userInput2, ruleField.getName()));
                        }
                    }
                }
            }
        }
    }

    private Rule extractSetRule(String input, int start, String value) {
        int previousSpaceIndex = input.lastIndexOf(" ", start - 2);
        if (input.length() > previousSpaceIndex + 1 && input.charAt(previousSpaceIndex + 1) == '(') {
            previousSpaceIndex++;
        }

        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1); // Remove parentheses for processing
        }

        String potentialNot = input.substring(Math.max(previousSpaceIndex - 3, 0), previousSpaceIndex);
        String operator = potentialNot.equals("NOT") || potentialNot.equals("not") ? "not in" : "in";

        String field = input.substring(previousSpaceIndex + 1, start - 1);
        Field parsedField = Field.getFieldsFromMiQL(field);
        String entity = parsedField != null ? parsedField.getEntity() : null;
        String fieldName = parsedField != null ? parsedField.getName() : field;
        //todo: check for userinput2

        System.out.println("SetRule entity: " + entity + " field: " + field + " value: " + value + " operator: " + operator);

        if (entity != null) {
            return new Rule(field, operator, entity, value, null, fieldName);
        }
        return null;
    }

}

