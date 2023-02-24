package com.jthinking.jdbaudit.scan.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.engine.interpreter.util.RuleUtils;
import com.jthinking.jdbaudit.scan.api.entity.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据库扫描器抽象类。
 * 用于特定风险类型扫描器扩展。
 * 该抽象类处理了规则加载、多线程任务执行、告警组装等公共逻辑。
 * 继承该抽象类需要实现match、prepareData、getId方法。
 * 并且需要实现特有的规则类型和匹配数据泛型参数。
 * @param <R> 特定风险类型单条规则类型。继承Rule类型。
 * @param <D> 特定风险类型待匹配的数据。实现Data接口。该Data数据的维度决定告警的维度。每个该类型数据只产生一次告警
 */
public abstract class AbstractDBScanner<R extends Rule, D extends DBData> implements DBScanner {

    /**
     * stop flag
     */
    private volatile boolean stopFlag;

    /**
     * 任务列表
     */
    private ConcurrentHashMap<String, ScanTask> scanTasks;

    private ConcurrentHashMap<String, R> rules;

    /**
     * 任务配置
     */
    private TaskConfig taskConfig;

    private ObjectMapper objectMapper;

    private ThreadPoolExecutor scanTaskExecutor;

    /**
     * 单条规则-全部数据 匹配。
     * 单条规则-全部数据 匹配模式。该模式可能不适合hyperscan等需要对规则进行统一编译的高性能匹配模式。
     * 全部规则-单条数据 匹配模式。该模式适合对规则进行统一编译的匹配模式，以应对不断增加的规则量。
     * @param rule 单条规则
     * @param data 全部待匹配数据
     * @param scanTask 任务控制器
     * @return 当前规则是否匹配
     */
    protected abstract boolean match(R rule, D data, ScanTask scanTask);

    /**
     * 在进行所有的规则匹配之前仅调用一次，准备所有规则匹配所需的数据。
     * @param dbSettings 数据库连接
     * @param rules 全部规则信息
     * @return 待匹配的数据列表，对列表中的每个数据项进行规则匹配
     */
    protected abstract List<D> prepareData(DBSettings dbSettings, List<R> rules);

    @Override
    public void configure(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public void start() {
        this.scanTasks = new ConcurrentHashMap<>();
        this.rules = new ConcurrentHashMap<>();
        this.taskConfig = new TaskConfig();
        this.objectMapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true).configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        LinkedBlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>(10);
        this.scanTaskExecutor = new ThreadPoolExecutor(0, this.taskConfig.getMaxRunningTask(), 5, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.AbortPolicy());
        this.startTaskTimeoutSchedule();
    }

    /**
     * 开启任务超时监听
     */
    private void startTaskTimeoutSchedule() {
        Thread thread = new Thread(() -> {
            while (!this.stopFlag) {
                for (Map.Entry<String, ScanTask> entry : this.scanTasks.entrySet()) {
                    ScanTask scanTask = entry.getValue();
                    if (scanTask.getTimeout() + scanTask.getStartTime() < System.currentTimeMillis()) {
                        scanTask.getTaskControl().stop();
                        this.scanTasks.remove(entry.getKey());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        });
        thread.setName("TaskTimeoutSchedule");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        this.stopFlag = true;
        for (Map.Entry<String, ScanTask> entry : this.scanTasks.entrySet()) {
            ScanTask scanTask = entry.getValue();
            scanTask.getTaskControl().stop();
        }
        this.scanTasks.clear();
        scanTaskExecutor.shutdown();
    }

    /**
     * 筛选指定任务所需的规则列表
     * @param rules 全部规则
     * @param scanTask 当前任务
     * @return 当前任务所需的规则列表
     */
    private List<R> filterRules(Collection<R> rules, ScanTask scanTask) {
        String dbId = scanTask.getDbSettings().getDBId();
        DBVersion dbVersion = scanTask.getDbSettings().getVersion();
        return rules.parallelStream()
                .filter(rule -> {
                    if (scanTask.getRuleSources() != null) {
                        return scanTask.getRuleSources().contains(rule.getSource());
                    } else {
                        return true;
                    }
                })
                .filter(rule -> RuleUtils.wildCardMatch(rule.getDbId(), dbId))
                .filter(rule -> RuleUtils.wildCardMatch(rule.getDbVersion(), dbVersion.getVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public void submitTask(ScanTask scanTask) {
        if (this.scanTaskExecutor == null) {
            throw new RuntimeException("scanner is not started, please call start() method before submitTask");
        }
        if (this.rules.isEmpty()) {
            throw new RuntimeException("rule set is empty, please call loadRule(String) method before submitTask");
        }
        final List<R> filterRules = filterRules(this.rules.values(), scanTask);
        // 连接数据库准备数据，每个任务查询一次。
        final List<D> preparedDataList = prepareData(scanTask.getDbSettings(), filterRules);
        if (scanTask.getTimeout() == null) {
            scanTask.setTimeout(taskConfig.getTaskTimeout());
        }
        scanTaskExecutor.submit(() -> {
            scanTask.setStartTime(System.currentTimeMillis());
            scanTasks.put(scanTask.getTaskId(), scanTask);
            ScanTaskHandler handler = scanTask.getHandler();
            handler.onStart(scanTask);
            for (D preparedData : preparedDataList) {
                // 逐条匹配规则
                for (R rule : filterRules) {
                    try {
                        boolean matched = match(rule, preparedData, scanTask);
                        if (matched) {
                            Alert alert = new Alert();
                            alert.setRule(rule);
                            alert.setData(preparedData.toDataString());
                            ScanResult scanResult = new ScanResult();
                            scanResult.setAlert(alert);
                            scanResult.setDatabaseId(scanTask.getDbSettings().getDBId());
                            scanResult.setRiskType(getId());
                            scanResult.setHost(scanTask.getDbSettings().getHost());
                            scanResult.setPort(scanTask.getDbSettings().getPort());
                            handler.onAlert(scanTask, scanResult);
                        }
                    } catch (Throwable t) {
                        handler.onError(scanTask, t);
                    }
                }
            }
            scanTasks.remove(scanTask.getTaskId());
            handler.onFinish(scanTask);
        });


    }

    @Override
    public void loadRule(String jsonRule, boolean update) {
        if (!update) {
            this.rules.clear();
        }
        try {
            JsonNode ruleSet = objectMapper.readTree(jsonRule);
            if (ruleSet.has(this.getId())) {
                String ruleValue = ruleSet.get(this.getId()).toString();
                JavaType type = objectMapper.getTypeFactory().constructParametricType(List.class, getRuleType());
                List<R> ruleList = objectMapper.readValue(ruleValue, type);
                ruleList.parallelStream().forEach(rule -> this.rules.put(rule.getId(), rule));
            }/* else {
                throw new RuntimeException(this.getId() + " not found in rule json");
            }*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Class<R> getRuleType();

}
