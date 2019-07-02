/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import io.waves.cloud.kitemanager.util.ConstUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
                        long sleepTime = 60000L - interval;
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
            Cmd cmd = new Cmd(ConstUtil.cmdRun, jobId, "echo \"conn check\"");
            int timeout = 3;
            cmd.getHead().setTimeout(timeout);

            CmdResultSyncer.addJob(jobId);
            KiteWebSocketEndpoint.sendCmd(agent.getKey(), cmd);
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
