package com.ccic.dadi.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 使用Redis进行分布式锁
 * 
 * @author lsj
 * @date 2021/7/23 16:07
 */

@Component
public class RedisLockUtils {
	private static final Logger LOG = LoggerFactory.getLogger(RedisLockUtils.class);

	@Autowired
	private RedisTemplate<String, String> stringRedisTemplate;

	/**
	 * Redis加锁的操作
	 *
	 * @param key
	 * @param value
	 *            当前时间撮作为value
	 * @return
	 */
	public Boolean tryLock(String key, String value) {
		if (stringRedisTemplate.opsForValue().setIfAbsent(key, value)) {
			LOG.info("【redis分布式锁】获取成功!");
			return true;
		}
		// 如果获取锁的客户端端执行时间过长，进程被kill掉，或者因为其他异常崩溃，导致无法释放锁，就会造成死锁。
		// 所以，需要对加锁要做时效性检测。因此，我们在加锁时，把当前时间戳作为value存入此锁中，
		// 通过当前时间戳和Redis中的时间戳进行对比，如果超过一定差值，认为锁已经时效，防止锁无限期的锁下去
		String currentValue = stringRedisTemplate.opsForValue().get(key);
		// 当时间相差1秒表示锁已经时效
		if (StringUtils.isNotEmpty(currentValue) && (Long.valueOf(value) - Long.valueOf(currentValue)) > 1000) {
			LOG.info("【redis分布式锁】由于外部原因死锁!");
			// 设置并获取就值
			String oldValue = stringRedisTemplate.opsForValue().getAndSet(key, value);
			// 如果oldValue=currentValue 说明中间没有其他客户端竞争,则 否则表示已经被其他客户端抢走锁
			if (StringUtils.isNotEmpty(oldValue) && oldValue.equals(currentValue)) {
				LOG.info("【redis分布式锁】解除死锁成功并获取到锁!");
				return true;
			}
		}
		LOG.info("【redis分布式锁】获取锁失败!已被其它客户端占用!");
		return false;
	}

	/**
	 * Redis解锁的操作
	 *
	 * @param key
	 * @param value
	 */
	public void unlock(String key, String value) throws Exception {
		String currentValue = stringRedisTemplate.opsForValue().get(key);
		try {
			if (StringUtils.isNotEmpty(currentValue) && currentValue.equals(value)) {
				stringRedisTemplate.opsForValue().getOperations().delete(key);
				LOG.info("【redis分布式锁】解锁成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("【redis分布式锁】 解锁失败!");
			throw new Exception("【redis分布式锁】 解锁失败!");
		}
	}
}