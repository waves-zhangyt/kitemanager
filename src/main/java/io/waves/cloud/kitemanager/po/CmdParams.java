/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.po;

import io.waves.cloud.kitemanager.websocket.Cmd;

/**
 * 基础命令接口参数
 * @author ytzhang0828@qq.com
 */
public class CmdParams {

    /** agentId */
    private String clientId;
    /** 具体指令,同agent指令结构 */
    private Cmd cmd;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Cmd getCmd() {
        return cmd;
    }

    public void setCmd(Cmd cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return "CmdParams{" +
                "clientId='" + clientId + '\'' +
                ", cmd=" + cmd +
                '}';
    }
}
