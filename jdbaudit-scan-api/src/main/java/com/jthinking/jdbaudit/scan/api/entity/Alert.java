package com.jthinking.jdbaudit.scan.api.entity;

public class Alert {

    private Rule rule;

    /**
     * 数据
     */
    private String data;

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
