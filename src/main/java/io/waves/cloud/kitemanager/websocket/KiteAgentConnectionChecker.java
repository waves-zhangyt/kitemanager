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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * kite agent connection health checker
 * @author ytzhang0828@qq.com
 */
@Component
public class KiteAgentConnectionChecker {

    private static Logger logger = LoggerFactory.getLogger(KiteAgentConnectionChecker.class);

    private static long tickTime = 180000L;

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
                        long sleepTime = tickTime - interval;
                        Thread.sleep(sleepTime < 0 ? 0 : sleepTime);
                    } catch (InterruptedException e) {
                        logger.error("thread sleep error", e);
                    }

                    long start = System.currentTimeMillis();
                    try {
                        Map<String, KiteWebSocketEndpoint> endpointMap = KiteWebSocketEndpoint.getEndpointMap();
                        for (Iterator<Map.Entry<String, KiteWebSocketEndpoint>> iterator = endpointMap.entrySet().iterator();
                             iterator.hasNext();) {
                            checkAgent(iterator.next());
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
        KiteWebSocketEndpoint endpoint = agent.getValue();
        if (!endpoint.getSession().isOpen()) {
            logger.info("conn check abort for agentId: {}, as session closed", agent.getKey());
            return;
        }

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
            int timeout = 5;
            cmd.getHead().setTimeout(timeout);

            CmdResultSyncer.addJob(jobId);
            KiteWebSocketEndpoint.sendCmd(agent.getKey(), cmd, false);
            CmdResult cmdResult = CmdResultSyncer.getJobResult(jobId, timeout + 1);
            if (cmdResult == null) {
                logger.warn("conn check command timeout, agentId: {}", agent.getKey());
                return;
            }
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
