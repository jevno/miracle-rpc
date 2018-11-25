package org.kkrpc.example;

import java.util.List;

public interface Calculator {
	int add(Integer a, Integer b);
	
	int getCombinedListSize(List<String> oneList, List<String> anotherList);
	
	MyBean echoBean(MyBean obj);
}
