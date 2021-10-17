package com.ccic.dadi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.ccic.dadi.utils.RedisLockUtils;
import com.ccic.dadi.utils.RedisUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LearnServiceTest {
	@Autowired
	private RedisUtils redisUtils;
	
	@Autowired
	private RedisLockUtils redisLockUtils;

	@Test
	public void getLearn1() {
		redisUtils.set("aa", "bb");
		String value = redisUtils.get("aa");
		System.out.println(value);
		
	}
	
	
	@Test
	public void getLearn2() throws Exception {
		String key = "TESTConstant&20210921" ;
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
			System.out.println("处理业务逻辑");
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
