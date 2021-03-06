/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager;

import io.waves.cloud.kitemanager.websocket.KiteWebSocketEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * 引导类
 * @author ytzhang0828@qq.com
 */
@SpringBootApplication
public class KiteManagerApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(KiteManagerApplication.class, args);
        KiteWebSocketEndpoint.setApplicationContext(context);
    }

}
