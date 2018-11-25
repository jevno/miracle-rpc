package com.miracle.module.rpc.core.api.wrapper.filter.metric;

import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatusRollingNumberTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		StatusRollingNumber mystats = new StatusRollingNumber(6000, 10);
		for(int i=0; i<1000; i++)
		{
			mystats.add(StatusRollingNumberEvent.SUCCESS, new Random().nextInt(10));
			Thread.sleep(new Random().nextInt(80));
			
			long[] vals = mystats.getValues(StatusRollingNumberEvent.SUCCESS);
			if(vals != null)
			{
				System.out.println(Arrays.toString(vals));
				System.out.println(mystats.getValueOfLatestBucket(StatusRollingNumberEvent.SUCCESS));
				System.out.println(mystats.getRollingSum(StatusRollingNumberEvent.SUCCESS));
				System.out.println(mystats.getRollingMaxValue(StatusRollingNumberEvent.SUCCESS));
				System.out.println(mystats.getCumulativeSum(StatusRollingNumberEvent.SUCCESS));
			}
		}
	}
	
	@Test
	public void test2() throws InterruptedException {
		StatusRollingNumber mystats = new StatusRollingNumber(6000, 10);

		mystats.add(StatusRollingNumberEvent.SUCCESS, 1);
		mystats.add(StatusRollingNumberEvent.SUCCESS, 1);
		mystats.add(StatusRollingNumberEvent.SUCCESS, 1);
		Thread.sleep(new Random().nextInt(60));
		System.out.println(mystats.getValueOfLatestBucket(StatusRollingNumberEvent.SUCCESS));
		mystats.add(StatusRollingNumberEvent.SUCCESS, -1);
		mystats.add(StatusRollingNumberEvent.SUCCESS, -1);
		mystats.add(StatusRollingNumberEvent.SUCCESS, 1);
		System.out.println(mystats.getValueOfLatestBucket(StatusRollingNumberEvent.SUCCESS));
		mystats.add(StatusRollingNumberEvent.SUCCESS, -1);
		Thread.sleep(new Random().nextInt(60));
		System.out.println(mystats.getValueOfLatestBucket(StatusRollingNumberEvent.SUCCESS));
	}

}
