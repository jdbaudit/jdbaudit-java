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


import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database information acquisition interface
 * @author Jia Bochao
 */
public abstract class AbstractDBSettings implements DBSettings {

    private Connection connection;


    protected Connection getConnection() {
        return this.connection;
    }

    /**
     * 打开数据库连接
     */
    protected abstract void openConnection();

    /**
     * 关闭数据库连接。
     * 同一对象可反复关闭、打开。何时关闭是问题。建议增加超时无查询自动关闭。
     */
    protected void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                this.connection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void registerDriver();

}
