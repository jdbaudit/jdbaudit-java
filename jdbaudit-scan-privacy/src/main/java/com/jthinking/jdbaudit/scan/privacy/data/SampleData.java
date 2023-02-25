package com.jthinking.jdbaudit.scan.privacy.data;

import com.jthinking.jdbaudit.db.api.entity.QueryResult;
import com.jthinking.jdbaudit.scan.api.DBData;

public record SampleData(QueryResult queryResult) implements DBData {

    @Override
    public String toDataString() {
        return queryResult.getQuery();
    }
}
