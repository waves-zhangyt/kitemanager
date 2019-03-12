/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import io.waves.cloud.kitemanager.util.JSONUtil;

/**
 * 传送给客户端的指令
 * @author ytzhang0828@qq.com
 */
public class Cmd {

    private Head head;
    private String body;

    public Cmd() {}

    public Cmd(String type, String body) {
        head = new Head(type, null);
        this.body = body;
    }

    public Cmd(String type, String jobId, String body) {
        head = new Head(type, jobId);
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Cmd{" +
                "head=" + head +
                ", body='" + body + '\'' +
                '}';
    }

    public String toJSONString() {
        return JSONUtil.encodeJSONString(this);
    }
}
