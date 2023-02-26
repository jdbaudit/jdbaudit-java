package com.jthinking.jdbaudit.db.postgres;

import com.jthinking.jdbaudit.core.exception.AuthenticationFailedException;
import com.jthinking.jdbaudit.core.exception.ConnectionFailedException;
import com.jthinking.jdbaudit.core.exception.RegisterDriverFailedException;
import com.jthinking.jdbaudit.db.api.AbstractDBSettings;
import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.DBPassword;
import com.jthinking.jdbaudit.db.api.entity.DBVersion;
import com.jthinking.jdbaudit.db.api.entity.PasswordParam;
import com.jthinking.jdbaudit.db.api.entity.QueryListResult;
import org.apache.commons.codec.digest.DigestUtils;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeBuilder;
import us.springett.parsers.cpe.values.Part;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PostgresSettings extends AbstractDBSettings {

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

    public PostgresSettings(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Connection openConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://" + host + ":" + port + "/postgres?connectTimeout=5&socketTimeout=5", username, password);
        } catch (Exception e) {
            if (e.getMessage().contains("password authentication failed for user")) {
                throw new AuthenticationFailedException("数据库用户名或密码错误");
            }
            if (e.getMessage().contains("Connection to") && e.getMessage().contains("refused")) {
                throw new ConnectionFailedException("数据库连接失败");
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerDriver() {
        synchronized (DBSettings.class) {
            try {
                DriverManager.registerDriver(new org.postgresql.Driver());
            } catch (Exception e) {
                throw new RegisterDriverFailedException("数据库驱动注册失败");
            }
        }
    }

    @Override
    public String getDBId() {
        return "postgres";
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
                try (ResultSet rs = st.executeQuery("SHOW server_version")) {
                    if (rs.next()) {
                        String serverVersion = rs.getString(1);
                        int index = serverVersion.indexOf(" ");
                        if (index == -1) {
                            index = serverVersion.length();
                        }
                        String version = serverVersion.substring(0, index);
                        Cpe cpe = new CpeBuilder().part(Part.APPLICATION).vendor("postgresql").product("postgresql").version(version).build();
                        List<Cpe> cpeList = new ArrayList<>();
                        cpeList.add(cpe);
                        dbVersion.setVendor("postgresql");
                        dbVersion.setProduct("postgresql");
                        dbVersion.setVersion(version);
                        dbVersion.setCpeList(cpeList);
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
        try (Connection conn = openConnection()) {
            List<DBPassword> passwords = new ArrayList<>();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select usename, passwd from pg_shadow")) {
                    while (rs.next()) {
                        String user = rs.getString(1).toLowerCase();
                        String pass = rs.getString(2);
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
        String usernameText = passwordParam.getUsernameText();
        if (plaintext == null || originalCiphertext == null) {
            return false;
        }
        if (originalCiphertext.startsWith("SCRAM-SHA-256")) {
            // password_encryption = 'scram-sha-256'
            return comparePasswordWithScramSha256(plaintext, originalCiphertext);
        } else if (originalCiphertext.startsWith("md5")) {
            // password_encryption = md5 => md5(password+username)
            return comparePasswordWithMD5(plaintext, originalCiphertext, usernameText);
        } else {
            return false;
        }
    }

    /**
     * SCRAM-SHA-256$4096:SbVCL65Uuqj7t2bwaVj83g==$knONpaoZxVLSyTeoGJ+BgddqMtkw3BOuP/wTp0OLx70=:wPOSaknUwovOY3gmh2r8yiygAc0OQ5kUapUQ0QRvpYo=
     * @param plaintext 明文密码
     * @param originalCiphertext PostgreSQL scram-sha-256 密文
     * @return true 匹配，false 不匹配
     */
    public boolean comparePasswordWithScramSha256(String plaintext, String originalCiphertext) {
        try {
            String[] items = originalCiphertext.split("\\$");
            if (items.length != 3) {
                return false;
            }
            String type = items[0];
            if (!type.equals("SCRAM-SHA-256")) {
                return false;
            }
            String iterationAndSalt = items[1];
            int separatorIndex = iterationAndSalt.indexOf(':');
            if (separatorIndex == -1) {
                return false;
            }
            String iterationStr = iterationAndSalt.substring(0, separatorIndex);
            int iteration = Integer.parseInt(iterationStr);
            String saltBase64 = iterationAndSalt.substring(separatorIndex + 1);
            byte[] saltBytes = Base64.getDecoder().decode(saltBase64);

            byte[] pbkdf2Digest = pbkdf2Digest(plaintext.toCharArray(), saltBytes, iteration, 256);
            byte[] serverKeys = hmacSha256("Server Key".getBytes(), pbkdf2Digest);
            byte[] clientKeys = sha256(hmacSha256("Client Key".getBytes(), pbkdf2Digest));

            String serverKeysBase64 = Base64.getEncoder().encodeToString(serverKeys);
            String clientKeysBase64 = Base64.getEncoder().encodeToString(clientKeys);

            String hexString = String.format("SCRAM-SHA-256$%d:%s$%s:%s", iteration, saltBase64, clientKeysBase64, serverKeysBase64);
            return hexString.equalsIgnoreCase(originalCiphertext);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * md5a3556571e93b0d20722ba62be61e8c2d
     * @param plaintext 明文密码
     * @param originalCiphertext PostgreSQL md5 密文
     * @return true 匹配，false 不匹配
     */
    public boolean comparePasswordWithMD5(String plaintext, String originalCiphertext, String usernameText) {
        try {
            String hexString = "md5" + DigestUtils.md5Hex(plaintext + usernameText);
            return hexString.equalsIgnoreCase(originalCiphertext);
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] pbkdf2Digest(char[] password, byte[] salt, int iterationCount, int keyLength) throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey key = secretKeyFactory.generateSecret(spec);
        return key.getEncoded();
    }

    public static byte[] hmacSha256(byte[] salt, byte[] data) throws Exception {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(data, "HmacSHA256");
        sha256HMAC.init(secretKey);
        return sha256HMAC.doFinal(salt);
    }

    public static byte[] sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
}
