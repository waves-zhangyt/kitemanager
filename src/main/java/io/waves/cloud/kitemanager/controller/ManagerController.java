/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.conf.KiteManangerProperties;
import io.waves.cloud.kitemanager.ro.ResultRo;
import io.waves.cloud.kitemanager.util.StringUtil;
import io.waves.cloud.kitemanager.util.VelocityUtil;
import io.waves.cloud.kitemanager.websocket.KiteWebSocketEndpoint;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理入口
 * @author ytzhang0828@qq.com
 */
@RestController
public class ManagerController {

    @Autowired
    private KiteManangerProperties kiteManangerProperties;

    /** 主页，进入agent列表 */
    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("text/html;charset=UTF-8");

        //获取现有连接列表
        Map<String, KiteWebSocketEndpoint> treeMap = KiteWebSocketEndpoint.getEndpointMap();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("agentList", treeMap);
        velocityContext.put("user", request.getSession().getAttribute("user"));
        return VelocityUtil.merge(velocityContext, "io/waves/cloud/kitemanager/ro/vm/index.html", "UTF-8");

    }

    /** 根据text内容过滤列表 */
    @RequestMapping(value = "filterAgent", method = RequestMethod.POST)
    public ResultRo filterAgent(@RequestParam("text") String text) {
        String lowerText = text.toLowerCase();

        return ResultRo.process(() -> {
            Map<String, KiteWebSocketEndpoint> treeMap = KiteWebSocketEndpoint.getEndpointMap();
            List<String[]> reList = new ArrayList<>();
            for (Map.Entry<String, KiteWebSocketEndpoint> entry : treeMap.entrySet()) {
                String key = entry.getKey();
                String clientIPv4 = entry.getValue().getClientIPv4();

                //输入为空时，不用过滤
                if (StringUtil.isEmpty(text)) {
                    String[] items = new String[] { entry.getValue().getClientId(), entry.getValue().getClientIPv4() };
                    reList.add(items);
                }
                //有输入时，需要过滤; 过滤不区分大小写
                else if (key.toLowerCase().contains(lowerText) || clientIPv4.toLowerCase().contains(lowerText)) {
                    //[ clientIp, clientIPv4 ]
                    String[] items = new String[] { entry.getValue().getClientId(), entry.getValue().getClientIPv4() };
                    reList.add(items);
                }
            }
            return reList;
        });
    }

    /** 进入命令执行页 */
    @RequestMapping(value = "execPage", method = RequestMethod.GET)
    public String execPage(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(value = "clientId", required = false) String clientId) {
        response.setContentType("text/html;charset=UTF-8");

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("clientId", clientId);
        velocityContext.put("user", request.getSession().getAttribute("user"));
        return VelocityUtil.merge(velocityContext, "io/waves/cloud/kitemanager/ro/vm/exec.html", "UTF-8");
    }

    /** get the max client number (it's a threshold, no more client can join when the client conneciton more than it) */
    @RequestMapping("maxClientNumber")
    public int maxClientNumber() {
        return kiteManangerProperties.getMaxClientNumber();
    }

}
