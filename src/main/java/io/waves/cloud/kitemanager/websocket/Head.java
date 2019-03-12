/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

/**
 * cmd head
 * @author ytzhang0828@qq.com
 */
public class Head {

    private String type;
    private String jobId;
    private Integer timeout; //单位秒
    private int async; //是否是异步 0 同步,  1 异步; 默认 0

    public Head() { }

    public Head(String type, String jobId) {
        this.type = type;
        this.jobId = jobId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public int getAsync() {
        return async;
    }

    public void setAsync(int async) {
        this.async = async;
    }

    @Override
    public String toString() {
        return "Head{" +
                "type='" + type + '\'' +
                ", jobId='" + jobId + '\'' +
                ", timeout=" + timeout +
                ", async=" + async +
                '}';
    }
}
