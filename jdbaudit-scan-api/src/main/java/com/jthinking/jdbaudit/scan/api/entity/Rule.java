package com.jthinking.jdbaudit.scan.api.entity;


import com.jthinking.jdbaudit.core.entity.*;

import java.util.List;
import java.util.Map;

public class Rule {

    /**
     * 规则格式版本
     */
    private int rfv;

    private String id;

    private String group;

    private RuleSource source;

    private String dbId;

    private String dbVersion;

    private String dbEdition;

    private String dbOS;

    /**
     * 重要程度
     */
    private Severity severity;

    /**
     * CCSS/CVSS向量字符串
     */
    private String vectorString;

    /**
     * CCSS/CVSS基础得分
     */
    private Double baseScore;

    private Map<Lang, String> name;

    private Map<Lang, String> description;

    private Map<Lang, String> rationale;

    private Map<Lang, String> audit;

    private Map<Lang, String> remediation;

    private List<RuleReference> references;

    private RiskType riskType;

    public int getRfv() {
        return rfv;
    }

    public void setRfv(int rfv) {
        this.rfv = rfv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public RuleSource getSource() {
        return source;
    }

    public void setSource(RuleSource source) {
        this.source = source;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getDbEdition() {
        return dbEdition;
    }

    public void setDbEdition(String dbEdition) {
        this.dbEdition = dbEdition;
    }

    public String getDbOS() {
        return dbOS;
    }

    public void setDbOS(String dbOS) {
        this.dbOS = dbOS;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Map<Lang, String> getName() {
        return name;
    }

    public void setName(Map<Lang, String> name) {
        this.name = name;
    }

    public Map<Lang, String> getDescription() {
        return description;
    }

    public void setDescription(Map<Lang, String> description) {
        this.description = description;
    }

    public Map<Lang, String> getRationale() {
        return rationale;
    }

    public void setRationale(Map<Lang, String> rationale) {
        this.rationale = rationale;
    }

    public Map<Lang, String> getAudit() {
        return audit;
    }

    public void setAudit(Map<Lang, String> audit) {
        this.audit = audit;
    }

    public Map<Lang, String> getRemediation() {
        return remediation;
    }

    public void setRemediation(Map<Lang, String> remediation) {
        this.remediation = remediation;
    }

    public List<RuleReference> getReferences() {
        return references;
    }

    public void setReferences(List<RuleReference> references) {
        this.references = references;
    }

    public RiskType getRiskType() {
        return riskType;
    }

    public void setRiskType(RiskType riskType) {
        this.riskType = riskType;
    }

    public String getVectorString() {
        return vectorString;
    }

    public void setVectorString(String vectorString) {
        this.vectorString = vectorString;
    }

    public Double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }
}
