package com.jthinking.jdbaudit.scan.api.entity;

import com.jthinking.jdbaudit.core.entity.RiskType;
import com.jthinking.jdbaudit.core.entity.RuleSource;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.scan.api.ScanTaskHandler;

import java.util.List;

/**
 * 一个ScanTask对应一个数据库实例的一种风险类型扫描
 */
public class ScanTask {

    private final String taskId;

    private final List<RuleSource> ruleSources;

    private final RiskType riskType;

    private final DBSettings dbSettings;

    private final ScanTaskHandler handler;

    /**
     * timeout in milliseconds
     */
    private Long timeout;

    /**
     * task start time in milliseconds
     */
    private Long startTime;

    private final TaskControl taskControl = new TaskControl();

    public static ScanTask of(DBSettings dbSettings, RiskType riskType, List<RuleSource> ruleSources, Long timeout, ScanTaskHandler handler) {
        return new ScanTask(dbSettings, riskType, ruleSources, timeout, handler);
    }

    public static ScanTask of(DBSettings dbSettings, RiskType riskType, ScanTaskHandler handler) {
        return of(dbSettings, riskType, null, null, handler);
    }

    private ScanTask(DBSettings dbSettings, RiskType riskType, List<RuleSource> ruleSources, Long timeout, ScanTaskHandler handler) {
        this("ST" + System.currentTimeMillis(), dbSettings, riskType, ruleSources, timeout, handler);
    }

    private ScanTask(String taskId, DBSettings dbSettings, RiskType riskType, List<RuleSource> ruleSources, Long timeout, ScanTaskHandler handler) {
        this.taskId = taskId;
        this.dbSettings = dbSettings;
        this.riskType = riskType;
        this.ruleSources = ruleSources;
        this.timeout = timeout;
        this.handler = handler;
    }

    public String getTaskId() {
        return taskId;
    }

    public RiskType getRiskType() {
        return riskType;
    }

    public List<RuleSource> getRuleSources() {
        return ruleSources;
    }

    public DBSettings getDbSettings() {
        return dbSettings;
    }

    public ScanTaskHandler getHandler() {
        return handler;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public TaskControl getTaskControl() {
        return taskControl;
    }

    public String getVersionString() {
        DBVersion version = dbSettings.getVersion();
        return version.getVersion();
    }
}
