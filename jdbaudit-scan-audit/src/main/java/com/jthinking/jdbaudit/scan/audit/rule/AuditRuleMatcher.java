package com.jthinking.jdbaudit.scan.audit.rule;

import java.util.List;

public class AuditRuleMatcher {

    private String expression;

    private List<AuditCondition> conditions;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<AuditCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<AuditCondition> conditions) {
        this.conditions = conditions;
    }
}
