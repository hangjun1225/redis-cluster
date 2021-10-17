package com.ccic.dadi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ccic.dadi.utils.RedisLockUtils;

@Service
public class UseRedisLockUtisService {
	@Autowired
	private RedisLockUtils redisLockUtils;

	/**
	 * 修改统计信息
	 * 
	 * @throws Exception
	 */
	public void updateScoStatustical(String drugScoId, String drugName, String drugType, String rewProfInfoId)
			throws Exception {
		String key = "TESTConstant&" + drugScoId;
		long time = System.currentTimeMillis();
		try {
			// 如果加锁失败
			if (!redisLockUtils.tryLock(key, String.valueOf(time))) {
				// throw new BusinessException("网络信号差,请重新尝试");
				throw new Exception("网络信号差,请重新尝试");
			}
			// 业务逻辑：
			// 对药品表统计信息
			// updateDrugScoStatistical(drugScoId, drugName, drugType);
			// 修改专家表统计信息
			// updateRewProfScoStatistical(rewProfInfoId, drugType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			// 解锁
			try {
				redisLockUtils.unlock(key, String.valueOf(time));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
