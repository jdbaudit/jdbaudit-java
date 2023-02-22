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

/**
 * 提交任务的返回结果
 * 用于提示用户数据库连接错误等错误信息
 * @author Jia Bochao
 */
public class TaskSubmitResult {

    /**
     * 数据库实例标识
     */
    private String db;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误返回错误信息
     */
    private String error;

    /**
     * 成功返回任务ID
     */
    private String taskId;

    public TaskSubmitResult(String db, boolean success, String error, String taskId) {
        this.db = db;
        this.success = success;
        this.error = error;
        this.taskId = taskId;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
