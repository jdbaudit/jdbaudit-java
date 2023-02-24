package com.jthinking.jdbaudit.scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jthinking.jdbaudit.core.entity.RiskType;
import com.jthinking.jdbaudit.scan.api.DBScanner;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import com.jthinking.jdbaudit.scan.api.entity.TaskConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 数据库风险扫描器
 */
public class RiskScanner {

    /**
     * 默认每种风险类型正在运行的最大任务数
     */
    private static final int DEFAULT_MAX_RUNNING_TASK_NUM_PER_RISK_TYPE = 100;

    /**
     * 默认单任务超时时间。单位：毫秒
     */
    private static final int DEFAULT_TASK_TIMEOUT = 1000 * 60 * 10;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 当前应用规则版本
     */
    private String ruleVersion;

    /**
     *
     */
    private final List<DBScanner> dbScanners;

    /**
     * 风险扫描器
     */
    public RiskScanner() {
        this(DEFAULT_MAX_RUNNING_TASK_NUM_PER_RISK_TYPE, DEFAULT_TASK_TIMEOUT);
    }

    /**
     * 风险扫描器
     * @param maxRunningTaskPerRiskType 每种风险类型正在运行的最大任务数
     */
    public RiskScanner(int maxRunningTaskPerRiskType) {
        this(maxRunningTaskPerRiskType, DEFAULT_TASK_TIMEOUT);
    }

    /**
     * 风险扫描器
     * @param maxRunningTaskPerRiskType 每种风险类型正在运行的最大任务数
     * @param taskTimeout 默认单任务超时时间。单位：毫秒
     */
    public RiskScanner(int maxRunningTaskPerRiskType, long taskTimeout) {
        ServiceLoader<DBScanner> load = ServiceLoader.load(DBScanner.class);
        List<DBScanner> dbScanners = new ArrayList<>();
        for (DBScanner dbScanner : load) {
            // 配置
            dbScanner.configure(new TaskConfig(maxRunningTaskPerRiskType, taskTimeout));
            // 启动
            dbScanner.start();
            dbScanners.add(dbScanner);
        }
        this.dbScanners = dbScanners;
    }

    /**
     * 加载规则
     * @param jsonRule 规则JSON
     * @param update 是否在原规则基础上更新规则
     */
    public void loadRule(String jsonRule, boolean update) {
        for (DBScanner scanner : dbScanners) {
            scanner.loadRule(jsonRule, update);
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonRule);
            JsonNode version = jsonNode.get("version");
            if (version != null) {
                this.ruleVersion = version.asText();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交扫描任务
     * @param scanTask 扫描任务
     * @return 任务ID
     */
    public String submitTask(ScanTask scanTask) {
        for (DBScanner scanner : dbScanners) {
            RiskType riskType = scanTask.getRiskType();
            if (riskType.equals(RiskType.valueOf(scanner.getId()))) {
                scanner.submitTask(scanTask);
            }
        }
        return scanTask.getTaskId();
    }

    /**
     * 停止扫描器。同时停止所有任务
     */
    public void stop() {
        // 停止扫描
        for (DBScanner scanner : dbScanners) {
            scanner.stop();
        }
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

}