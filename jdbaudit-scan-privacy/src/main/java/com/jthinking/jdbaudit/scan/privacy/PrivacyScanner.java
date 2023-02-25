package com.jthinking.jdbaudit.scan.privacy;

import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import com.jthinking.jdbaudit.engine.interpreter.util.PatternMatcher;
import com.jthinking.jdbaudit.scan.api.AbstractDBScanner;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import com.jthinking.jdbaudit.scan.privacy.data.SampleData;
import com.jthinking.jdbaudit.scan.privacy.rule.PrivacyRule;
import com.jthinking.jdbaudit.scan.privacy.rule.PrivacyRuleMatcher;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PrivacyScanner extends AbstractDBScanner<PrivacyRule, SampleData> {

    @Override
    protected boolean match(PrivacyRule rule, SampleData sampleData, ScanTask scanTask) {
        PrivacyRuleMatcher matcher = rule.getMatcher();
        String mode = matcher.getMode();
        String value = matcher.getValue();
        for (String[] data : sampleData.queryResult().getData()) {
            for (String columnData : data) {
                switch (mode) {
                    case "text":
                        if (textMatch(columnData, value)) {
                            return true;
                        }
                        break;
                    case "pattern":
                        if (patternMatch(columnData, value)) {
                            return true;
                        }
                        break;
                    case "regex":
                        if (regexMatch(columnData, value)) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return false;
    }

    @Override
    protected Iterable<SampleData> prepareData(DBSettings dbSettings, List<PrivacyRule> rules) {
        QueryListResult dataSample = dbSettings.getDataSample();
        return dataSample.getQueryResultMap().values().parallelStream()
                .map(SampleData::new)
                .collect(Collectors.toList());
    }

    @Override
    protected Class<PrivacyRule> getRuleType() {
        return PrivacyRule.class;
    }

    @Override
    public String getId() {
        return "PRIVACY";
    }


    private boolean textMatch(String columnData, String ruleValue) {
        if (columnData == null) {
            return false;
        }
        return columnData.equals(ruleValue);
    }

    private boolean patternMatch(String columnData, String ruleValue) {
        if (columnData == null) {
            return false;
        }
        return PatternMatcher.match(columnData, ruleValue);
    }

    private boolean regexMatch(String columnData, String ruleValue) {
        if (columnData == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(ruleValue);
        return pattern.matcher(columnData).find();
    }
}
