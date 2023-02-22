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
package com.jthinking.jdbaudit.db.api;


import com.jthinking.jdbaudit.db.api.entity.*;

import java.util.List;

/**
 * Database information acquisition interface
 * @author Jia Bochao
 */
public interface DBSettings {

    /**
     * Gets the database ID. e.g. mysql, oracle, mssql
     * @return database ID
     */
    String getDBId();

    /**
     * 获取数据库连接主机
     * @return 数据库连接主机名
     */
    String getHost();

    /**
     * 获取数据库连接端口
     * @return 数据库连接端口
     */
    Integer getPort();

    /**
     * 获取版本信息
     * @return
     */
    DBVersion getVersion();

    /**
     * 获取用户密码
     * @return
     */
    List<DBPassword> getPassword();

    /**
     * 查询数据
     * @param queries 查询语句
     * @return map.key: query statement, map.value: query statement对应的数据列表
     */
    QueryListResult query(List<String> queries);

    QueryResult query(String sql);

    /**
     * 查询数据样例
     * @return map.key: query statement, map.value: query statement对应的数据列表
     */
    QueryListResult getDataSample();

    /**
     * 明文密文比较。
     * @param passwordParam
     * @return 相同返回true，否则返回false
     */
    boolean comparePassword(PasswordParam passwordParam);

    /**
     * 测试数据库连接
     * @return
     */
    boolean testConnection();

}
