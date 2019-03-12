/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.util;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  redis util 配合 redis.txt使用
 * @author ytzhang0828@qq.com
 */
public class RedisUtil {

    /**
     * 个业务连接池存储器
     */
    private static Map<String, ShardedJedisPool> jedisPoolMap = new ConcurrentHashMap<String, ShardedJedisPool>();
    /**
     * 加载配置
     */
    private static Properties prop = new Properties();

    static {
        InputStream inputStream = RedisUtil.class.getClassLoader()
                .getResourceAsStream("redis.txt");
        try {
            prop.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 获取指定业务的连接池
     *
     * @param domain 指定业务
     * @return
     */
    public static ShardedJedisPool getJedisPool(String domain) {

        //存在直接返回
        ShardedJedisPool jedisPool = jedisPoolMap.get(domain);
        if (jedisPool != null) {
            return jedisPool;
        }

        //否则创建
        synchronized (jedisPoolMap) {
            jedisPool = jedisPoolMap.get(domain);
            if (jedisPool != null) {
                return jedisPool;
            }

            //池特性
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setTestWhileIdle(true);
            jedisPoolConfig.setMaxTotal(Integer.parseInt(prop.getProperty(domain + ".maxTotal")));
            jedisPoolConfig.setMaxIdle(Integer.parseInt(prop.getProperty(domain + ".maxIdle")));
            jedisPoolConfig.setMinIdle(Integer.parseInt(prop.getProperty(domain + ".minIdle")));

            //连接特性
            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            JedisShardInfo si = new JedisShardInfo(
                    prop.getProperty(domain + ".host"),
                    Integer.parseInt(prop.getProperty(domain + ".port")),
                    Integer.parseInt(prop.getProperty(domain + ".timeoutMill")));
            //判断是否设置了密码
            String password = prop.getProperty(domain + ".password");
            if (password != null) {
                si.setPassword(password);
            }
            shards.add(si);
            jedisPool = new ShardedJedisPool(new JedisPoolConfig(), shards);

            jedisPoolMap.put(domain, jedisPool);

            return jedisPool;
        }

    }

    //=== 通用组 ===

    /**
     * 获取值
     *
     * @param domain 业务redis名称
     * @param key    键
     */
    public static String get(String domain, String key) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.get(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * set 值
     * <p>
     * 注意没有设置过期时间
     *
     * @param domain 业务redis名称
     * @param key    键
     * @param value  值
     */
    public static String set(String domain, String key, String value) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.set(key, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 设置 值
     * <p>
     * 设置过期时间
     *
     * @param domain        业务redis名称
     * @param key           键
     * @param value         值
     * @param expireSeconds 到期时间
     */
    public static String setex(String domain, String key, String value, int expireSeconds) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.setex(key, expireSeconds, value);
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除 值
     * <p>
     * 设置过期时间
     *
     * @param domain 业务redis名称
     * @param key    键
     */
    public static Long del(String domain, String key) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.del(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 判断是否存在
     * <p>
     * 设置过期时间
     *
     * @param domain 业务redis名称
     * @param key    键
     */
    public static boolean exists(String domain, String key) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.exists(key);
        } finally {
            jedis.close();
        }
    }

    /**
     * 设置过期时间
     *
     * @param domain 业务redis名称
     * @param key    键
     */
    public static Long expire(String domain, String key, int expiredSeconds) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return jedis.expire(key, expiredSeconds);
        } finally {
            jedis.close();
        }
    }

    //======  自动序列化方式  ==

    /**
     * 获取值
     *
     * @param domain 业务redis名称
     * @param key    键
     */
    public static <T> T get(String domain, String key, Class<T> clazz) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        ObjectInputStream objectInputStream = null;
        try {
            byte[] data = jedis.get(key.getBytes("UTF-8"));
            if (data == null) {
                return null;
            }
            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            jedis.close();
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * set 值
     * <p>
     * 注意没有设置过期时间
     *
     * @param domain 业务redis名称
     * @param key    键
     * @param value  值 必须是可序列化的
     */
    public static String set(String domain, String key, Serializable value) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            return jedis.set(key.getBytes("UTF-8"), byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            jedis.close();
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * set 值
     * <p>
     * 注意没有设置过期时间
     *
     * @param domain         业务redis名称
     * @param key            键
     * @param value          值 必须是可序列化的
     * @param expiredSeconds 到期时间
     */
    public static String setex(String domain, String key, Serializable value, int expiredSeconds) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            return jedis.setex(key.getBytes("UTF-8"), expiredSeconds, byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            jedis.close();
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 不支持的命令实现的接口
     */
    public static interface Command {
        public Object run(ShardedJedis jedis);
    }
    /**
     * 通用命令
     * <pre>
     *     此工具类不支持的其它快捷命令,通过lambda表达块实现
     * </pre>
     * @param domain 业务redis名称
     * @param command 实现命令接口的对象
     * @return Object
     */
    public static Object execute(String domain, Command command) {
        ShardedJedis jedis = getJedisPool(domain).getResource();
        try {
            return command.run(jedis);
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取json格式序列化缓存适配器
     * <pre>
     * 和 JsonCache.getEnv(clusterName)起同样效果
     * </pre>
     */
    public static JsonCache getJsonCache(String domain) {
        return new JsonCache(domain);
    }

    /** json 序列化缓存适配器 */
    public static class JsonCache {

        /** 默认缓存时间 1 小时 */
        private static int defaultInterval = 60 * 60;

        String domain;

        JsonCache(String domain) {
            this.domain = domain;
        }

        public String set(String key, Object obj) {
            return set(key, obj, defaultInterval);
        }

        public String set(String key, Object obj, int interval) {
            return (String) execute(domain, (shardedJedis) -> {
                return shardedJedis.setex(key, interval, JSONUtil.encodeJSONString(obj));
            });
        }
        public <T> T get(String key, Class<T> clazz) {
            return (T) execute(domain, (shardedJedis) -> {
                String val = shardedJedis.get(key);
                return JSONUtil.decodeJSONString(val, clazz);
            });

        }

        public Long del(String key) {
            return (Long) execute(domain, (shardedJedis) -> {
                return shardedJedis.del(key);
            });
        }

        public Boolean exists(String key) {
            return (Boolean) execute(domain, (shardedJedis) -> {
                return shardedJedis.exists(key);
            });
        }

        public Long expire(String key, int second) {
            return (Long) execute(domain, (shardedJedis) -> {
                return shardedJedis.expire(key, second);
            });
        }

        public Long lpush(String key, Object obj) {
            return (Long) execute(domain, (shardedJedis) -> {
                return shardedJedis.lpush(key, JSONUtil.encodeJSONString(obj));
            });
        }

        public <T> T rpop(String key, Class<T> clazz) {
            return (T) execute(domain, (shardedJedis) -> {
                String val = shardedJedis.rpop(key);
                return JSONUtil.decodeJSONString(val, clazz);
            });
        }

        public Long llen(String key) {
            return (Long) execute(domain, (shardedJedis) -> {
                return shardedJedis.llen(key);
            });
        }

        public <T> List<T> lrange(String key, long start, long stop, Class<T> clazz) {
            return (List<T>) execute(domain, (shardedJedis) -> {
                List<String> list = shardedJedis.lrange(key, start, stop);
                List<T> reList = new ArrayList<>();
                for (String text : list) {
                    reList.add(JSONUtil.decodeJSONString(text, clazz));
                }
                return reList;
            });
        }

    }

}
