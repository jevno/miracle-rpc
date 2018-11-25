package org.kkrpc.example;

import java.util.List;

public class RemoteCollectionImpl implements RemoteCollection {

	@Override
	public int getListSize(List<String> mylist) {
		if(mylist == null)
			return 0;
		return mylist.size();
	}
	
}
