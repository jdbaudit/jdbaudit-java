package com.jthinking.jdbaudit.scanner;

import com.jthinking.jdbaudit.core.entity.RiskType;
import com.jthinking.jdbaudit.core.entity.RuleSource;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.mysql.MySQLSettings;
import com.jthinking.jdbaudit.scan.api.*;
import com.jthinking.jdbaudit.scan.api.entity.Alert;
import com.jthinking.jdbaudit.scan.api.entity.Rule;
import com.jthinking.jdbaudit.scan.api.entity.ScanResult;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AppTest {

    @Test
    public void testAPI() throws Exception {

        // 参数
        List<RiskType> riskTypes = new ArrayList<>();
        riskTypes.add(RiskType.AUDIT);
        riskTypes.add(RiskType.WEAK_PASSWORD);
        riskTypes.add(RiskType.VULNERABILITY);
        riskTypes.add(RiskType.PRIVILEGE);
        riskTypes.add(RiskType.PRIVACY);
        riskTypes.add(RiskType.BACKDOOR);

        // 参数
        List<RuleSource> ruleSources = new ArrayList<>();
        ruleSources.add(RuleSource.NIST);
        ruleSources.add(RuleSource.CIS);
        ruleSources.add(RuleSource.SplashData);

        // 参数
        Long timeout = 1000000L;

        List<DBSettings> dbSettingsList = new ArrayList<>();
        // 参数
        dbSettingsList.add(new MySQLSettings("localhost", 3306, "root", "root"));

        // 创建扫描器实例
        RiskScanner riskXScanner = new RiskScanner();

        try (FileInputStream inputStream = new FileInputStream("jdbaudit-rules/AUDIT.json")) {
            String jsonRule = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            riskXScanner.loadRule(jsonRule, false);
        }

        for (DBSettings dbSettings : dbSettingsList) {
            for (RiskType riskType : riskTypes) {
                // 提交任务，通过回调函数获取结果
                riskXScanner.submitTask(ScanTask.of(dbSettings, riskType, ruleSources, timeout, new ScanTaskHandler() {

                    @Override
                    public void onStart(ScanTask scanTask) {
                        System.out.println("onStart:" + scanTask.getTaskId());
                    }

                    @Override
                    public void onAlert(ScanTask scanTask, ScanResult result) {
                        Alert alert = result.getAlert();
                        Rule rule = alert.getRule();
                        String data = alert.getData();
                        String taskId = scanTask.getTaskId();
                        System.out.println("onAlert:" + scanTask.getTaskId());
                    }

                    @Override
                    public void onFinish(ScanTask scanTask) {
                        System.out.println("onFinish:" + scanTask.getTaskId());
                    }

                    @Override
                    public void onError(ScanTask scanTask, Throwable error) {
                        System.out.println("onError:" + scanTask.getTaskId());
                    }
                }));

            }
        }

        // 关闭扫描器
        riskXScanner.stop();

    }

    @Test
    public void testAPI2() throws Exception {

        RiskScanner riskScanner = new RiskScanner();

        try (FileInputStream inputStream = new FileInputStream("~\\projects\\jdbaudit-rules\\PRIVILEGE.json")) {
            String jsonRule = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            riskScanner.loadRule(jsonRule, false);
        }

        DBSettings dbSettings = new MySQLSettings("localhost", 3306, "root", "root");

        riskScanner.submitTask(ScanTask.of(dbSettings, RiskType.PRIVILEGE, new ScanTaskHandler() {

            @Override
            public void onStart(ScanTask scanTask) {
                System.out.println("onStart:" + scanTask.getTaskId());
            }

            @Override
            public void onAlert(ScanTask scanTask, ScanResult result) {
                Alert alert = result.getAlert();
                Rule rule = alert.getRule();
                String data = alert.getData();
                String taskId = scanTask.getTaskId();
                Double baseScore = rule.getBaseScore();
                String vectorString = rule.getVectorString();
                Severity severity = rule.getSeverity();
                System.out.println("onAlert:" + scanTask.getTaskId() + ":" + rule.getName() + ":" + data);
            }

            @Override
            public void onFinish(ScanTask scanTask) {
                System.out.println("onFinish:" + scanTask.getTaskId());
            }

            @Override
            public void onError(ScanTask scanTask, Throwable error) {
                System.out.println("onError:" + scanTask.getTaskId());
            }
        }));

        // 等待异步任务结束
        Thread.sleep(1000 * 60);

        // 关闭扫描器
        riskScanner.stop();

    }
}
