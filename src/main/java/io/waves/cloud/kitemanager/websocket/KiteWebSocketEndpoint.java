/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import io.waves.cloud.kitemanager.ro.ClientCmdResult;
import io.waves.cloud.kitemanager.util.ConstUtil;
import io.waves.cloud.kitemanager.util.RedisUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.waves.cloud.kitemanager.websocket.CmdTypes.res_pong;

/**
 * kite web socket endpoint and session holder
 * @author ytzhang0828@qq.com
 */
@ServerEndpoint("/ws")
@Component
public class KiteWebSocketEndpoint {

    private static Logger logger = LoggerFactory.getLogger(KiteWebSocketEndpoint.class);

    /**
     * 当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 在线记录 [clientId -> instance]
     */
    private static Map<String, KiteWebSocketEndpoint> endpointMap = new TreeMap<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     * <pre>java ee定义的接口标准</pre>
     */
    private Session session;

    private String clientId;
    private String clientIPv4;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        //判断id
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> clientIds = params.get("clientId");
        List<String> ipv4 = params.get("ipv4");
        if (clientIds == null || clientIds.isEmpty()) {
            Cmd cmd = new Cmd(CmdTypes.res_error, "客户端未提供id");
            try {
                session.getBasicRemote().sendText(cmd.toJSONString());
                session.close();
            } catch (IOException e) {
                logger.error("发送信息出错 {}", cmd, e);
            }
            return;
        }
        if (ipv4 == null || ipv4.isEmpty()) {
            Cmd cmd = new Cmd(CmdTypes.res_error, "客户端未提供ip");
            try {
                session.getBasicRemote().sendText(cmd.toJSONString());
                session.close();
            } catch (IOException e) {
                logger.error("发送信息出错 {}", cmd, e);
            }
            return;
        }

        //获取到clientId
        String clientId = clientIds.get(0);
        //获取到clientIPv4
        this.clientIPv4 = ipv4.get(0);

        synchronized (endpointMap) {
            //判断id是否有重复
            if (endpointMap.containsKey(clientId)) {
                Cmd cmd = new Cmd(CmdTypes.res_error, "客户端id重复");
                try {
                    session.getBasicRemote().sendText(cmd.toJSONString());
                    session.close();
                } catch (IOException e) {
                    logger.error("发送信息出错 {}", cmd, e);
                }
                return;
            }

            //加入维护map
            logger.info("接收一个连接 clientId: {}", clientId);
            this.clientId = clientId;
            endpointMap.put(clientId, this);
        }

        Cmd cmd = new Cmd(CmdTypes.res_ok, "连接manager成功");
        try {
            session.getBasicRemote().sendText(cmd.toJSONString());
        } catch (IOException e) {
            logger.error("发送信息出错 {}", cmd, e);
        }
    }

    @OnClose
    public void onClose() {

        synchronized (endpointMap) {
            endpointMap.remove(clientId);
        }

        logger.info("一条连接关闭 clientId: {}", clientId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.debug("接收到来自客户端的消息 clientId {}, 消息 {}", clientId, message.trim());
        try {
            CmdResult cmdResult = CmdResult.decodeJSONString(message);

            //todo 客户端主动信息目前仅这一种暂时不写分发器，后期写分发器模式
            String type = cmdResult.getType();
            if (!StringUtil.isEmpty(type) && type.equals("req_ping")) {
                Cmd cmd = new Cmd();
                Head head = new Head();
                head.setType(res_pong);
                cmd.setHead(head);
                cmd.setBody("pong");
                sendCmd(clientId, cmd);
            }

            //正常命令的返回信息
            else if (cmdResult.getAsync() == 0) { //同步命令
                CmdResultSyncer.setJobResult(cmdResult.getJobId(), cmdResult);
            }
            else { //异步命令
                //把结果放入缓存
                ClientCmdResult clientCmdResult = new ClientCmdResult(clientId, cmdResult);
                RedisUtil.getJsonCache(ConstUtil.kitemanager).lpush(cmdResult.getJobId(), clientCmdResult);
                //设置超时，多个客户端时，进行了重复设置
                RedisUtil.getJsonCache(ConstUtil.kitemanager).expire(cmdResult.getJobId(), ConstUtil.defaultCacheSecond);
            }
        } catch (Exception e) {
            logger.error("客户端消息格式出错，message: {}", message, e);
            return;
        }

    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误 clientId {}", clientId, error);
    }

    /**
     * 向客户端发送指令
     * @param clientId
     * @param cmd
     */
    public static boolean sendCmd(String clientId, Cmd cmd) {
        try {
            logger.debug("发送消息：clientid {}, cmd {}", clientId, cmd);
            synchronized (endpointMap) {
                if (!endpointMap.containsKey(clientId)) {
                    throw new RuntimeException("客户端连接不存在");
                }
                endpointMap.get(clientId).getSession().getBasicRemote().sendText(cmd.toJSONString());
            }
        } catch (IOException e) {
            logger.error("发送命令出错 clientId {}, cmd {}", clientId, cmd, e);
            return false;
        }

        return true;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static AtomicInteger getOnlineCount() {
        return onlineCount;
    }

    public static void setOnlineCount(AtomicInteger onlineCount) {
        KiteWebSocketEndpoint.onlineCount = onlineCount;
    }

    public String getClientIPv4() {
        return clientIPv4;
    }

    public void setClientIPv4(String clientIPv4) {
        this.clientIPv4 = clientIPv4;
    }

    public static Map<String, KiteWebSocketEndpoint> getEndpointMap() {
        return endpointMap;
    }

}
