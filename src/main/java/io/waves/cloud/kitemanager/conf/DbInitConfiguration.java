/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * db init
 * @author ytzhang0828@qq.com
 */
@Component
public class DbInitConfiguration {

    private static Logger logger = LoggerFactory.getLogger(DbInitConfiguration.class);

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    /** 初始化表格 */
    @PostConstruct
    public void initTables() {

        //用户表初始化
        if (!existsTable("user")) {
            createTableUser();
        }

        if (!existsTable("open_api_app")) {
            createTabeOpenApiApp();
        }

    }

    /**
     * 创建 open_api_app 表
     * <pre>
     *     初始化数据
     *     insert into open_api_app(appId, secret, uris, createTime) values('admin', 'admin', '', now())
     *     todo, open all uri to echo app right now, may improve the feature in future.
     * </pre>
     */
    public void createTabeOpenApiApp() {
        String sql = "create table open_api_app(" +
                " id int identity primary key," +
                " appId varchar(50)," +
                " secret varchar(128)," +
                " uris text," +
                " createTime datetime," +
                " status int default 1" +
                " );" +
                " create index appId on open_api_app (appId);" +
                " insert into open_api_app(appId, secret, uris, createTime) values('admin', 'admin', '', now());";
        createTable(sql);
    }

    /**
     * 创建表
     * <pre>
     *     可用如下语句初始化一条数据
     *     insert into user(name, password, username, createTime) values('admin', 'admin', 'admin', now());
     * </pre>
     */
    public void createTableUser() {
        String sql = "create table user(" +
                " id int identity primary key," +
                " name varchar(50)," +
                " password varchar(50)," +
                " username varchar(50)," +
                " createTime datetime," +
                " status int default 1" +
                " );" +
                " create index name on user (name);" +
                " insert into user(name, password, username, createTime) values('admin', 'admin', 'admin', now());";
        createTable(sql);
    }

    private void createTable(String sql) {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
        ) {
            logger.info("创建表: {}", sql);
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 判断表是否存在 */
    public boolean existsTable(String tableName) {
        String sql = "show tables";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
        ) {
            while (rs.next()) {
                String name = rs.getString(1);
                // h2会把表名字转换成大写
                if (name.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
