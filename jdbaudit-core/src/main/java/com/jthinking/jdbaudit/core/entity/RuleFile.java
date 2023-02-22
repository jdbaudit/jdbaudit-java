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
 * 规则文件
 * @author Jia Bochao
 */
public class RuleFile {

    /**
     * 规则版本
     */
    private String version;

    /**
     * 规则文件
     */
    private byte[] ruleBytes;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 状态。1启用，0禁用
     */
    private Integer status;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getRuleBytes() {
        return ruleBytes;
    }

    public void setRuleBytes(byte[] ruleBytes) {
        this.ruleBytes = ruleBytes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
