/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.service;

import io.waves.cloud.kitemanager.websocket.KiteWebSocketEndpoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * cmd service
 * @author ytzhang0828@qq.com
 */
@Service
public class CmdService {

    /**
     * 获取符合clientId模式的客户端所有客户端id
     *
     * @param clientId 格式  xxx | *xxx | xxx*, 即只有完全匹配，前缀匹配，后缀匹配3种
     * @return 符合模式的客户端id集合
     */
    public List<String> getValidClientIds(String clientId) {
        List<String> clientIds = new ArrayList<>();

        if (!clientId.contains("*")) {
            clientIds.add(clientId);
            return clientIds;
        }

        Set<String> allClientIds = KiteWebSocketEndpoint.getEndpointMap().keySet();
        if (clientId.equals("*")) {
            clientIds.addAll(allClientIds);
        }
        else if (clientId.startsWith("*")) {
            String end = clientId.substring(clientId.indexOf(1));
            for (String id : allClientIds) {
                if (id.endsWith(end)) clientIds.add(id);
            }
        }
        else if (clientId.endsWith("*")) {
            String start = clientId.substring(0, clientId.length() - 1);
            for (String id : allClientIds) {
                if (id.startsWith(start)) clientIds.add(id);
            }
        }

        return clientIds;
    }

}
