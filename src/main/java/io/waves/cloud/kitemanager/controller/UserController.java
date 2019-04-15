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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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

    @RequestMapping(value = "login", method = RequestMethod.POST)
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

    @RequestMapping("logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        try {
            response.sendRedirect(contextPath + "/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("changePasswordPage")
    public String changePasswordPage(HttpServletRequest request, HttpServletResponse response) {
        User user = (User) request.getSession().getAttribute("user");

        response.setContentType("text/html;charset=UTF-8");
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("user", user);
        return VelocityUtil.merge(velocityContext, "io/waves/cloud/kitemanager/ro/vm/changePasswordPage.html", "UTF-8");
    }

    @RequestMapping(value = "changePassword", method = RequestMethod.POST)
    public ResultRo changePassword(@RequestParam("name") String name, @RequestParam("password") String password,
                                   @RequestParam("newPassword") String newPassword,
                                   @RequestParam("confirmNewPassword") String confirmNewPassword) {
        return ResultRo.process(() -> {
            User user = userMapper.getUserByNameAndPassword(name, password);
            if (user == null) {
                throw new RuntimeException("原用户名密码验证失败");
            }

            if (!newPassword.equals(confirmNewPassword)) {
                throw new RuntimeException("新密码两次输入的不一致");
            }

            user.setPassword(newPassword);
            int count = userMapper.updateUser(user);

            return "更新成功";
        });
    }
}
