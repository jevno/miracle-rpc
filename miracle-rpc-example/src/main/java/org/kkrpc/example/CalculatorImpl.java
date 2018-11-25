package org.kkrpc.example;

import java.util.List;

public class CalculatorImpl implements Calculator {

	private RemoteCollection coll;
	
	
	public void setColl(RemoteCollection coll) {
		this.coll = coll;
	}

	@Override
	public int add(Integer a, Integer b) {
		return a + b;
	}

	@Override
	public int getCombinedListSize(List<String> oneList,
			List<String> anotherList) {
		return coll.getListSize(oneList) + coll.getListSize(anotherList);
	}

	@Override
	public MyBean echoBean(MyBean obj) {
		return obj;
	}

}
