package com.jthinking.jdbaudit.scan.audit.data;

import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import com.jthinking.jdbaudit.db.api.entity.QueryResult;
import com.jthinking.jdbaudit.scan.api.DBData;

import java.util.Map;

public class DBConfigData extends QueryListResult implements DBData {

    public DBConfigData(Map<String, QueryResult> queryResultMap) {
        super(queryResultMap);
    }

    @Override
    public String toDataString() {
        return "";
    }

}
