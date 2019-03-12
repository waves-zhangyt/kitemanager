/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.util;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * json adapter
 * @author ytzhang0828@qq.com
 */
public class JSONUtil {

    public static String encodeJSONString(Object object) {
        if (object == null) return null;
        return JSON.toJSONString(object);
    }

    public static <T> T decodeJSONString(String text, Class<T> clazz) {
        if (clazz.equals(Map.class)) {
            return (T) JSON.parse(text);
        }

        if (text == null) return null;
        return JSON.parseObject(text, clazz);
    }

    public static Map<String, Object> decodeJSONString(String text) {
        return (Map<String, Object>) JSON.parse(text);
    }

}
