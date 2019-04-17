package io.waves.cloud.kitemanager.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kitemanager")
public class KiteManangerProperties {

    /** is auth interceptor enabled */
    private boolean authEnabled;

    /** the max client number can connect */
    private int maxClientNumber;

    @Component
    @ConfigurationProperties(prefix = "kitemanager.openApiApp")
    public static class OpenApiApp {

        /** open api token timeout interval */
        private int tokenInterval;

        public int getTokenInterval() {
            return tokenInterval;
        }

        public void setTokenInterval(int tokenInterval) {
            this.tokenInterval = tokenInterval;
        }

        @Override
        public String toString() {
            return "OpenApiApp{" +
                    "tokenInterval=" + tokenInterval +
                    '}';
        }
    }

    private OpenApiApp openApiApp;

    /** if enable connection auth */
    private boolean connectionAuthEnabled;
    /** connection secret, the client must be take with when connect if "connectionAuthEnabled" is true */
    private String connectionSecret;

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public int getMaxClientNumber() {
        return maxClientNumber;
    }

    public void setMaxClientNumber(int maxClientNumber) {
        this.maxClientNumber = maxClientNumber;
    }

    public OpenApiApp getOpenApiApp() {
        return openApiApp;
    }

    public void setOpenApiApp(OpenApiApp openApiApp) {
        this.openApiApp = openApiApp;
    }

    public boolean isConnectionAuthEnabled() {
        return connectionAuthEnabled;
    }

    public void setConnectionAuthEnabled(boolean connectionAuthEnabled) {
        this.connectionAuthEnabled = connectionAuthEnabled;
    }

    public String getConnectionSecret() {
        return connectionSecret;
    }

    public void setConnectionSecret(String connectionSecret) {
        this.connectionSecret = connectionSecret;
    }

    @Override
    public String toString() {
        return "KiteManangerProperties{" +
                "authEnabled=" + authEnabled +
                ", maxClientNumber=" + maxClientNumber +
                ", openApiApp=" + openApiApp +
                ", connectionAuthEnabled=" + connectionAuthEnabled +
                ", connectionSecret='" + connectionSecret + '\'' +
                '}';
    }
}
