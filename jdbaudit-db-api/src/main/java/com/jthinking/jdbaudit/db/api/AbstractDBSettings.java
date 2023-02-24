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


import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import com.jthinking.jdbaudit.db.api.entity.QueryResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database information acquisition interface
 * @author Jia Bochao
 */
public abstract class AbstractDBSettings implements DBSettings {

    public AbstractDBSettings() {
        registerDriver();
    }

    protected abstract Connection openConnection();

    protected abstract void registerDriver();

    @Override
    public QueryResult query(String sql) {
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                QueryResult queryResult = new QueryResult();
                try (ResultSet rs = st.executeQuery(sql)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    Map<String, Integer> label = new HashMap<>();
                    for (int i = 0; i < columnCount; i++) {
                        String columnLabel = metaData.getColumnLabel(i + 1);
                        label.put(columnLabel, i);
                    }
                    queryResult.setQuery(sql);
                    queryResult.setLabel(label);
                    List<String[]> rowDataList = new ArrayList<>();
                    while (rs.next()) {
                        String[] data = new String[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            data[i] = rs.getString(i + 1);
                        }
                        rowDataList.add(data);
                    }
                    queryResult.setData(rowDataList);
                } catch (Exception e) {
                    queryResult.setError(e.getMessage());
                }
                return queryResult;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryListResult query(List<String> queries) {
        Map<String, QueryResult> rowDataMap = new HashMap<>();
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                for (String query : queries) {
                    QueryResult queryResult = new QueryResult();
                    try (ResultSet rs = st.executeQuery(query)) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        Map<String, Integer> label = new HashMap<>();
                        for (int i = 0; i < columnCount; i++) {
                            String columnLabel = metaData.getColumnLabel(i + 1);
                            label.put(columnLabel, i);
                        }

                        queryResult.setQuery(query);
                        queryResult.setLabel(label);

                        List<String[]> rowDataList = new ArrayList<>();
                        while (rs.next()) {
                            String[] data = new String[columnCount];
                            for (int i = 0; i < columnCount; i++) {
                                data[i] = rs.getString(i + 1);
                            }
                            rowDataList.add(data);
                        }
                        queryResult.setData(rowDataList);
                    } catch (Exception e) {
                        queryResult.setError(e.getMessage());
                    }
                    rowDataMap.put(query, queryResult);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new QueryListResult(rowDataMap);
    }

    @Override
    public boolean testConnection() {
        try (Connection conn = openConnection()) {
            return conn != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
