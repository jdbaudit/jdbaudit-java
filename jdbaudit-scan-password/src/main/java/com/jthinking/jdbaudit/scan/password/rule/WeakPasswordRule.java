package com.jthinking.jdbaudit.scan.password.rule;

import com.jthinking.jdbaudit.core.entity.CCSSVector;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.core.util.scap.CCSSCalculator;
import com.jthinking.jdbaudit.scan.api.entity.Rule;

public class WeakPasswordRule extends Rule {

    private WeakPasswordRuleMatcher matcher;

    private CCSSVector ccss;

    @Override
    public String getVectorString() {
        if (ccss != null) {
            return CCSSCalculator.vectorString(ccss);
        }
        return super.getVectorString();
    }

    @Override
    public Double getBaseScore() {
        if (ccss != null) {
            return CCSSCalculator.baseScore(ccss);
        }
        return super.getBaseScore();
    }

    @Override
    public Severity getSeverity() {
        if (ccss != null) {
            return CCSSCalculator.getSeverity(ccss);
        }
        return super.getSeverity();
    }

    public WeakPasswordRuleMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(WeakPasswordRuleMatcher matcher) {
        this.matcher = matcher;
    }

    public CCSSVector getCcss() {
        return ccss;
    }

    public void setCcss(CCSSVector ccss) {
        this.ccss = ccss;
    }
}
