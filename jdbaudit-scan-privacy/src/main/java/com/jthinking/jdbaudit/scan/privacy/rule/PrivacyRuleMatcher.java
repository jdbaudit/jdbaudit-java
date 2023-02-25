package com.jthinking.jdbaudit.scan.privacy.rule;

public class PrivacyRuleMatcher {

    /**
     * 模式，匹配值的方式。
     * 取值：text、pattern、regex、cpe
     */
    private String mode;

    /**
     * 规则值
     */
    private String value;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
