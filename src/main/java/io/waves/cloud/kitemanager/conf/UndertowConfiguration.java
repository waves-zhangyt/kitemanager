/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.conf;

import io.undertow.UndertowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * undertow web container http configuration
 * <pre>the default is https</pre>
 * @author ytzhang0828@qq.com
 */
@Configuration
public class UndertowConfiguration {

    @Value("${server.http-port}")
    private int httpPort;
    @Value("${server.port}")
    private int httpsPort;

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        UndertowEmbeddedServletContainerFactory undertowFactory = new UndertowEmbeddedServletContainerFactory();
        undertowFactory.addBuilderCustomizers((UndertowBuilderCustomizer) builder -> {
            builder.addHttpListener(httpPort, "0.0.0.0");
            builder.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true);
        });
        return undertowFactory;
    }

}
