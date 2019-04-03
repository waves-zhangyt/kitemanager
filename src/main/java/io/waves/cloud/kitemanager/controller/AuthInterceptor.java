package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.db.OpenApiApp;
import io.waves.cloud.kitemanager.db.OpenApiAppMapper;
import io.waves.cloud.kitemanager.db.User;
import io.waves.cloud.kitemanager.db.UserMapper;
import io.waves.cloud.kitemanager.util.DigestUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限过滤
 * @author ytzhang0828@qq.com
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OpenApiAppMapper openApiAppMapper;

    @Value("${server.context-path}")
    private String contextPath;

    @Value("${kitemanager.openApiApp.tokenInterval}")
    private long tokenInterval;

    @Value("${kitemanager.authEnabled}")
    private boolean authEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!authEnabled) return true;

        // 1.white list region
        String uri = request.getRequestURI();
        if (uri.startsWith("/kite/h2-console/")) return true;
        String[] whiteUris = new String[] {
                contextPath + "/user/login.html",
                contextPath + "/user/login",
                contextPath + "/error"
        };
        for (String item : whiteUris) {
            if (item.equals(uri)) return true;
        }

        // 2.open api token region
        String appId = request.getHeader("kAppId");
        String appToken = request.getHeader("kAppToken");
        String timestamp = request.getHeader("kTimestamp");
        if (!StringUtil.isEmpty(appId) && !StringUtil.isEmpty(appToken)
                && !StringUtil.isEmpty(timestamp)) {
            if (isValidToken(appId, appToken, Long.parseLong(timestamp))) return true;
        }

        // 3.login region, use session just now
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            //跳转到登录页面
            response.sendRedirect(contextPath + "/user/login.html?priUrl=" + uri);
            return true;
        }

        return true;
    }

    /**
     * check is valid token with a md5 algorithm
     * @param appId
     * @param appToken
     * @param timestamp the time when create appToken. it's the seconds count from 1970/01/01
     */
    private boolean isValidToken(String appId, String appToken, long timestamp) {
        //check timeout
        long interval = System.currentTimeMillis() / 1000 - timestamp;
        if (interval > tokenInterval) {
            logger.debug("app token timeout: app-{}, timestamo-{}", appId, timestamp);
            return false;
        }

        OpenApiApp openApiApp = openApiAppMapper.getOpenApiAppByAppId(appId);
        if (openApiApp == null) {
            logger.debug("no appId {}", appId);
            return false;
        }

        String feed = appId + "-" + openApiApp.getSecret() + "-" + timestamp;
        String feedback = DigestUtil.digestFromUtf8Text(feed, DigestUtil.ALGORITHM_MD5);
        if (feedback.equalsIgnoreCase(appToken)) {
            return true;
        } else {
            logger.debug("appId {}, appToken {} is invalied", appId, appToken);
            return false;
        }
    }

    public void printNoLogin(HttpServletResponse response) throws Exception {
        response.setStatus(400);
        response.setContentType("text/html;charset=UTF-8");

        response.getWriter().print("未登录");
    }

}
