/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 命令结果同步器
 * @author ytzhang0828@qq.com
 */
public class CmdResultSyncer {

    private static Logger logger = LoggerFactory.getLogger(CmdResultSyncer.class);

    private static Map<String, SynchronousQueue<CmdResult>> jobs = new ConcurrentHashMap<>();

    /** 默认超时时间 （单位:秒） */
    public final static long DEFAULT_TIMEOUT = 60L;

    public static void addJob(String jobId) {
        SynchronousQueue<CmdResult> synchronousQueue = new SynchronousQueue<>();
        jobs.put(jobId, synchronousQueue);
    }

    public static CmdResult getJobResult(String jobId, long timeout) {
        SynchronousQueue<CmdResult> synchronousQueue = jobs.get(jobId);
        if (synchronousQueue == null) return null;


        try {
            return synchronousQueue.poll(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearJob(String jobId) {
        SynchronousQueue<CmdResult> synchronousQueue = jobs.get(jobId);
        if (synchronousQueue == null) return;

        jobs.remove(jobId);
    }

    public static void setJobResult(String jobId, CmdResult cmdResult) {
        SynchronousQueue<CmdResult> synchronousQueue = jobs.get(jobId);
        if (synchronousQueue == null) {
            logger.error("jobId {} 不存在, 忽略结果", jobId, cmdResult);
            return;
        }

        synchronousQueue.offer(cmdResult);
    }

    public static boolean exists(String jobId) {
        return jobs.containsKey(jobId);
    }

}
