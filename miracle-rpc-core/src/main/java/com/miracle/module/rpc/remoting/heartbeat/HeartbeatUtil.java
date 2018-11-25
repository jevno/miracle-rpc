package com.miracle.module.rpc.remoting.heartbeat;

import com.miracle.module.rpc.core.api.RpcHeader;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;

public class HeartbeatUtil {
	public static String HEARTBEAT_INF = "HB";
	public static String HEARTBEAT_METHOD = "HB_ECHO";
	private static final int HEARTBEAT_ID = -1;
	
	private static RpcRequest HB_REQUEST = new RpcRequest(HEARTBEAT_INF, HEARTBEAT_METHOD, null);
	private static RpcResult HB_HESSIAN_RESULT = new RpcResult(HB_REQUEST.getId(), RpcHeader.CODEC_HESSIAN, "HB_ECHOBACK");
	private static RpcResult HB_PROTOSTUFF_RESULT = new RpcResult(HB_REQUEST.getId(), RpcHeader.CODEC_PROTOSTUFF, "HB_ECHOBACK");
	private static RpcResult HB_FASTJSON_RESULT = new RpcResult(HB_REQUEST.getId(), RpcHeader.CODEC_FASTJSON, "HB_ECHOBACK");
	static {
		HB_REQUEST.setId(HEARTBEAT_ID);
		HB_HESSIAN_RESULT.setId(HEARTBEAT_ID);
		HB_PROTOSTUFF_RESULT.setId(HEARTBEAT_ID);
		HB_FASTJSON_RESULT.setId(HEARTBEAT_ID);
	}
	
	public static RpcRequest getDefaultHeartbeatReq()
	{
		return HB_REQUEST;
	}
	
	public static RpcResult getDefaultHeartbeatResult(short serializeType)
	{
		if(serializeType == RpcHeader.CODEC_HESSIAN)
		{
			return HB_HESSIAN_RESULT;
		}
		else if(serializeType == RpcHeader.CODEC_FASTJSON)
		{
			return HB_FASTJSON_RESULT;
		}
		return HB_PROTOSTUFF_RESULT;
	}
	
	public static boolean isHeartbeatRequest(Object msg)
	{
		if(msg instanceof RpcRequest)
		{
			return ((RpcRequest) msg).getId() == HEARTBEAT_ID;
		}
			
		return false;
	}
	
	public static boolean isHeartbeatResponse(Object msg)
	{
		if(msg instanceof RpcResult)
		{
			return ((RpcResult) msg).getId() == HEARTBEAT_ID;
		}
			
		return false;
	}
}
