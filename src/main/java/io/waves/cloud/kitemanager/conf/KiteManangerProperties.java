package io.waves.cloud.kitemanager.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kitemanager")
public class KiteManangerProperties {

    /** is auth interceptor enabled */
    private boolean authEnabled;

    private int maxClientNumber;

    @Component
    @ConfigurationProperties(prefix = "kitemanager.openApiApp")
    public static class OpenApiApp {
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

    @Override
    public String toString() {
        return "KiteManangerProperties{" +
                "authEnabled=" + authEnabled +
                ", maxClientNumber=" + maxClientNumber +
                ", openApiApp=" + openApiApp +
                '}';
    }
}
