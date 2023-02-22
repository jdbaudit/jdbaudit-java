package com.jthinking.jdbaudit.db.api.entity;

public class PasswordParam {

    /**
     * 待尝试的明文密码
     */
    private String plaintext;

    /**
     * 待比较的原始密文密码。某些数据库原始密文中带有salt信息，用于密文生成。
     */
    private String originalCiphertext;

    /**
     * 密码对应的用户名
     */
    private String usernameText;

    public PasswordParam(String plaintext, String originalCiphertext, String usernameText) {
        this.plaintext = plaintext;
        this.originalCiphertext = originalCiphertext;
        this.usernameText = usernameText;
    }

    public String getPlaintext() {
        return plaintext;
    }

    public void setPlaintext(String plaintext) {
        this.plaintext = plaintext;
    }

    public String getOriginalCiphertext() {
        return originalCiphertext;
    }

    public void setOriginalCiphertext(String originalCiphertext) {
        this.originalCiphertext = originalCiphertext;
    }

    public String getUsernameText() {
        return usernameText;
    }

    public void setUsernameText(String usernameText) {
        this.usernameText = usernameText;
    }
}
