/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.websocket;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * kite agent transaction counter
 * @author ytzhang0828@qq.com
 */
public class KiteAgentTransactionCounter {

    private static Map<String, Recorder> agentsTransactionMap = new ConcurrentHashMap<>();

    private static long time1 = 1 * 60 * 1000L;
    private static long time5 = 5 * 60 * 1000L;
    private static long time15 = 15 * 60 * 1000L;

    public static class Recorder {
        private String agentId;

        private long time1Start = System.currentTimeMillis();
        private long time5Start = System.currentTimeMillis();
        private long time15Start = System.currentTimeMillis();

        private int time1Count = 0;
        private int time5Count = 0;
        private int time15Count = 0;

        private int time1CountPri = 0;
        private int time5CountPri = 0;
        private int time15CountPri = 0;

        public Recorder(String agentId) {
            this.agentId = agentId;
        }

        public synchronized void increment() {
            long now = System.currentTimeMillis();
            if (now - time1Start < time1) {
                time1Count++;
            } else {
                time1CountPri = time1Count;
                time1Count = 1;
                time1Start = now;
            }

            if (now - time5Start < time5) {
                time5Count++;
            } else {
                time5CountPri = time5Count;
                time5Count = 1;
                time5Count = 1;
                time5Start = now;
            }

            if (now - time15Start < time15) {
                time15Count++;
            } else {
                time5CountPri = time5Count;
                time5Count = 1;
                time15Count = 1;
                time15Start = now;
            }
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public int getTime1Count() {
            long now = System.currentTimeMillis();
            if (now - time1Start > time1) {
                time1Start = now;
                time1CountPri = time1Count;
                time1Count = 0;
            }

            return time1Count;
        }

        public void setTime1Count(int time1Count) {
            this.time1Count = time1Count;
        }

        public int getTime5Count() {
            long now = System.currentTimeMillis();
            if (now - time5Start > time5) {
                time5Start = now;
                time5CountPri = time5Count;
                time5Count = 0;
            }

            return time5Count;
        }

        public void setTime5Count(int time5Count) {
            this.time5Count = time5Count;
        }

        public int getTime15Count() {
            long now = System.currentTimeMillis();
            if (now - time15Start > time15) {
                time15Start = now;
                time15CountPri = time15Count;
                time15Count = 0;
            }

            return time15Count;
        }

        public void setTime15Count(int time15Count) {
            this.time15Count = time15Count;
        }

        public int getTime1CountPri() {
            return time1CountPri;
        }

        public void setTime1CountPri(int time1CountPri) {
            this.time1CountPri = time1CountPri;
        }

        public int getTime5CountPri() {
            return time5CountPri;
        }

        public void setTime5CountPri(int time5CountPri) {
            this.time5CountPri = time5CountPri;
        }

        public int getTime15CountPri() {
            return time15CountPri;
        }

        public void setTime15CountPri(int time15CountPri) {
            this.time15CountPri = time15CountPri;
        }
    }

    public static void addRecorder(String agentId) {
        synchronized (agentsTransactionMap) {
            agentsTransactionMap.put(agentId, new Recorder(agentId));
        }
    }

    public static void increment(String agentId) {
        //mem protected
        if (!KiteWebSocketEndpoint.getEndpointMap().containsKey(agentId)) {
            removeRecorder(agentId);
            return;
        }

        Recorder recorder;
        synchronized (agentsTransactionMap) {
            recorder = agentsTransactionMap.get(agentId);
            if (recorder == null) {
                recorder = new Recorder(agentId);
                agentsTransactionMap.put(agentId, recorder);
            }
        }

        recorder.increment();
    }

    public static void removeRecorder(String agentId) {
        synchronized (agentsTransactionMap) {
            agentsTransactionMap.remove(agentId);
        }
    }

    public static Recorder getRecorder(String agentId) {
        Recorder recorder = agentsTransactionMap.get(agentId);
        if (recorder == null) {
            // make one new, but don't put it in agentsTransactionMap
            recorder = new Recorder(agentId);
        }

        return recorder;
    }

}
