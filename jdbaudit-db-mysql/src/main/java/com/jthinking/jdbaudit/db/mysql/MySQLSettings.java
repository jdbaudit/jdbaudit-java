package com.jthinking.jdbaudit.db.mysql;

import com.jthinking.jdbaudit.core.exception.AuthenticationFailedException;
import com.jthinking.jdbaudit.core.exception.ConnectionFailedException;
import com.jthinking.jdbaudit.core.exception.RegisterDriverFailedException;
import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.*;
import com.jthinking.jdbaudit.db.mysql.util.CachingSHA2PasswordHashUtils;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeBuilder;
import us.springett.parsers.cpe.exceptions.CpeValidationException;
import us.springett.parsers.cpe.values.Part;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * MySQL数据库信息获取
 */
public class MySQLSettings extends AbstractDBSettings {

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


    public MySQLSettings(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getDBId() {
        return "mysql";
    }

    @Override
    public DBVersion getVersion() {
        String innodbVersion = null;
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("show variables like 'innodb_version'")) {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        innodbVersion = rs.getString(2);
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String version = innodbVersion == null ? "0" : innodbVersion;
        try {
            List<Cpe> cpeList = new ArrayList<>();

            Cpe cpe1 = new CpeBuilder().part(Part.APPLICATION).product("mysql").version(version).build();
            cpeList.add(cpe1);

            Cpe cpe2 = new CpeBuilder().part(Part.APPLICATION).product("mysql_server").version(version).build();
            cpeList.add(cpe2);

            Cpe cpe3 = new CpeBuilder().part(Part.APPLICATION).product("mysql_community_server").version(version).build();
            cpeList.add(cpe3);

            Cpe cpe4 = new CpeBuilder().part(Part.APPLICATION).product("mysql_enterprise_server").version(version).build();
            cpeList.add(cpe4);

            return new DBVersion(null, "mysql", version, null, cpeList);
        } catch (CpeValidationException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<DBPassword> getPassword() {
        List<DBPassword> passwords = new ArrayList<>();
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                // 5.6
                try (ResultSet rs = st.executeQuery("select `Host`, `User`, `Password` from mysql.user where plugin = 'mysql_native_password'")) {
                    while (rs.next()) {
                        String host = rs.getString(1);
                        String user = rs.getString(2).toLowerCase();
                        String pass = rs.getString(3);
                        if (pass.trim().equals("")) {
                            continue;
                        }
                        String key = user + "@" + host;
                        passwords.add(new DBPassword(key, user, pass));
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
                // 5.7
                try (ResultSet rs = st.executeQuery("select `Host`, `User`, `authentication_string` from mysql.user where plugin = 'mysql_native_password'")) {
                    while (rs.next()) {
                        String host = rs.getString(1);
                        String user = rs.getString(2).toLowerCase();
                        String pass = rs.getString(3);
                        if (pass.trim().equals("")) {
                            continue;
                        }
                        String key = user + "@" + host;
                        passwords.add(new DBPassword(key, user, pass));
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
                // 8.0
                try (ResultSet rs = st.executeQuery("SELECT host, user, CONCAT('$mysql',LEFT(authentication_string,6),'*',INSERT(HEX(SUBSTR(authentication_string,8)),41,0,'*')) AS hash FROM mysql.user WHERE plugin = 'caching_sha2_password' AND authentication_string NOT LIKE '%INVALIDSALTANDPASSWORD%'")) {
                    while (rs.next()) {
                        String host = rs.getString(1);
                        String user = rs.getString(2).toLowerCase();
                        String pass = rs.getString(3);
                        if (pass.trim().equals("")) {
                            continue;
                        }
                        String key = user + "@" + host;
                        passwords.add(new DBPassword(key, user, pass));
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
        Map<String, QueryResult> dbDataSampleMap = new HashMap<>();
        try (Connection conn = openConnection()) {
            try (Statement st = conn.createStatement()) {
                Set<String> tables = new HashSet<>();
                try (ResultSet rs = st.executeQuery(
                        "select TABLE_SCHEMA, TABLE_NAME from information_schema.tables where TABLE_SCHEMA not in ('information_schema', 'mysql', 'performance_schema', 'sys')")) {
                    while (rs.next()) {
                        String tableSchema = rs.getString(1);
                        String tableName = rs.getString(2);
                        tables.add(tableSchema + "." + tableName);
                    }
                } catch (Exception e) {
                    // 数据库版本不同可能导致特定语句执行失败
                }
                for (String table : tables) {
                    String query = "SELECT * FROM " + table + " limit 1";
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
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new QueryListResult(dbDataSampleMap);
    }

    /**
     * 密文比对
     * @param passwordParam 待尝试的明文密码
     */
    @Override
    public boolean comparePassword(PasswordParam passwordParam) {
        String plaintext = passwordParam.getPlaintext();
        String originalCiphertext = passwordParam.getOriginalCiphertext();
        if (originalCiphertext.startsWith("$mysql$A")) {
            return comparePasswordWithCachingSha2PasswordPlugin(plaintext, originalCiphertext);
        } else {
            return comparePasswordWithMysqlNativePasswordPlugin(plaintext, originalCiphertext);
        }
    }

    @Override
    public Connection openConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/mysql?connectTimeout=5000&socketTimeout=5000", username, password);
        } catch (Exception e) {
            if (e.getMessage().contains("Communications link failure")) {
                throw new ConnectionFailedException("数据库连接失败");
            }
            if (e.getMessage().contains("Access denied")) {
                throw new AuthenticationFailedException("数据库用户名或密码错误");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerDriver() {
        synchronized (DBSettings.class) {
            try {
                DriverManager.registerDriver(new Driver());
            } catch (Exception e) {
                throw new RegisterDriverFailedException("数据库驱动注册失败");
            }
        }
    }

    /**
     * MySQL mysql_native_password plugin 密码比对
     * @param plaintext
     * @param originalCiphertext
     * @return
     */
    public boolean comparePasswordWithMysqlNativePasswordPlugin(String plaintext, String originalCiphertext) {
        byte[] utf8 = plaintext.getBytes(StandardCharsets.UTF_8);
        return originalCiphertext.equalsIgnoreCase("*" + DigestUtils.sha1Hex(DigestUtils.sha1(utf8)));
    }

    /**
     * MySQL caching_sha2_password plugin 密码比对
     * @param plaintext
     * @param originalCiphertext
     * @return
     */
    public boolean comparePasswordWithCachingSha2PasswordPlugin(String plaintext, String originalCiphertext) {
        try {
            String[] items = originalCiphertext.split("\\*");
            String info = items[0];
            String saltHex = items[1];
            String digestHex = items[2];
            String[] infoItems = info.split("\\$");
            int seed = Integer.parseInt(infoItems[3]);
            byte[] newDigest = CachingSHA2PasswordHashUtils.shaCrypts(256, plaintext.getBytes(), Hex.decodeHex(saltHex), seed * 1000);
            String newDigestHex = Hex.encodeHexString(newDigest);
            return newDigestHex.equalsIgnoreCase(digestHex);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * MySQL sha256_password plugin 密码比对
     * @param plaintext
     * @param originalCiphertext
     * @return
     */
    public boolean comparePasswordWithSha256PasswordPlugin(String plaintext, String originalCiphertext) {
        return comparePasswordWithCachingSha2PasswordPlugin(plaintext, originalCiphertext);
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public Integer getPort() {
        return this.port;
    }

    

}