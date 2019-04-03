/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * digest util for md5 and sha
 * @author ytzhang0828@qq.com
 */
public class DigestUtil {

    /** md5算法 */
    public static final String ALGORITHM_MD5 = "MD5";
    /** sha1算法 */
    public static final String ALGORITHM_SHA1 = "SHA1";

    public static String digestFromUtf8Text(String text, String algorithm) {
        try {
            byte[] data = text.getBytes("UTF-8");
            return digest(data, algorithm);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算摘要信息
     *
     * @param data 数据
     * @param algorithm 算法
     * @return String
     */
    public static String digest(byte[] data, String algorithm) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(data);
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /**
     * 把摘要数据转换成16进制数字字符串形式
     *
     * @param bytes
     *            摘要数据
     * @return 16进制数字字符串.
     */
    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

}
