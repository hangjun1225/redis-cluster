package com.ccic.dadi.utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * Redis工具类
 *
 * @author 张磊
 */
@Component
public class RedisUtils {
	/** 默认过期时长，单位：秒 */
	public final static long DEFAULT_EXPIRE = 60 * 60 * 24;
	/** 不设置过期时长 */
	public final static long NOT_EXPIRE = -1;

	private final static Gson gson = new Gson();

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ValueOperations<String, String> valueOperations;
	@Autowired
	private HashOperations<String, String, Object> hashOperations;
	@Autowired
	private ListOperations<String, Object> listOperations;
	@Autowired
	private SetOperations<String, Object> setOperations;
	@Autowired
	private ZSetOperations<String, Object> zSetOperations;

	/** 判断redis数据库是否有对应的key */
	public boolean exists(final String key) {
		return redisTemplate.getConnectionFactory().getConnection().exists(key.getBytes());
	}

	/**
	 * 哈希 添加
	 *
	 * @param key
	 * @param hashKey
	 * @param value
	 * @param expire
	 */
	public void hmSet(String key, String hashKey, Object value, long expire) {
		hashOperations.put(key, hashKey, toJson(value));
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
	}

	public Object hmGet(String key, String hashKey) {
		return hashOperations.get(key, hashKey);
	}

	public void hmSet(String key, Object hashKey, Object value, long expire) {
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		hash.put(key, hashKey, value);
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
	}

	/**
	 * 哈希获取数据
	 *
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public Object hmGet(String key, Object hashKey) {
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		return hash.get(key, hashKey);
	}
	// hash end

	public void set(String key, Object value, long expire) {
		valueOperations.set(key, toJson(value));
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
	}

	public void set(String key, Object value) {
		set(key, value, DEFAULT_EXPIRE);
	}

	public <T> T get(String key, Class<T> clazz, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
		return value == null ? null : fromJson(value, clazz);
	}

	public <T> T get(String key, Class<T> clazz) {
		return get(key, clazz, NOT_EXPIRE);
	}

	public String get(String key, long expire) {
		String value = valueOperations.get(key);
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
		return value;
	}

	public String get(String key) {
		return get(key, NOT_EXPIRE);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * Object转成JSON数据
	 */
	public String toJson(Object object) {
		if (object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double
				|| object instanceof Boolean || object instanceof String) {
			return String.valueOf(object);
		}
		return gson.toJson(object);
	}

	/**
	 * JSON数据，转成Object
	 */
	public <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	public <T> void setList(String key, String hashKey, List<T> list, long expire) {
		hashOperations.put(key, hashKey, ObjectTranscoder.serialize(list));
		if (expire != NOT_EXPIRE) {
			redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		}
	}

	/**
	 * 获取list
	 *
	 * @param <T>
	 * @param key
	 * @return list
	 */
	public <T> List<T> getList(String key, String hashKey) {
		// String bKey = buildKey(key);
		// if (getJedis() == null || !getJedis().exists(key.getBytes())) {
		// return null;
		// }
		// byte[] in = getJedis().get(key.getBytes());
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		byte[] in = (byte[]) hash.get(key, hashKey);
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) ObjectTranscoder.deserialize(in);
		return list;
	}

	/**
	 * 设置NX锁
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setNX(String key, String value) {
		return redisTemplate.getConnectionFactory().getConnection().setNX(key.getBytes(), value.getBytes());
	}

	public boolean setExpire(String key, long seconds) {
		return redisTemplate.getConnectionFactory().getConnection().expire(key.getBytes(), seconds);
	}

	/**
	 * incr自增
	 * 
	 * @param liveTime
	 * @return
	 */
	public Long incr(String key) {
		return incr(key, -1);
	}

	/**
	 * incr自增
	 * 
	 * @param key
	 * @param liveTime
	 * @return
	 */
	public Long incr(String key, long liveTime) {
		RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
		Long increment = entityIdCounter.getAndIncrement();
		if ((null == increment || increment.longValue() == 0) && liveTime > 0) {// 初始设置过期时间
			entityIdCounter.expire(liveTime, TimeUnit.SECONDS);
		}
		return increment;
	}
}
