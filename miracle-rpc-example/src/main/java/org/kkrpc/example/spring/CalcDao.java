package org.kkrpc.example.spring;

import org.kkrpc.example.Calculator;

public class CalcDao {
	private Calculator tool;

	public void setTool(Calculator tool) {
		this.tool = tool;
	}
	
	public void doJob()
	{
		System.out.println("Do job with tool begin:");
		System.out.println("Got result: " + tool.add(100, 200));
		System.out.println("Do job with tool end:");
	}
}
