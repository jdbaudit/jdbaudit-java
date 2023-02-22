package com.jthinking.jdbaudit.scan.audit.rule;



import com.jthinking.jdbaudit.core.entity.CCSSVector;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.core.util.scap.CCSSCalculator;
import com.jthinking.jdbaudit.scan.api.entity.Rule;

public class AuditRule extends Rule {

    private AuditRuleMatcher matcher;

    private CCSSVector ccss;

    @Override
    public Severity getSeverity() {
        return CCSSCalculator.getSeverity(ccss);
    }

    @Override
    public String getVectorString() {
        return CCSSCalculator.vectorString(ccss);
    }

    @Override
    public Double getBaseScore() {
        return CCSSCalculator.baseScore(ccss);
    }

    public AuditRuleMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(AuditRuleMatcher matcher) {
        this.matcher = matcher;
    }

    public CCSSVector getCcss() {
        return ccss;
    }

    public void setCcss(CCSSVector ccss) {
        this.ccss = ccss;
    }
}
