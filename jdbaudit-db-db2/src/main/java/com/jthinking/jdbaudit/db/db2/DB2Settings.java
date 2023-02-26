package com.jthinking.jdbaudit.db.db2;

import com.ibm.db2.jcc.DB2Driver;
import com.jthinking.jdbaudit.core.exception.AuthenticationFailedException;
import com.jthinking.jdbaudit.core.exception.ConnectionFailedException;
import com.jthinking.jdbaudit.core.exception.RegisterDriverFailedException;
import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBPassword;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.db.api.entity.PasswordParam;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeBuilder;
import us.springett.parsers.cpe.values.Part;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB2Settings extends AbstractDBSettings {


    /**
     * 主机名
     */
    private final String host;

    /**
     * 端口
     */
    private final Integer port;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;

    /**
     * 数据库
     */
    private final String database;

    /**
     * 版本号提取
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");

    public DB2Settings(String host, Integer port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    @Override
    protected Connection openConnection() {
        try {
            return DriverManager.getConnection("jdbc:db2://" + host + ":" + port + "/" + database , username, password);
        } catch (Exception e) {
            if (e.getMessage().contains("invalid username/password")) {
                throw new AuthenticationFailedException("数据库用户名或密码错误");
            }
            if (e.getMessage().contains("could not establish the connection")
                    || e.getMessage().contains("Connection refused: connect")
                    || e.getMessage().contains("Connection timed out")) {
                throw new ConnectionFailedException("数据库连接失败");
            }
            if (e.getMessage().contains("未找到该数据库")) {
                throw new ConnectionFailedException("数据库实例不存在");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerDriver() {
        synchronized (DBSettings.class) {
            try {
                DriverManager.registerDriver(new DB2Driver());
            } catch (Exception e) {
                throw new RegisterDriverFailedException("数据库驱动注册失败");
            }
        }
    }

    @Override
    public String getDBId() {
        return "db2";
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public Integer getPort() {
        return this.port;
    }

    @Override
    public DBVersion getVersion() {
        try (Connection conn = openConnection()) {
            DBVersion dbVersion = new DBVersion();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT SERVICE_LEVEL, FIXPACK_NUM FROM SYSIBMADM.ENV_INST_INFO")) {
                    if (rs.next()) {
                        String serviceLevel = rs.getString(1);
                        Matcher matcher = VERSION_PATTERN.matcher(serviceLevel);
                        if (matcher.find()) {
                            List<Cpe> cpeList = new ArrayList<>();
                            String version = matcher.group();
                            String fixPackNum = rs.getString(2);
                            if (!fixPackNum.equals("0") && !fixPackNum.equals("")) {
                                Cpe cpe1 = new CpeBuilder().part(Part.APPLICATION).vendor("ibm").product("db2").version(version).update("fp" + fixPackNum).build();
                                cpeList.add(cpe1);

                                Cpe cpe2 = new CpeBuilder().part(Part.APPLICATION).vendor("ibm").product("db2").version(version).update("fixpak" + fixPackNum).build();
                                cpeList.add(cpe2);
                            } else {
                                Cpe cpe = new CpeBuilder().part(Part.APPLICATION).vendor("ibm").product("db2").version(version).build();
                                cpeList.add(cpe);
                            }
                            dbVersion.setVendor("ibm");
                            dbVersion.setProduct("db2");
                            dbVersion.setVersion(version);
                            dbVersion.setUpdate("fixpak" + fixPackNum);
                            dbVersion.setCpeList(cpeList);
                        }
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
            }
            return dbVersion;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DBPassword> getPassword() {
        List<DBPassword> passwords = new ArrayList<>();
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select AUTHID from sysibmadm.AUTHORIZATIONIDS where AUTHIDTYPE = 'U'")) {
                    while (rs.next()) {
                        String username = rs.getString(1).trim().toLowerCase();
                        passwords.add(new DBPassword(username, username, "null"));
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return passwords;
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
