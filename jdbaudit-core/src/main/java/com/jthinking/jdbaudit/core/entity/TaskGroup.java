/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jthinking.jdbaudit.core.entity;


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风险扫描任务组
 * @author Jia Bochao
 */
public class TaskGroup {

    /**
     * 任务ID。格式：SG+时间戳。e.g. SG1659498653061
     */
    private String id;

    /**
     * 任务名称。用户自定义名称。可不填
     */
    private String name;

    /**
     * 应用的风险类型ID列表
     */
    private List<RiskType> riskTypes;

    /**
     * 应用的规则来源
     */
    private List<RuleSource> ruleSources;

    /**
     * 应用的数据库实例ID列表
     */
    private List<DBInstance> dbInstances;

    /**
     * 任务总数
     */
    private Integer taskTotal;

    /**
     * 已完成任务数
     */
    private Integer taskFinish;

    /**
     * 告警总数
     */
    private Integer alertTotal;

    /**
     * 任务状态。0任务创建，1任务开始，2任务完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 任务列表
     */
    private List<Task> tasks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RiskType> getRiskTypes() {
        return riskTypes;
    }

    public void setRiskTypes(List<RiskType> riskTypes) {
        this.riskTypes = riskTypes;
    }

    public List<RuleSource> getRuleSources() {
        return ruleSources;
    }

    public void setRuleSources(List<RuleSource> ruleSources) {
        this.ruleSources = ruleSources;
    }

    public List<DBInstance> getDbInstances() {
        return dbInstances;
    }

    public void setDbInstances(List<DBInstance> dbInstances) {
        this.dbInstances = dbInstances;
    }

    public Integer getTaskTotal() {
        return taskTotal;
    }

    public void setTaskTotal(Integer taskTotal) {
        this.taskTotal = taskTotal;
    }

    public Integer getTaskFinish() {
        return taskFinish;
    }

    public void setTaskFinish(Integer taskFinish) {
        this.taskFinish = taskFinish;
    }

    public Integer getAlertTotal() {
        return alertTotal;
    }

    public void setAlertTotal(Integer alertTotal) {
        this.alertTotal = alertTotal;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<Task> getRiskScanTasks() {
        return tasks;
    }

    public void setRiskScanTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getRiskTypesString() {
        if (riskTypes != null) {
            return riskTypes.stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(","));
        } else {
            return "";
        }
    }

    public void setRiskTypesString(String riskTypesString) {
        if (riskTypesString != null) {
            String[] types = riskTypesString.split(",");
            List<RiskType> riskTypeList = Arrays.stream(types)
                    .map(RiskType::valueOf)
                    .collect(Collectors.toList());
            setRiskTypes(riskTypeList);
        }
    }

    public String getDbInstanceIdsString() {
        if (dbInstances != null) {
            return dbInstances.stream()
                    .map(dbInstance -> String.valueOf(dbInstance.getId()))
                    .collect(Collectors.joining(","));
        } else {
            return "";
        }
    }

    public void setDbInstanceIdsString(String dbInstanceIdsString) {
        if (dbInstanceIdsString != null) {
            String[] ids = dbInstanceIdsString.split(",");
            List<DBInstance> dbInstanceList = Arrays.stream(ids)
                    .map(id -> {
                        DBInstance dbInstance = new DBInstance();
                        dbInstance.setId(Long.valueOf(id));
                        return dbInstance;
                    })
                    .collect(Collectors.toList());
            setDbInstances(dbInstanceList);
        }
    }

}
