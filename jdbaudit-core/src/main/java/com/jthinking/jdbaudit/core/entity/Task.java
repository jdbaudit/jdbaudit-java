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

import java.util.Date;

/**
 * 扫描任务
 * @author Jia Bochao
 */
public class Task {

    /**
     * 任务ID。格式：S+时间戳。e.g. S1659498653061
     */
    private String id;

    /**
     * 开启的风险类型。取值：VULNERABILITY、WEEK_PASSWORD、AUDIT，见：RiskType枚举',
     */
    private String riskType;

    /**
     * 数据库实例
     */
    private DBInstance dbInstance;

    /**
     * 告警总数
     */
    private Integer alertTotal;

    /**
     * 任务组ID
     */
    private TaskGroup taskGroup;

    /**
     * 任务状态。0任务创建，1任务开始，2任务完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRiskType() {
        return riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    public DBInstance getDbInstance() {
        return dbInstance;
    }

    public void setDbInstance(DBInstance dbInstance) {
        this.dbInstance = dbInstance;
    }

    public Integer getAlertTotal() {
        return alertTotal;
    }

    public void setAlertTotal(Integer alertTotal) {
        this.alertTotal = alertTotal;
    }

    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
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
}
