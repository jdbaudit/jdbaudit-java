package com.jthinking.jdbaudit.scan.audit.rule;

public class AuditConditionMatch {

    private Integer column;
    private String columnLabel;
    private String mode;
    private boolean matchCase;
    private boolean reverse;
    private String value;

    public AuditConditionMatch() {
    }

    public Integer getColumn() {
        return this.column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getColumnLabel() {
        return this.columnLabel;
    }

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isMatchCase() {
        return this.matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isReverse() {
        return this.reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
