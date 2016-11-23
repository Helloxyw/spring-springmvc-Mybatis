package org.seckill.test;

import javax.annotation.Resource;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class TestSeckillDao {
	
	@Resource
	private SeckillDao seckillDao;

	public void testQueryById() throws Exception{
		long id = 1000;
		Seckill seckill = seckillDao.queryById(id);
		System.out.println(seckill.getName());
		System.out.println(seckill);
	}
	
	public static void main(String[] args) throws Exception {
		TestSeckillDao test = new TestSeckillDao();
		test.testQueryById();
	}
}
