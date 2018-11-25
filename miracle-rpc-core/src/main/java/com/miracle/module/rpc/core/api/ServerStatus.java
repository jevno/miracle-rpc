package com.miracle.module.rpc.core.api;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;

public enum ServerStatus {
	NEW("new"), READY("ready"), STOPPING("stopping"), STOPPED("stopped");
	
	private String status;
	private ServerStatus(String status)
	{
		this.status = status;
	}
	
	public String getStatus()
	{
		return this.status;
	}
	
	public String getKey()
	{
		return Constants.SERVER_STATUS_KEY;
	}
	
	public static ServerStatus parseStatusFromString(String status)
	{
		if(StringUtils.isEmpty(status))
			return READY;
		
		if("new".equals(status))
			return NEW;
		else if("ready".equals(status))
			return READY;
		else if("stopping".equals(status))
			return STOPPING;
		else if("stopped".equals(status))
			return STOPPED;
		else
			return READY;
	}
	
	public static ServerStatus getServerStatusByRpcConfig(RpcConfig config)
	{
		if(config == null)
			return NEW;
		
		return parseStatusFromString(config.getParameter(Constants.SERVER_STATUS_KEY));
	}
}
