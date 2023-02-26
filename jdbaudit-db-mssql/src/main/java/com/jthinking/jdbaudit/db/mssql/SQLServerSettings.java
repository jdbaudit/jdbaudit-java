package com.jthinking.jdbaudit.db.mssql;

import com.jthinking.jdbaudit.core.exception.AuthenticationFailedException;
import com.jthinking.jdbaudit.core.exception.ConnectionFailedException;
import com.jthinking.jdbaudit.core.exception.RegisterDriverFailedException;
import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBPassword;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.db.api.entity.PasswordParam;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeBuilder;
import us.springett.parsers.cpe.exceptions.CpeValidationException;
import us.springett.parsers.cpe.values.Part;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLServerSettings extends AbstractDBSettings {

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

    public SQLServerSettings(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Connection openConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:sqlserver://" + host + ":" + port + ";encrypt=true;trustServerCertificate=true;socketTimeout=5000;loginTimeout=5", username, password);
        } catch (Exception e) {
            if (e.getMessage().contains("Login failed for user")) {
                throw new AuthenticationFailedException("数据库用户名或密码错误");
            }
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("connect timed out")) {
                throw new ConnectionFailedException("数据库连接失败");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerDriver() {
        synchronized (DBSettings.class) {
            try {
                DriverManager.registerDriver(new SQLServerDriver());
            } catch (Exception e) {
                throw new RegisterDriverFailedException("数据库驱动注册失败");
            }
        }
    }

    @Override
    public String getDBId() {
        return "mssql";
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
                try (ResultSet rs = st.executeQuery("SELECT CONVERT(varchar(200), SERVERPROPERTY('productversion')) as v, CONVERT(varchar(200), SERVERPROPERTY('productlevel')) as sp")) {
                    if (rs.next()) {
                        String version = rs.getString(1).toLowerCase();
                        String sp = rs.getString(2).toLowerCase();

                        String yearVersion = null;
                        if (version.startsWith("8.")) {
                            yearVersion = "2000";
                        } else if (version.startsWith("9.")) {
                            yearVersion = "2005";
                        } else if (version.startsWith("10.")) {
                            yearVersion = "2008";
                        } else if (version.startsWith("11.")) {
                            yearVersion = "2012";
                        } else if (version.startsWith("12.")) {
                            yearVersion = "2014";
                        } else if (version.startsWith("13.")) {
                            yearVersion = "2016";
                        } else if (version.startsWith("14.")) {
                            yearVersion = "2017";
                        } else if (version.startsWith("15.")) {
                            yearVersion = "2019";
                        } else if (version.startsWith("16.")) {
                            yearVersion = "2022";
                        }

                        List<Cpe> cpeList = new ArrayList<>();

                        Cpe cpe1 = new CpeBuilder().part(Part.APPLICATION).vendor("microsoft").product("sql_server").version(version).update(sp).build();
                        cpeList.add(cpe1);

                        if (yearVersion != null) {
                            Cpe cpe2 = new CpeBuilder().part(Part.APPLICATION).vendor("microsoft").product("sql_server").version(yearVersion).update(sp).build();
                            cpeList.add(cpe2);
                        }

                        dbVersion.setVendor("microsoft");
                        dbVersion.setProduct("sql_server");
                        dbVersion.setVersion(version);
                        dbVersion.setUpdate(sp);
                        dbVersion.setCpeList(cpeList);
                    }
                } catch (CpeValidationException e) {
                    throw new RuntimeException(e);
                }
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
                try (ResultSet rs = st.executeQuery("SELECT name, password_hash FROM sys.sql_logins where is_disabled = 'false'")) {
                    while (rs.next()) {
                        String user = rs.getString(1).toLowerCase();
                        String pass = "0x" + rs.getString(2);
                        passwords.add(new DBPassword(user, user, pass));
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
        return null;
    }


    @Override
    public boolean comparePassword(PasswordParam passwordParam) {
        String plaintext = passwordParam.getPlaintext();
        String originalCiphertext = passwordParam.getOriginalCiphertext();
        if (plaintext == null || originalCiphertext == null) {
            return false;
        }
        if (originalCiphertext.startsWith("0x0200")) {
            return comparePasswordWithMSSQL2012_2014(plaintext, originalCiphertext);
        } else if (originalCiphertext.startsWith("0x0100") && originalCiphertext.length() == 54) {
            return comparePasswordWithMSSQL2005(plaintext, originalCiphertext);
        } else if (originalCiphertext.startsWith("0x0100") && originalCiphertext.length() == 94) {
            return comparePasswordWithMSSQL2000(plaintext, originalCiphertext);
        } else {
            return false;
        }
    }

    public boolean comparePasswordWithMSSQL2012_2014(String plaintext, String originalCiphertext) {
        try {
            String prefix = "0x0200";
            if (!originalCiphertext.startsWith(prefix)) {
                return false;
            }
            String salt = originalCiphertext.substring(6, 6 + 8);
            byte[] saltBytes = Hex.decodeHex(salt);
            byte[] passwordBytes = plaintext.getBytes(StandardCharsets.UTF_16LE);
            byte[] bytes = ByteBuffer.allocate(saltBytes.length + passwordBytes.length).put(passwordBytes).put(saltBytes).array();
            String digest = DigestUtils.sha512Hex(bytes);
            String hex = prefix + salt + digest;
            return originalCiphertext.equalsIgnoreCase(hex);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean comparePasswordWithMSSQL2005(String plaintext, String originalCiphertext) {
        try {
            String prefix = "0x0100";
            if (!originalCiphertext.startsWith(prefix)) {
                return false;
            }
            String salt = originalCiphertext.substring(6, 6 + 8);
            byte[] saltBytes = Hex.decodeHex(salt);
            byte[] passwordBytes = plaintext.getBytes(StandardCharsets.UTF_16LE);
            byte[] bytes = ByteBuffer.allocate(saltBytes.length + passwordBytes.length).put(passwordBytes).put(saltBytes).array();
            String digest = DigestUtils.sha1Hex(bytes);
            String hex = prefix + salt + digest;
            return originalCiphertext.equalsIgnoreCase(hex);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean comparePasswordWithMSSQL2000(String plaintext, String originalCiphertext) {
        try {
            String prefix = "0x0100";
            if (!originalCiphertext.startsWith(prefix)) {
                return false;
            }
            String salt = originalCiphertext.substring(6, 6 + 8);
            byte[] saltBytes = Hex.decodeHex(salt);
            byte[] passwordBytes = plaintext.toUpperCase().getBytes(StandardCharsets.UTF_16LE);
            byte[] bytes = ByteBuffer.allocate(saltBytes.length + passwordBytes.length).put(passwordBytes).put(saltBytes).array();
            String digest = DigestUtils.sha1Hex(bytes);
            String hex = String.format("%s%s%040d%s", prefix, salt, 0, digest);
            return originalCiphertext.equalsIgnoreCase(hex);
        } catch (Exception e) {
            return false;
        }
    }
}
