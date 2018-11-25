package com.miracle.module.rpc.router;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.RpcException;

import java.util.ArrayList;
import java.util.List;

public class GroupRouter implements Router{

	private RpcConfig config;
	private static final String ANYGROUP = "*";
	private List<String> groupList = new ArrayList<String>();
	
	public GroupRouter(RpcConfig config)
	{
		this.config = config;
		String group = config.getParameter(Constants.GROUP_KEY);
		if(!StringUtils.isBlank(group))
		{
			String[] groups = group.split(","); 
			if(groups != null && groups.length > 0)
			{
				for(String tmp: groups)
				{
					groupList.add(tmp);
				}
			}
		}
	}
	
	@Override
	public RpcConfig getConfig() {
		return config;
	}

	@Override
	public List<RpcConfig> route(List<RpcConfig> providerConfigs, RpcConfig consumerConfig) throws RpcException {
		
		if(groupList.size() <= 0 || groupList.contains(ANYGROUP)) {
			return providerConfigs;
		}
		
		List<RpcConfig> routedConfigs = new ArrayList<RpcConfig>(1);
		for(RpcConfig config : providerConfigs)
		{
			String group = config.getParameter(Constants.GROUP_KEY);
			if(StringUtils.isBlank(group) || ANYGROUP.equals(group) || Constants.GRAY_GROUP_NAME.equalsIgnoreCase(group) 
					|| groupList.contains(group))
			{
				routedConfigs.add(config);
			}
		}
		
		return routedConfigs;
	}

}
