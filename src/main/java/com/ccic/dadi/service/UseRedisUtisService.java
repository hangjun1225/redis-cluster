package com.ccic.dadi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ccic.dadi.utils.RedisUtils;

@Service
public class UseRedisUtisService {
	@Autowired
	private RedisUtils redisUtils;

	public void getLearn() {
		redisUtils.set("aa", "bb");
		String value = redisUtils.get("aa");
		System.out.println(value);
		
	}
}
