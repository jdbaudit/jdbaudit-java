package com.jthinking.jdbaudit.scan.privacy.rule;

import com.jthinking.jdbaudit.core.entity.CCSSVector;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.core.util.scap.CCSSCalculator;
import com.jthinking.jdbaudit.scan.api.entity.Rule;

public class PrivacyRule extends Rule {

    private PrivacyRuleMatcher matcher;

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

    public PrivacyRuleMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(PrivacyRuleMatcher matcher) {
        this.matcher = matcher;
    }

    public CCSSVector getCcss() {
        return ccss;
    }

    public void setCcss(CCSSVector ccss) {
        this.ccss = ccss;
    }
}
