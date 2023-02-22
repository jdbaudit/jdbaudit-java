package com.jthinking.jdbaudit.scan.audit.rule;


import java.util.List;

public class AuditCondition {

    private String id;

    private String query;

    private List<AuditConditionMatch> matchRows;

    private Integer matchRowSize;

    private String matchError;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<AuditConditionMatch> getMatchRows() {
        return matchRows;
    }

    public void setMatchRows(List<AuditConditionMatch> matchRows) {
        this.matchRows = matchRows;
    }

    public Integer getMatchRowSize() {
        return matchRowSize;
    }

    public void setMatchRowSize(Integer matchRowSize) {
        this.matchRowSize = matchRowSize;
    }

    public String getMatchError() {
        return matchError;
    }

    public void setMatchError(String matchError) {
        this.matchError = matchError;
    }
}
