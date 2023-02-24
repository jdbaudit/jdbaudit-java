package com.jthinking.jdbaudit.scan.audit;

import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import com.jthinking.jdbaudit.db.api.entity.QueryResult;
import com.jthinking.jdbaudit.engine.interpreter.dsl.perfs.ExprEvaluatorHelper;
import com.jthinking.jdbaudit.engine.interpreter.util.PatternMatcher;
import com.jthinking.jdbaudit.scan.api.AbstractDBScanner;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import com.jthinking.jdbaudit.scan.api.entity.TaskControl;
import com.jthinking.jdbaudit.scan.audit.data.DBConfigData;
import com.jthinking.jdbaudit.scan.audit.rule.AuditCondition;
import com.jthinking.jdbaudit.scan.audit.rule.AuditConditionMatch;
import com.jthinking.jdbaudit.scan.audit.rule.AuditRule;
import com.jthinking.jdbaudit.scan.audit.rule.AuditRuleMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class AuditScanner extends AbstractDBScanner<AuditRule, DBConfigData> {

    @Override
    protected boolean match(AuditRule rule, DBConfigData dbConfigData, ScanTask scanTask) {
        TaskControl taskControl = scanTask.getTaskControl();
        if (taskControl.isStop()) {
            return false;
        }
        AuditRuleMatcher matcher = rule.getMatcher();
        String expression = matcher.getExpression();
        List<AuditCondition> conditions = matcher.getConditions();
        Map<String, Boolean> matched = new HashMap<>();
        for (AuditCondition condition : conditions) {
            String id = condition.getId();
            String matchError = condition.getMatchError();
            Integer matchRowSize = condition.getMatchRowSize();
            String query = condition.getQuery();

            QueryResult result = dbConfigData.get(query);
            String error = result.getError();
            Map<String, Integer> label = result.getLabel();
            List<String[]> rowDataList = result.getData();

            if (matchError != null) {
                matched.put(id, error != null && error.contains(matchError));
            } else if (matchRowSize != null) {
                matched.put(id, matchRowSize.equals(rowDataList.size()));
            } else {
                List<AuditConditionMatch> matches = condition.getMatchRows();
                int count = 0;
                for (AuditConditionMatch match : matches) {
                    Integer column = match.getColumn();
                    String columnLabel = match.getColumnLabel();
                    boolean matchCase = match.isMatchCase();
                    boolean reverse = match.isReverse();
                    String mode = match.getMode();
                    String value = match.getValue();

                    outer: for (String[] data : rowDataList) {
                        if (taskControl.isStop()) {
                            return false;
                        }
                        String columnData;
                        if (column != null) {
                            columnData = data[column - 1];
                        } else if (columnLabel != null) {
                            columnData = data[label.get(columnLabel)];
                        } else {
                            break;
                        }
                        switch (mode) {
                            case "text":
                                if (textMatch(columnData, value, matchCase, reverse)) {
                                    count++;
                                    break outer;
                                }
                                break;
                            case "pattern":
                                if (patternMatch(columnData, value, matchCase, reverse)) {
                                    count++;
                                    break outer;
                                }
                                break;
                            case "regex":
                                if (regexMatch(columnData, value, matchCase, reverse)) {
                                    count++;
                                    break outer;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                matched.put(id, count == matches.size());
            }
        }
        return ExprEvaluatorHelper.exec(expression, matched);
    }

    @Override
    protected List<DBConfigData> prepareData(DBSettings dbSettings, List<AuditRule> rules) {
        List<String> queries = rules.parallelStream()
                .filter(rule -> rule.getMatcher() != null)
                .flatMap(rule -> rule.getMatcher().getConditions().stream()
                        .map(AuditCondition::getQuery))
                .collect(Collectors.toList());
        QueryListResult queryListResult = dbSettings.query(queries);
        return Collections.singletonList(new DBConfigData(queryListResult.getQueryResultMap()));
    }

    @Override
    protected Class<AuditRule> getRuleType() {
        return AuditRule.class;
    }

    @Override
    public String getId() {
        return "AUDIT";
    }

    private boolean textMatch(String columnData, String ruleValue, boolean matchCase, boolean reverse) {
        if (columnData == null) {
            return false;
        }
        boolean equals;
        if (matchCase) {
            equals = columnData.equals(ruleValue);
        } else {
            equals = columnData.equalsIgnoreCase(ruleValue);
        }
        return reverse != equals;
    }

    private boolean patternMatch(String columnData, String ruleValue, boolean matchCase, boolean reverse) {
        if (columnData == null) {
            return false;
        }
        boolean equals = PatternMatcher.match(columnData, ruleValue);
        return reverse != equals;
    }

    private boolean regexMatch(String columnData, String ruleValue, boolean matchCase, boolean reverse) {
        if (columnData == null) {
            return false;
        }
        Pattern pattern;
        if (matchCase) {
            pattern = Pattern.compile(ruleValue);
        } else {
            pattern = Pattern.compile(ruleValue, Pattern.CASE_INSENSITIVE);
        }
        boolean equals = pattern.matcher(columnData).find();
        return reverse != equals;
    }
}
