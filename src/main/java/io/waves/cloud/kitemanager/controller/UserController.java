/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.db.User;
import io.waves.cloud.kitemanager.db.UserMapper;
import io.waves.cloud.kitemanager.ro.ResultRo;
import io.waves.cloud.kitemanager.util.StringUtil;
import io.waves.cloud.kitemanager.util.VelocityUtil;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * user about api controller
 * @author ytzhang0828@qq.com
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Value("${server.context-path}")
    private String contextPath;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("login.html")
    public String loginHtml(HttpServletRequest request, HttpServletResponse response) {
        String priUrl = request.getParameter("priUrl");
        response.setContentType("text/html;charset=UTF-8");

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("priUrl", priUrl);
        return VelocityUtil.merge(velocityContext, "io/waves/cloud/kitemanager/ro/vm/login.html", "UTF-8");
    }

    @RequestMapping("login")
    public ResultRo login(@RequestParam("name") String name, @RequestParam("password") String password,
                          @RequestParam("priUrl") String priUrl, HttpServletRequest request) {
        User user = userMapper.getUserByNameAndPassword(name, password);
        if (user != null) {
            String toUrl = priUrl;
            if (StringUtil.isEmpty(priUrl)) {
                toUrl = contextPath + "/";
            }

            request.getSession().setAttribute("user", user);
            return new ResultRo(200, "ok", toUrl);
        }

        return new ResultRo(400, "认证错误");
    }

}
