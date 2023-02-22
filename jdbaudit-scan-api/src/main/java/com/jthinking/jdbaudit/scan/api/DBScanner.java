package com.jthinking.jdbaudit.scan.api;

import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import com.jthinking.jdbaudit.scan.api.entity.TaskConfig;

public interface DBScanner {

    /**
     * 扫描器ID
     * ID规范。全拼大写下划线分割。
     * 该ID与规则文件中的规则块字段名关联。规则解析器会使用该ID去查找规则文件中对应的规则块字段。
     * 例如：getId返回AUDIT。则规则解析器对应解析如下规则中的AUDIT段。
     * <pre>
     * {
     *     "version": "1.0.0",
     *     "rules": {
     *          "AUDIT": [...]
     *     }
     * }
     * </pre>
     * @return 唯一标识的扫描器ID
     */
    String getId();

    void configure(TaskConfig taskConfig);

    void start();

    void stop();

    void submitTask(ScanTask scanTask);

    void loadRule(String jsonRule, boolean update);

}
