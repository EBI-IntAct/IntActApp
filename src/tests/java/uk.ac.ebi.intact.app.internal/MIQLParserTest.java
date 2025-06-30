package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.MIQLParser;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.Rule;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components.RuleSet;

import java.util.Objects;

public class MIQLParserTest {

    MIQLParser parser = new MIQLParser();

    @Test
    public void testParseSimpleQuery() {
        String TEST_STRING = "idA:1234 AND idB:5678";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);
        for (RuleComponent rule : parsedResult.getRules()) {
            Assert.assertTrue(rule instanceof Rule);
            Rule parsedRule = (Rule) rule;

            String entity = parsedRule.getEntity();
            String operator = parsedRule.getOperator();
            String miql = parsedRule.getMiql();
            String userInput1 = parsedRule.getUserInput1();

            Assert.assertTrue(Objects.equals(userInput1, "1234")
                    || Objects.equals(userInput1, "5678"));

            Assert.assertTrue(Objects.equals(miql, Field.P_ID_A.getMiqlQuery())
                    || Objects.equals(miql, Field.P_ID_B.getMiqlQuery()));

            Assert.assertTrue(Objects.equals(entity, Field.P_ID_A.getEntity())
                    || Objects.equals(entity, Field.P_ID_B.getEntity()));

            Assert.assertEquals(operator, "=");

        }
        Assert.assertEquals(parsedResult.rules.size(), 2);
    }

    @Test
    public void testParseQueryWithMultipleUserInputs() {
        String TEST_STRING = "rdate:[2000 TO 2025]";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);
        for (RuleComponent rule : parsedResult.getRules()) {
            Assert.assertTrue(rule instanceof Rule);
            Rule parsedRule = (Rule) rule;

            String entity = parsedRule.getEntity();
            String operator = parsedRule.getOperator();
            String miql = parsedRule.getMiql();
            String userInput1 = parsedRule.getUserInput1();
            String userInput2 = parsedRule.getUserInput2();

            Assert.assertTrue(Objects.equals(userInput1, "2000") && Objects.equals(userInput2, "2025"));

            Assert.assertEquals(Field.R_DATE.getMiqlQuery(), miql);

            Assert.assertEquals(Field.R_DATE.getEntity(), entity);

            Assert.assertEquals(operator, "∈");

        }
        Assert.assertEquals(parsedResult.rules.size(), 1);
    }

    @Test
    public void testParseQueryWithRuleSet() {
        final String TEST_STRING = "rdate:[2000 TO 2025] AND (taxidHost:12345)";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);
        Assert.assertEquals(parsedResult.rules.size(), 2);
    }

    @Test
    public void testParseQueryWithNegation() {
        final String TEST_STRING = "NOT idA:1234 AND idB:5678";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);

        Rule negatedRule = parsedResult.getRule(0);
        Assert.assertEquals(negatedRule.getOperator(), "≠");
        Assert.assertEquals(parsedResult.rules.size(), 2);
    }

    @Test
    public void testParseQueryWithIn() {
        final String TEST_STRING = "idB:(5678)";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);

        Rule negatedRule = parsedResult.getRule(0);
        Assert.assertEquals(negatedRule.getOperator(), "in");
        Assert.assertEquals(parsedResult.rules.size(), 1);
    }

    @Test
    public void testParseQueryWithNotIn() {
        final String TEST_STRING = "NOT idB:(5678)";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);

        Rule negatedRule = parsedResult.getRule(0);
        Assert.assertEquals(negatedRule.getOperator(), "not in");
        Assert.assertEquals(parsedResult.rules.size(), 1);
    }

    @Test
    public void testParseQueryWithNegationAndRuleset() {
        String TEST_STRING = "NOT idA:IDA AND (interaction_id:(ID) OR pubid:PUBMEDID OR source:DATABASE)";
        RuleSet parsedResult = parser.parseMIQL(TEST_STRING);
        Assert.assertEquals(parsedResult.rules.size(), 2);
    }

    @Test
    public void testParseQueryWithNestedRuleset() {
        String TEST_STRING = "idA:IDA AND (interaction_id:ID OR pubid:PUBMEDID OR (source:DATABASE))";
        RuleSet fullQuery = parser.parseMIQL(TEST_STRING);
        Assert.assertEquals(fullQuery.rules.size(), 2);

        RuleComponent parentRuleSet = fullQuery.getRules().get(1);
        Assert.assertTrue(parentRuleSet instanceof RuleSet);

        Assert.assertEquals(((RuleSet) parentRuleSet).rules.size(), 3);

        RuleComponent childRuleSet = ((RuleSet) parentRuleSet).rules.get(2);
        Assert.assertTrue(childRuleSet instanceof  RuleSet);
    }

    @Test
    public void testParseQueryWithRuleSetAndIn(){
        String TEST_STRING = "idA:IDA AND (interaction_id:(ID) OR pubid:PUBMEDID OR (source:DATABASE))";
        RuleSet fullQuery = parser.parseMIQL(TEST_STRING);
        Assert.assertEquals(fullQuery.rules.size(), 2);

        RuleComponent parentRuleSet = fullQuery.getRules().get(1);
        Assert.assertTrue(parentRuleSet instanceof RuleSet);

        Assert.assertEquals(((RuleSet) parentRuleSet).rules.size(), 3);

        Assert.assertEquals(((RuleSet) parentRuleSet).rules.get(2).getRule(0).getOperator(), "in");
    }

}
