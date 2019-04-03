/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.db;

import java.util.Date;

/**
 * open api domain
 * @author ytzhang0828@qq.com
 */
public class OpenApiApp {

    private Integer id;
    private String appId;
    private String secret;
    private String uris;
    private Date createTime;
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUris() {
        return uris;
    }

    public void setUris(String uris) {
        this.uris = uris;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OpenApiApp{" +
                "id=" + id +
                ", appId='" + appId + '\'' +
                ", secret='" + secret + '\'' +
                ", uris='" + uris + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                '}';
    }
}
