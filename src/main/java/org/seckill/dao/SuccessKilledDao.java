package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

public interface SuccessKilledDao {
	
	//插入购买明细，过滤重复
	int insertSuccessKilled(@Param("seckillId")long seckillId,@Param("userPhone") long userPhone);

	//根据id查询SuccessKilled并携带回Seckill对象实体
	SuccessKilled queryByIdWithSeckill(@Param("seckillId")long seckillId,@Param("userPhone") long userPhone);
}
