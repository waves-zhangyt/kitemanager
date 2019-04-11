/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * agent更新服务
 * <pre>
 *     此服务可以单独部署，即视为另一个项目部署，从而不影响kitemanger本身的性能和带宽
 * </pre>
 * @author ytzhang0828@qq.com
 */
@Controller
@RequestMapping("update")
public class AgentUpdateController {

    /** 获取最新版agent */
    @RequestMapping(value = "latestAgent", method = RequestMethod.GET)
    public void latestAgent(HttpServletResponse response) {
        String version = latestAgentVersion();

        response.setContentType("application/octet-stream");
        FileInputStream fin = null;
        try {
            OutputStream os = response.getOutputStream();
            fin = new FileInputStream(new File("agent-productions/kiteagent-" + version));
            byte[] buf = new byte[1024];
            for (int n = fin.read(buf); n != -1; n = fin.read(buf)) {
                os.write(buf, 0, n);
            }
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fin != null) try { fin.close(); } catch (IOException e) { }
        }
    }

    /**
     * 过去最新版agent版本号
     * production name rule "kiteagent-v0.7.0"
     */
    @ResponseBody
    @RequestMapping(value = "latestAgentVersion", method = RequestMethod.GET)
    public String latestAgentVersion() {
        File prodDir = new File("agent-productions");
        String[] names = prodDir.list();

        if (names.length == 0) {
            return null;
        }

        return getMaxVersionName(names);
    }

    private String getMaxVersionName(String[] names) {
        List<String> sorter = new ArrayList<>();
        for (String name : names) {
            sorter.add(name.substring(name.indexOf("-v") + 2));
        }

        Collections.sort(sorter, (o1, o2) -> {
            String[] items1 = o1.split("\\.");
            String[] items2 = o2.split("\\.");
            for (int i= 0; i < items1.length; i++) {
                if (Integer.parseInt(items1[i]) > Integer.parseInt(items2[i])) {
                    return 1;
                }
                else if (Integer.parseInt(items1[i]) < Integer.parseInt(items2[i])) {
                    return -1;
                }
            }
            return 0;
        });

        return "v" + sorter.get(sorter.size() - 1);
    }

}
