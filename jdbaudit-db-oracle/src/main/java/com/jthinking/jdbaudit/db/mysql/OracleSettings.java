package com.jthinking.jdbaudit.db.mysql;

import com.jthinking.jdbaudit.core.exception.AuthenticationFailedException;
import com.jthinking.jdbaudit.core.exception.ConnectionFailedException;
import com.jthinking.jdbaudit.core.exception.RegisterDriverFailedException;
import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.*;
import oracle.jdbc.driver.OracleDriver;
import org.apache.commons.codec.binary.Hex;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeBuilder;
import us.springett.parsers.cpe.exceptions.CpeValidationException;
import us.springett.parsers.cpe.values.Part;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleSettings extends AbstractDBSettings {

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
     * 数据库实例名
     */
    private final String database;

    private final Pattern PASSWORD_PATTERN = Pattern.compile("'S:(?<password>\\w+);");

    public OracleSettings(String host, Integer port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    @Override
    protected Connection openConnection() {
        try {
            Properties props = new Properties();
            props.put("user", username);
            props.put("password", password);
            props.put("oracle.net.CONNECT_TIMEOUT", "5000");
            props.put("oracle.jdbc.ReadTimeout", "5000");
            return DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":" + port + database, props);
        } catch (Exception e) {
            if (e.getMessage().contains("invalid username/password")) {
                throw new AuthenticationFailedException("数据库用户名或密码错误");
            }
            if (e.getMessage().contains("could not establish the connection")) {
                throw new ConnectionFailedException("数据库连接失败");
            }
            if (e.getMessage().contains("TNS:listener was not given the SERVICE_NAME in CONNECT_DATA")) {
                throw new ConnectionFailedException("数据库实例名未指定");
            }
            if (e.getMessage().contains("Invalid connection string format")) {
                throw new ConnectionFailedException("数据库连接字符串格式错误");
            }
            if (e.getMessage().contains("TNS:listener does not currently know of service requested in connect descriptor")) {
                throw new ConnectionFailedException("数据库实例不存在");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerDriver() {
        synchronized (DBSettings.class) {
            try {
                DriverManager.registerDriver(new OracleDriver());
            } catch (Exception e) {
                throw new RegisterDriverFailedException("数据库驱动注册失败");
            }
        }
    }

    @Override
    public String getDBId() {
        return "oracle";
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
                String version = null;
                try (ResultSet rs = st.executeQuery("SELECT VERSION_FULL FROM V$INSTANCE")) {
                    if (rs.next()) {
                        version = rs.getString(1);
                    }
                } catch (Exception e) {
                    try (ResultSet rs = st.executeQuery("SELECT VERSION FROM V$INSTANCE")) {
                        if (rs.next()) {
                            version = rs.getString(1);

                        }
                    }
                }

                List<Cpe> cpeList = new ArrayList<>();

                Cpe cpe1 = new CpeBuilder().part(Part.APPLICATION).vendor("oracle").product("database_server").version(version).build();
                cpeList.add(cpe1);

                Cpe cpe2 = new CpeBuilder().part(Part.APPLICATION).vendor("oracle").product("database").version(version).build();
                cpeList.add(cpe2);

                dbVersion.setVendor("oracle");
                dbVersion.setProduct("database");
                dbVersion.setVersion(version);
                dbVersion.setCpeList(cpeList);
            } catch (CpeValidationException e) {
                throw new RuntimeException(e);
            }
            return dbVersion;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DBPassword> getPassword() {
        try (Connection conn = openConnection()) {
            List<DBPassword> passwords = new ArrayList<>();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(
                        "select username, dbms_metadata.get_ddl('USER', username) from dba_users")) {
                    while (rs.next()) {
                        String key = rs.getString(1).toLowerCase();
                        String value = rs.getString(2);
                        Matcher matcher = PASSWORD_PATTERN.matcher(value);
                        String password;
                        if (matcher.find()) {
                            password = matcher.group("password");
                        } else {
                            password = "NO AUTHENTICATION";
                        }
                        passwords.add(new DBPassword(key, key, password));
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
            }
            return passwords;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryListResult getDataSample() {
        try (Connection conn = openConnection()) {
            Map<String, QueryResult> dbDataSampleMap = new HashMap<>();
            try (Statement st = conn.createStatement()) {
                Set<String> tables = new HashSet<>();
                try (ResultSet rs = st.executeQuery("SELECT OWNER, TABLE_NAME FROM ALL_TABLES WHERE OWNER not in ('SYS', 'SYSTEM', 'DBSNMP', 'APPQOSSYS', 'DBSFWUSER', 'CTXSYS', 'DVSYS', 'AUDSYS', 'GSMADMIN_INTERNAL', 'MDSYS', 'LBACSYS', 'OUTLN', 'XDB')")) {
                    while (rs.next()) {
                        String tableSchema = rs.getString(1);
                        String tableName = rs.getString(2);
                        tables.add(tableSchema + "." + tableName);
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
                for (String table : tables) {
                    String query = "SELECT * FROM " + table + " WHERE rownum = 1";
                    try (ResultSet rs = st.executeQuery(query)) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        Map<String, Integer> label = new HashMap<>();
                        for (int i = 0; i < columnCount; i++) {
                            String columnLabel = metaData.getColumnLabel(i + 1);
                            label.put(columnLabel, i);
                        }
                        QueryResult queryResult = new QueryResult();
                        queryResult.setQuery(query);
                        queryResult.setLabel(label);
                        List<String[]> rowDataList = new ArrayList<>();
                        if (rs.next()) {
                            String[] data = new String[columnCount];
                            for (int i = 0; i < columnCount; i++) {
                                data[i] = rs.getString(i + 1);
                            }
                            rowDataList.add(data);
                        }
                        queryResult.setData(rowDataList);
                        dbDataSampleMap.put(query, queryResult);
                    } catch (Throwable t) {
                        //
                    }
                }
            }
            return new QueryListResult(dbDataSampleMap);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean comparePassword(PasswordParam passwordParam) {
        String plaintext = passwordParam.getPlaintext();
        String originalCiphertext  = passwordParam.getOriginalCiphertext();
        return comparePassword11g(plaintext, originalCiphertext);
    }


    /**
     * Oracle 11g密文比较。
     * 经测试：密文的S段兼容12c，18c，19c，21c
     * <p>In Oracle Database 11g there is "S:" part and it is created as follows:</p>
     * <pre>password hash (20 bytes) = sha1(password + salt (10 bytes))</pre>
     * 参考：<a href="https://www.trustwave.com/en-us/resources/blogs/spiderlabs-blog/changes-in-oracle-database-12c-password-hashes/">Changes in Oracle Database 12c password hashes</a>
     * @param plaintext
     * @param originalCiphertext
     * @return
     */
    private boolean comparePassword11g(String plaintext, String originalCiphertext) {
        try {
            if (originalCiphertext.length() < 40) {
                return false;
            }
            String passwordHex = originalCiphertext.substring(0, 40);
            String saltHex = originalCiphertext.substring(40);
            byte[] salt = Hex.decodeHex(saltHex);
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(plaintext.getBytes(StandardCharsets.UTF_8));
            sha1.update(salt);
            byte[] digest = sha1.digest();
            String password = Hex.encodeHexString(digest);
            return password.equalsIgnoreCase(passwordHex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
