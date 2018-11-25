package org.kkrpc.example.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.miracle.module.rpc.core.api.RpcContext;
import com.miracle.module.rpc.starter.Main;
import org.kkrpc.example.Calculator;



public class ClientSpringDemo {
	public static void main(String args[]) throws InterruptedException, ExecutionException
	{
		Main.startClient("client-spring-config.xml");
		
		CalcDao dao = (CalcDao) Main.getBean("mydao");
		dao.doJob();
		
		Calculator calculator = (Calculator) Main.getBean("myCalc");
		
		List<String> oneList = new ArrayList<String>();
		oneList.add("aaa");
		oneList.add("bbbb");
		List<String> anotherList = new ArrayList<String>();
		oneList.add("ccccc");
		System.out.println("Combined list size is: " + calculator.getCombinedListSize(oneList, anotherList));
		
		RpcContext.getContext().asyncBegin();
		calculator.add(5, 5);
		Future<Integer> sumFuture = RpcContext.getContext().getFuture();
		calculator.add(12, 23);
		Future<Integer> sum2Future = RpcContext.getContext().getFuture();
		System.out.println("sum is " + sumFuture.get());
		System.out.println("sum2 is " + sum2Future.get());
		RpcContext.getContext().asyncEnd();
		
		Main.holdTheWorld();
	}
}
