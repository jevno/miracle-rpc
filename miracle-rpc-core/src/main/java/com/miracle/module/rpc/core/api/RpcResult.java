package com.miracle.module.rpc.core.api;



public class RpcResult extends RpcObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3710777586119880182L;

	private Object                   result;

	private Throwable                exception;
	
	private RpcProtoError			 protoErr;
	    
    public RpcResult(){
    }
    public RpcResult(int id)
    {
    	this.id = id;
    }

    public RpcResult(int id, short serializeType, Object result){
    	this(id);
    	this.serializeType = serializeType;
        this.result = result;
    }

    public RpcResult(int id, short serializeType, Throwable exception){
    	this(id);
    	this.serializeType = serializeType;
        this.exception = exception;
    }
    
    public RpcResult(int id, short serializeType, RpcProtoError protoErr)
    {
    	this(id);
    	this.serializeType = serializeType;
    	this.protoErr = protoErr;
    }
    
    public RpcProtoError getProtoErr()
    {
    	return protoErr;
    }

    public void setProtoErr(RpcProtoError protoErr)
    {
    	this.protoErr = protoErr;
    }
    
	public Object getValue() {
		return result;
	}
	
	public void setValue(Object value) {
        this.result = value;
    }


	public Throwable getException() {
		return exception;
	}
	
	public void setException(Throwable e) {
        this.exception = e;
    }

	public boolean hasException() {
		return this.exception != null || this.protoErr != null;
	}

	@Override
    public String toString() {
        return "RpcResult [result=" + result + ", exception=" + exception + "]";
    }

	public Object recreate() throws Throwable {
		if (exception != null) {
			throw exception;
		}
		return result;
	}
}
