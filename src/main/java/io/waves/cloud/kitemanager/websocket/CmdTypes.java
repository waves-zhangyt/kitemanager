/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

/**
 * 命令或消息类型
 * @author ytzhang0828@qq.com
 */
public abstract class CmdTypes {

    /** response error */
    public static String res_error = "res_error";

    /** response ok */
    public static String res_ok = "res_ok";

    /** response pong */
    public static String res_pong = "res_pong";

}
