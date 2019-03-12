/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.waves.cloud.kitemanager.util.JSONUtil;

/**
 * cmd result
 * @author ytzhang0828@qq.com
 */
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class CmdResult {

    //结果类型（可不是命令的结果，而是客户端主动请求）
    private String type;

    private String jobId;
    @JsonIgnore
    private int async; //客户端通过调用不同的接口来实现，因此对于客户端可不见
    private Boolean isTimeout;
    private String stdout;
    private String stderr;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public int getAsync() {
        return async;
    }

    public void setAsync(int async) {
        this.async = async;
    }

    public Boolean getIsTimeout() {
        return isTimeout;
    }

    public void setIsTimeout(Boolean isTimeout) {
        this.isTimeout = isTimeout;
    }

    public static CmdResult decodeJSONString(String text) {
        return JSONUtil.decodeJSONString(text, CmdResult.class);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CmdResult{" +
                "type='" + type + '\'' +
                ", jobId='" + jobId + '\'' +
                ", async=" + async +
                ", isTimeout=" + isTimeout +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                '}';
    }
}
