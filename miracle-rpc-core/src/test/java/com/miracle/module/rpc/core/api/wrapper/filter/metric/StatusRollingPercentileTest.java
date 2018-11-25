package com.miracle.module.rpc.core.api.wrapper.filter.metric;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatusRollingPercentileTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		StatusRollingPercentile status = new StatusRollingPercentile(10000, 10, 2, true);
		status.addValue(1);
		status.addValue(2);
		status.addValue(3);
		status.addValue(4);
		status.addValue(5);
		status.addValue(6);
		status.addValue(5);
		status.addValue(4);
		status.addValue(3);
		status.addValue(2);
		status.addValue(1);
		Thread.sleep(1000);
		System.out.println(status.getPercentile(50));
		status.addValue(10);
		status.addValue(20);
		status.addValue(30);
		status.addValue(40);
		status.addValue(50);
		status.addValue(60);
		status.addValue(50);
		status.addValue(40);
		status.addValue(30);
		status.addValue(20);
		status.addValue(10);
		Thread.sleep(1000);
		System.out.println(status.getPercentile(50));
		status.addValue(100);
		status.addValue(200);
		status.addValue(300);
		status.addValue(400);
		status.addValue(500);
		status.addValue(600);
		status.addValue(500);
		status.addValue(400);
		status.addValue(300);
		status.addValue(200);
		status.addValue(100);
		Thread.sleep(1000);
		System.out.println(status.getPercentile(50));
		status.addValue(1000);
		status.addValue(2000);
		status.addValue(3000);
		status.addValue(4000);
		status.addValue(5000);
		status.addValue(6000);
		status.addValue(5000);
		status.addValue(4000);
		status.addValue(3000);
		status.addValue(2000);
		status.addValue(1000);
		Thread.sleep(1000);
		System.out.println(status.getPercentile(50));
	}

}
