package com.jthinking.jdbaudit.db.api.entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新API-SQL查询结果
 */
public class QueryResult {

    /**
     * 查询SQL语句
     */
    private String query;

    /**
     * 列名与返回列索引下标
     */
    private Map<String, Integer> label;

    /**
     * 返回行数
     */
    private int size;

    /**
     * 数据
     */
    private List<String[]> data;

    /**
     * 查询报错信息
     */
    private String error;

    public List<String> getDataByLabel(String label) {
        Integer index = this.label.get(label);
        return getDataByIndex(index);
    }

    public List<String> getDataByIndex(int index) {
        return this.data.parallelStream()
                .map(row -> row[index])
                .collect(Collectors.toList());
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Integer> getLabel() {
        return label;
    }

    public void setLabel(Map<String, Integer> label) {
        this.label = label;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String[]> getData() {
        return data;
    }

    public void setData(List<String[]> data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
