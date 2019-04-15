package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.db.User;
import io.waves.cloud.kitemanager.db.UserMapper;
import io.waves.cloud.kitemanager.ro.ResultRo;
import io.waves.cloud.kitemanager.util.StringUtil;
import io.waves.cloud.kitemanager.util.VelocityUtil;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * the endpoint for admin role
 */
@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("userManagerPage")
    public String userManagerPage(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");

        List<User> userList = userMapper.getUsers();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("user", request.getSession().getAttribute("user"));
        velocityContext.put("userList", userList);
        return VelocityUtil.merge(velocityContext, "io/waves/cloud/kitemanager/ro/vm/userManagerPage.html", "UTF-8");
    }

    @RequestMapping(value = "addUser", method = RequestMethod.POST)
    public ResultRo addUser(@RequestParam("name") String name, @RequestParam("password") String password,
                            @RequestParam("username") String username, @RequestParam("role") String role) {

        if (StringUtil.isEmpty(name) || StringUtil.isEmpty(password) || StringUtil.isEmpty(username)
                || StringUtil.isEmpty(role)) {
            return new ResultRo(400, "登录名，密码，昵称，角色均不能为空");
        }

        return ResultRo.process(() -> {
            User user = userMapper.getUserByName(name);
            if (user != null) {
                throw new RuntimeException("用户名已存在");
            }

            user = new User();
            user.setName(name);
            user.setPassword(password);
            user.setUsername(username);
            user.setRole(role);
            user.setCreateTime(new Date());
            int count = userMapper.insertUser(user);
            if (count != 1) {
                throw new RuntimeException("插入失败");
            }

            return "添加成功";
        });

    }

    @RequestMapping(value = "delUser", method = RequestMethod.POST)
    public ResultRo delUser(@RequestParam("id") int id) {
        return ResultRo.process(() -> {
            int count = userMapper.deleteUserById(id);
            if (count == 1) {
                return "删除用户成功";
            } else {
                throw new RuntimeException("删除用户失败");
            }
        });
    }

}
