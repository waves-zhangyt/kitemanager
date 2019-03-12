/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.ro;

import io.waves.cloud.kitemanager.websocket.CmdResult;

/**
 * 通用接口命令返回结果
 * @author ytzhang0828@qq.com
 */
public class ClientCmdResult {

    private String clientId;

    private CmdResult cmdResult;

    public ClientCmdResult(String clientId, CmdResult cmdResult) {
        this.clientId = clientId;
        this.cmdResult = cmdResult;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public CmdResult getCmdResult() {
        return cmdResult;
    }

    public void setCmdResult(CmdResult cmdResult) {
        this.cmdResult = cmdResult;
    }

    @Override
    public String toString() {
        return "ClientCmdResult{" +
                "clientId='" + clientId + '\'' +
                ", cmdResult=" + cmdResult +
                '}';
    }
}
