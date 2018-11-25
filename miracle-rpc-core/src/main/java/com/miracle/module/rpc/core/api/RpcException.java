package com.miracle.module.rpc.core.api;

public final class RpcException extends RuntimeException {

	private static final long serialVersionUID = 7815426752583648734L;

    public static final int UNKNOWN_EXCEPTION = 0;
    
    public static final int NETWORK_EXCEPTION = 1;
    
    public static final int TIMEOUT_EXCEPTION = 2;
    
    public static final int BIZ_EXCEPTION = 3;
    
    public static final int FORBIDDEN_EXCEPTION = 4;
    
    public static final int SERIALIZATION_EXCEPTION = 5;
    
    public static final int NOINVOKER_EXCEPTION = 6;
    
    public static final int NETPROTOCOL_EXCEPTION = 7;
    
    public static final int UNMATCHEDID_EXCEPTION = 8;
    
    public static final int CLSNOTFOUND_EXCEPTION = 9;
    
    public static final int PUBSUB_EXCEPTION = 10;
    
    public static final int CANCELLED_EXCEPTION = 11;
    
    public static final int UNSUPPORT_EXCEPTION = 12;
    
    public static final int NOSERVICE_EXCEPTION = 13;
    
    public static final int NONEORMORE_METHOD_EXCEPTION = 14;
    
    public static final int LOCAL_CIRCUITBREAKER_OPEN_EXCEPTION = 15; /*断路异常，接口多次错误，暂时不可用(消费端)*/
    
    public static final int TPSLIMIT_EXCEPTION = 16;    /*流控异常，接口访问次数超过限制(提供者端)*/
    
    public static final int EXECUTION_EXCEPTION = 17;   /*线程池reject(提供者端)*/
    
    public static final int DEGRADE_EXCEPTION = 18;     /*接口方法降级(提供者端)*/
    
    public static final int NOSEMAPHORE_EXCEPTION = 19; /*超过方法最大并发限额(提供者端)*/
        
    public static final int LOCAL_DEGRADE_EXCEPTION = 20;     /*接口方法降级(消费端)*/
    
    public static final int RESPONSE_PACKET_TOO_LONG_EXCEPTION = 21; /* 响应包过大*/
    
    private int code; // RpcException不能有子类，异常类型用ErrorCode表示，以便保持兼容。

    public RpcException() {
        super();
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(int code) {
        super();
        this.code = code;
    }

    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RpcException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }
    
    public boolean isForbidded() {
        return code == FORBIDDEN_EXCEPTION;
    }

    public boolean isTimeout() {
        return code == TIMEOUT_EXCEPTION;
    }

    public boolean isNetwork() {
        return code == NETWORK_EXCEPTION;
    }

    public boolean isSerialization() {
        return code == SERIALIZATION_EXCEPTION;
    }
    
    public boolean isIgnorableExceptionForErrorStat()
    {
    	return code == BIZ_EXCEPTION || 
    			code == SERIALIZATION_EXCEPTION ||
    			code == LOCAL_CIRCUITBREAKER_OPEN_EXCEPTION || 
    			code == CANCELLED_EXCEPTION || 
    			code == LOCAL_DEGRADE_EXCEPTION;
    }
}
