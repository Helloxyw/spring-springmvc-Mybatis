package org.seckill.service.impl;

import java.util.Date;
import java.util.List;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//注入Service依赖
	@Autowired
	private SeckillDao seckillDao;
	@Autowired
	private SuccessKilledDao successKilledDao;

	// MD5盐值字符串，用于混淆MD5
	private final String slat = "alsklaskl1092787*&*lasmal;LKAJSLDKJA";

	public List<Seckill> getSeckillList() {
		// TODO Auto-generated method stub
		return seckillDao.queryAll(0, 4);
	}

	public Seckill getById(long seckillId) {
		// TODO Auto-generated method stub
		return seckillDao.queryById(seckillId);
	}

	public Exposer exportSeckillUrl(long seckillId) {
		// TODO Auto-generated method stub

		Seckill seckill = seckillDao.queryById(seckillId);
		if (seckill == null) {
			return new Exposer(false, seckillId);
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime()
				|| nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(),
					startTime.getTime(), endTime.getTime());
		}
		String md5 = getMD5(seckillId); // TODO
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Transactional
	/*
	 * 使用注解控制事务方法的优点：
	 * 1.开发团队达成一致的约定，明确标注事务方法的编程风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部
	 * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
	 * (non-Javadoc)
	 * @see org.seckill.service.SeckillService#executeSeckill(long, long, java.lang.String)
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone,
			String md5) throws SeckillException, SeckillCloseException,
			RepeatKillException {
		// TODO Auto-generated method stub
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill date rewrite");
		}

		// 执行秒杀逻辑：减库存+记录购买行为
		Date nowTime = new Date();

		// 减库存
		try {
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount <= 0) {
				// 没有更新到记录，秒杀结束
				throw new SeckillCloseException("seckill is close");
			} else {
				// 记录购买行为
				int insertCount = successKilledDao.insertSuccessKilled(
						seckillId, userPhone);
				// 唯一 ：seckillId ,userPhone
				if (insertCount <= 0) {
					// 重复秒杀
					throw new RepeatKillException("seckill repeated");
				} else {
					// 秒杀成功
					SuccessKilled successKilled = successKilledDao
							.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,
							successKilled);

				}
			}
		} catch (SeckillCloseException e1) {
			throw e1;
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
			// 所有编译期异常转化成运行期异常
			throw new SeckillException("seckill inner error" + e.getMessage());

		}
	}

}
