package com.miracle.module.rpc.router;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NetUtils;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.RpcException;

import java.util.ArrayList;
import java.util.List;

public class IpRouter implements Router{

	private RpcConfig config;
	private static final String ANYIP = "*.*。*。*";
	private List<String> patternList = new ArrayList<String>();
	
	public IpRouter(RpcConfig config)
	{
		this.config = config;
		
		String iprouter = config.getParameter(Constants.IPROUTER_KEY);
		if(!StringUtils.isBlank(iprouter))
		{
			String[] patterns = iprouter.split(","); 
			if(patterns != null && patterns.length > 0)
			{
				for(String tmp: patterns)
				{
					patternList.add(tmp);
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
		
		if(patternList.size() <= 0 || patternList.contains(ANYIP)) {
			return providerConfigs;
		}
		
		List<RpcConfig> routedConfigs = new ArrayList<RpcConfig>(1);
		for(RpcConfig config : providerConfigs)
		{
			String host = config.getParameter(Constants.HOST_KEY);
			for(String pattern : patternList)
			{
				if(NetUtils.isIpMatchPattern(pattern, host))
				{
					routedConfigs.add(config);
					break;
				}
			}
		}
		
		return routedConfigs;
	}

}
