package com.jthinking.jdbaudit.db.mysql;

import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBPassword;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.db.api.entity.PasswordParam;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;

import java.sql.Connection;
import java.util.List;

public class OracleSettings extends AbstractDBSettings {


    @Override
    protected Connection openConnection() {
        return null;
    }

    @Override
    protected void registerDriver() {

    }

    @Override
    public String getDBId() {
        return null;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Integer getPort() {
        return null;
    }

    @Override
    public DBVersion getVersion() {
        return null;
    }

    @Override
    public List<DBPassword> getPassword() {
        return null;
    }

    @Override
    public QueryListResult getDataSample() {
        return null;
    }

    @Override
    public boolean comparePassword(PasswordParam passwordParam) {
        return false;
    }
}
