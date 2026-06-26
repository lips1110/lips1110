package org.example.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalCacheUtil {
    
    private static final Map<String, CacheObject> CACHE = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();
    
    static {
        CLEANER.scheduleAtFixedRate(LocalCacheUtil::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    private LocalCacheUtil() {
    }
    
    /**
     * 永久缓存
     */
    public static void put(String key, Object value) {
        CACHE.put(key, new CacheObject(value, -1));
    }
    
    /**
     * 指定过期时间（秒）
     */
    public static void put(String key, Object value, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + expireSeconds * 1000;
        CACHE.put(key, new CacheObject(value, expireTime));
    }
    
    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        CacheObject obj = CACHE.get(key);
        
        if (obj == null) {
            return null;
        }
        
        if (obj.isExpired()) {
            CACHE.remove(key);
            return null;
        }
        
        return (T) obj.getValue();
    }
    
    /**
     * 删除缓存
     */
    public static void remove(String key) {
        CACHE.remove(key);
    }
    
    /**
     * 是否存在
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }
    
    /**
     * 缓存数量
     */
    public static int size() {
        return CACHE.size();
    }
    
    /**
     * 清空缓存
     */
    public static void clear() {
        CACHE.clear();
    }
    
    /**
     * 清理过期缓存
     */
    private static void cleanExpired() {
        CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 缓存对象
     */
    private static class CacheObject {
        private final Object value;
        /**
         * -1 永不过期
         */
        private final long expireTime;
        
        public CacheObject(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return expireTime != -1 && System.currentTimeMillis() > expireTime;
        }
    }
}
