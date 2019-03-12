/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.po.CmdParams;
import io.waves.cloud.kitemanager.ro.ClientCmdResult;
import io.waves.cloud.kitemanager.ro.ResultRo;
import io.waves.cloud.kitemanager.service.CmdService;
import io.waves.cloud.kitemanager.util.ConstUtil;
import io.waves.cloud.kitemanager.util.RedisUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import io.waves.cloud.kitemanager.websocket.CmdResult;
import io.waves.cloud.kitemanager.websocket.CmdResultSyncer;
import io.waves.cloud.kitemanager.websocket.KiteWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * 基础命令API接口控制器
 * @author ytzhang0828@qq.com
 */
@RestController
@RequestMapping("cmd")
public class CmdController {

    private static Logger logger = LoggerFactory.getLogger(CmdController.class);

    private Map<String, SynchronousQueue<CmdResult>> jobs = new ConcurrentHashMap<>();

    @Autowired
    private CmdService cmdService;

    /** 同步方式执行命令 */
    @RequestMapping(value = "sync", method = RequestMethod.POST)
    public ResultRo sync(@RequestBody CmdParams cmdParams) {

        return ResultRo.process(() -> {

            //jobId 是必须的
            String jobId = cmdParams.getCmd().getHead().getJobId();
            if (StringUtil.isEmpty(jobId)) {
                jobId = StringUtil.uuid();
                cmdParams.getCmd().getHead().setJobId(jobId);
            }
            //本接口对应的必是同步命令
            cmdParams.getCmd().getHead().setAsync(0);

            //客户端指定jobId的情况下，有重复，则提示报错
            if (CmdResultSyncer.exists(jobId)) {
                throw new RuntimeException("重复jobId");
            }

            try {
                //（1）新建一个job结果同步
                CmdResultSyncer.addJob(jobId);

                //clientId可以是带通配符（前缀、后缀、或全不匹配）
                List<ClientCmdResult> resultList = new ArrayList<>();
                String clientId = cmdParams.getClientId();
                List<String> validClientIds = cmdService.getValidClientIds(clientId);
                for (String cId : validClientIds) {
                    KiteWebSocketEndpoint.sendCmd(cId, cmdParams.getCmd());

                    CmdResult cmdResult = null;
                    Integer timeout = cmdParams.getCmd().getHead().getTimeout();
                    if (timeout == null) timeout = (int) CmdResultSyncer.DEFAULT_TIMEOUT; //和客户端默认超时时间一样
                    try {
                        //（2）获取同步结果；timeout + 3 客户端返回的超时结果优先显示
                        cmdResult = CmdResultSyncer.getJobResult(jobId, timeout + 3);
                    } catch (Exception e) {
                        logger.error("等待同步命令执行结果出现异常", e);
                    }

                    //超时获取不到结果对象 todo 可改进
                    if (cmdResult == null) {
                        throw new RuntimeException("命令超时");
                    }

                    ClientCmdResult clientCmdResult = new ClientCmdResult(cId, cmdResult);
                    resultList.add(clientCmdResult);
                }

                return resultList;
            } catch (Exception e) {
                String errMsg = "执行出现问题: " + e.getMessage();
                logger.error(errMsg);
                throw new RuntimeException(errMsg);
            } finally {
                //（3）释放同步应用基础资源
                CmdResultSyncer.clearJob(jobId);
            }

        });

    }

    /** 异步方式执行命令 */
    @RequestMapping(value = "async", method = RequestMethod.POST)
    public ResultRo async(@RequestBody CmdParams cmdParams) {

        return ResultRo.process(() -> {

            //jobId 是必须的
            String jobId = cmdParams.getCmd().getHead().getJobId();
            if (StringUtil.isEmpty(jobId)) {
                jobId = StringUtil.uuid();
                cmdParams.getCmd().getHead().setJobId(jobId);
            }
            //本接口对应的必是异步命令
            cmdParams.getCmd().getHead().setAsync(1);

            if (RedisUtil.getJsonCache(ConstUtil.kitemanager).exists(jobId)) {
                throw new RuntimeException("重复jobId");
            }

            try {
                //站位，标志该job确实执行过 (异步队列模式暂时用不到了 todo 以后可优化加入此功能)
                //RedisUtil.getJsonCache(ConstUtil.kitemanager).set(jobId, new HashMap<>());

                String clientId = cmdParams.getClientId();
                List<String> validClientIds = cmdService.getValidClientIds(clientId);
                List<Map<String, Object>> reList = new ArrayList<>();
                for (String cId : validClientIds) {
                    //发送命令执行
                    boolean send = KiteWebSocketEndpoint.sendCmd(cId, cmdParams.getCmd());
                    if (!send) {
                        logger.error("存在未发送成功的命令clientId {} cmd {}", cId, cmdParams.getCmd());
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("send", send);
                    map.put("jobId", jobId);
                    map.put("clientId", cId);
                    reList.add(map);
                }

                return reList;
            } catch (Exception e) {
                String errMsg = "执行出现问题 " + e.getMessage();
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg);
            } finally {
                //do nothing
            }

        });

    }

    /** 获取异步命令执行结果 */
    @RequestMapping(value = "asyncResult/{jobId}", method = RequestMethod.GET)
    public ResultRo asyncResult(@PathVariable("jobId") String jobId) {

        if (StringUtil.isEmpty(jobId)) {
            return ResultRo.error(403, "jobId不能为空");
        }
        if (!RedisUtil.getJsonCache(ConstUtil.kitemanager).exists(jobId)) {
            return ResultRo.error(403, "暂未有任务结果或不存在该任务或命令结果已经过期（一小时）");
        }

        return ResultRo.process(() -> {
            //每次都获取现有的全部结果，没有则显示为空列表
            List<ClientCmdResult> list = RedisUtil.getJsonCache(ConstUtil.kitemanager)
                    .lrange(jobId, 0, -1, ClientCmdResult.class);
            RedisUtil.getJsonCache(ConstUtil.kitemanager).expire(jobId, ConstUtil.readedCacheSecond);
            return list;
        });

    }


}
