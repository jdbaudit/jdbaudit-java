package com.jthinking.jdbaudit.scan.api;


import com.jthinking.jdbaudit.scan.api.entity.ScanResult;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;

public interface ScanTaskHandler {

    // 1. 任务开始
    void onStart(ScanTask scanTask);

    // 2. 告警输出
    void onAlert(ScanTask scanTask, ScanResult result);

    // 3. 任务完成
    void onFinish(ScanTask scanTask);

    // 4. 任务异常
    void onError(ScanTask scanTask, Throwable error);

}
