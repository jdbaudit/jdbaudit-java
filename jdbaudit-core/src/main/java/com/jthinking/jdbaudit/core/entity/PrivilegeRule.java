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

import com.jthinking.jdbaudit.core.util.scap.CCSSCalculator;

import java.util.List;

/**
 * 权限审计规则实体
 * @author Jia Bochao
 */
public class PrivilegeRule {

    public PrivilegeRule() {
    }

    /**
     * 待检测数据查询语句，用于提供检测数据。
     * 目前为原生SQL语句，后期可适配为更多类型的数据库，做成通用化的查询语句
     * 参考：Presto、Trino
     * universal database query language
     */
    private String query;

    /**
     * 逻辑表达式
     */
    private String expression;

    private List<PrivilegeCondition> conditions;

    /**
     * CCSS向量
     */
    private CCSSVector ccss;

    /**
     * CCSS向量字符串
     */
    private String vectorString;

    /**
     * CCSS基础得分
     */
    private Double baseScore;

    public CCSSVector getCcss() {
        return ccss;
    }

    public void setCcss(CCSSVector ccss) {
        this.ccss = ccss;
    }

    public String getVectorString() {
        if (vectorString == null || vectorString.equals("")) {
            if (ccss != null) {
                return CCSSCalculator.vectorString(ccss);
            }
        }
        return vectorString;
    }

    public void setVectorString(String vectorString) {
        this.vectorString = vectorString;
    }

    public Double getBaseScore() {
        if (baseScore == null) {
            if (ccss != null) {
                return CCSSCalculator.baseScore(ccss);
            }
        }
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<PrivilegeCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<PrivilegeCondition> conditions) {
        this.conditions = conditions;
    }
}
