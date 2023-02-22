package com.jthinking.jdbaudit.db.mysql.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;

/**
 * MySQL caching_sha2_password plugin 密文生成算法
 */
public class CachingSHA2PasswordHashUtils {

    /**
     * SELECT host, user, CONCAT('$mysql',LEFT(authentication_string,6),'*',INSERT(HEX(SUBSTR(authentication_string,8)),41,0,'*')) AS hash
     * FROM mysql.user
     * WHERE plugin = 'caching_sha2_password' AND authentication_string NOT LIKE '%INVALIDSALTANDPASSWORD%';
     * @see <a href="https://github.com/hashcat/hashcat/blob/master/tools/test_modules/m07401.pm">m07401.pm</a>
     * @return
     */
    public static byte[] shaCrypts(int bits, byte[] key, byte[] salt, int loops) {
        int bytes = bits / 8;

        byte[] b = sha256(ByteBuffer.allocate(key.length + salt.length + key.length).put(key).put(salt).put(key).array());

        // Add for any character in the key one byte of the alternate sum.

        ByteBuffer tmp = ByteBuffer.allocate((16 + 256) * salt.length).put(key).put(salt);

        for (int i = key.length; i > 0; i -= bytes) {
            if (i > bytes) {
                tmp.put(b);
            } else {
                tmp.put(b, 0, i);
            }
        }

        // Take the binary representation of the length of the key and for every 1 add the alternate sum, for every 0 the key.

        for (int i = key.length; i > 0; i >>= 1) {
            if ((i & 1) != 0) {
                tmp.put(b);
            } else {
                tmp.put(key);
            }
        }

        byte[] a = sha256(getBytes(tmp));

        // NOTE, this will be the 'initial' $c value in the inner loop.

        // For every character in the password add the entire password.  produces DP

        tmp.clear();

        for (int i = 0; i < key.length; i++) {
            tmp.put(key);
        }

        byte[] dp = sha256(getBytes(tmp));

        // Create byte sequence P

        ByteBuffer p = ByteBuffer.allocate(dp.length * (key.length / bytes + 1));

        for (int i = key.length; i > 0; i -= bytes) {
            if (i > bytes) {
                p.put(dp);
            } else {
                p.put(dp, 0, i);
            }
        }

        // produce ds

        tmp.clear();

        int til = 16 + ord(substr(a, 0, 1));

        for (int i = 0; i < til; i++) {
            tmp.put(salt);
        }

        byte[] ds = sha256(getBytes(tmp));

        // Create byte sequence S

        ByteBuffer s = ByteBuffer.allocate(ds.length * (salt.length / bytes + 1));

        for (int i = salt.length; i > 0; i -= bytes) {
            if (i > bytes) {
                s.put(ds);
            } else {
                s.put(ds, 0, i);
            }
        }

        byte[] c = a; // Ok, we saved this, which will 'seed' our crypt value here in the loop.

        // now we do 5000 iterations of SHA2 (256 or 512)

        for (int i = 0; i < loops; i++) {
            if ((i & 1) != 0) {
                tmp.clear();
                tmp.put(getBytes(p));
            } else {
                tmp.clear();
                tmp.put(c);;
            }

            if ((i % 3) != 0) {
                tmp.put(getBytes(s));
            }

            if ((i % 7) != 0) {
                tmp.put(getBytes(p));
            }

            if ((i & 1) != 0) {
                tmp.put(c);
            } else {
                tmp.put(getBytes(p));
            }

            c = sha256(getBytes(tmp));
        }

        int inc1;
        int inc2;
        int mod;
        int end;

        if (bits == 256) {
            inc1 = 10;
            inc2 = 21;
            mod = 30;
            end = 0;
        } else {
            inc1 = 21;
            inc2 = 22;
            mod = 63;
            end = 21;
        }

        int i = 0;
        tmp.clear();

        do {
            tmp.put(to64 ((ord (substr (c, i, 1)) << 16) | (ord (substr (c, (i + inc1) % mod, 1)) << 8) | ord (substr (c, (i + inc1 * 2) % mod, 1)), 4));
            i = (i + inc2) % mod;
        } while (i != end);

        if (bits == 256) {
            tmp.put(to64 ((ord (substr (c, 31, 1)) << 8) | ord (substr (c, 30, 1)), 3));
        } else {
            tmp.put(to64  (ord (substr (c, 63, 1)), 2));
        }

        return getBytes(tmp);
    }

    private static final byte[] i64 = {
            '.', '/',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static byte[] substr(byte[] src, int begin, int length) {
        byte[] temp = new byte[length];
        ByteBuffer buffer = ByteBuffer.wrap(src);
        buffer.position(begin);
        buffer.get(temp, 0, length);
        return temp;
    }

    private static int ord(byte[] src) {
        return Byte.toUnsignedInt(src[0]);
    }

    private static byte[] to64(int v, int n) {
        ByteBuffer str = ByteBuffer.allocate(n);
        while (--n >= 0) {
            str.put(i64[v & 0x3F]);
            v >>= 6;
        }
        return getBytes(str);
    }

    private static byte[] sha256(byte[] data) {
        return DigestUtils.sha256(data);
    }

    private static byte[] getBytes(ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        byte[] temp = new byte[position];
        byteBuffer.position(0);
        byteBuffer.get(temp, 0, position);
        byteBuffer.position(position);
        return temp;
    }

}