/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import io.waves.cloud.kitemanager.util.ConstUtil;
import io.waves.cloud.kitemanager.util.JSONUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * kite agent connection health checker
 * @author ytzhang0828@qq.com
 */
@Component
public class KiteAgentConnectionChecker {

    private static Logger logger = LoggerFactory.getLogger(KiteAgentConnectionChecker.class);

    /**
     * loop all kiteagent and send the simple echo command to check
     */
    @PostConstruct
    public void check() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                logger.info("start the conn health check form kite manager");
                long interval = 0;
                while (true) {
                    try {
                        long sleepTime = 180000L - interval;
                        Thread.sleep(sleepTime < 0 ? 0 : sleepTime);
                    } catch (InterruptedException e) {
                        logger.error("thread sleep error", e);
                    }

                    long start = System.currentTimeMillis();
                    try {
                        Map<String, KiteWebSocketEndpoint> endpointMap = KiteWebSocketEndpoint.getEndpointMap();
                        Set<Map.Entry<String, KiteWebSocketEndpoint>> agents;
                        synchronized (endpointMap) {
                            agents = endpointMap.entrySet();
                        }
                        for (Map.Entry<String, KiteWebSocketEndpoint> agent : agents) {
                            checkAgent(agent);
                        }
                    } catch (Exception e) {
                        logger.error("check conn error", e);
                    }
                    interval = System.currentTimeMillis() - start;
                }
            }
        };

        thread.setName("agent conn checker thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void checkAgent(Map.Entry<String, KiteWebSocketEndpoint> agent) {
        String jobId = StringUtil.uuid();
        try {
            Map<String, Object> cmdBody = new LinkedHashMap<>();
            cmdBody.put("method", "GET");
            // note, if agent http server port changed, the health check is also ok
            // (only the cmd result get the failed msg).
            cmdBody.put("url", "http://127.0.0.1:19988/version");
            cmdBody.put("headers", null);
            cmdBody.put("body", null);
            cmdBody.put("bodyType", null);

            Cmd cmd = new Cmd(ConstUtil.proxyHttp, jobId, JSONUtil.encodeJSONString(cmdBody));
            int timeout = 3;
            cmd.getHead().setTimeout(timeout);

            CmdResultSyncer.addJob(jobId);
            KiteWebSocketEndpoint.sendCmd(agent.getKey(), cmd, false);
            CmdResult cmdResult = CmdResultSyncer.getJobResult(jobId, timeout + 2);
            String stdout = cmdResult.getStdout();
            if (!StringUtil.isEmpty(stdout)) {
                stdout = stdout.trim();
            }
            logger.debug("conn check back, agentId: {}, stdout: {} ", agent.getKey(), stdout);
        } catch (Exception e) {
            logger.error("conn check command exception, agentId: {}", agent.getKey(), e);
        } finally {
            CmdResultSyncer.clearJob(jobId);
        }
    }

}
