package com.miracle.module.rpc.core.api;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcRequest extends RpcObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4825945177445541762L;

	private static AtomicInteger idGenerator = new AtomicInteger(0);
	
	private String				 interfaceName;
	
	private String               methodName;

    private Object[]             arguments;
    
    private Map<String, Object>  attachments;

    public RpcRequest() {
    }

    public RpcRequest(Class<?> cls, Method method, Object[] arguments) {
        this(cls.getName(), method.getName(), arguments);
    }

    public RpcRequest(String infName, String methodName, Object[] arguments) {
        this.id = idGenerator.addAndGet(1);
        this.serializeType = RpcHeader.CODEC_PROTOSTUFF;
    	this.interfaceName = infName;
    	this.methodName = methodName;
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    public void addAttachment(String key, String val)
    {
    	if(attachments == null)
    	{
    		attachments = new HashMap<String, Object>();
    	}
    	attachments.put(key, val);
    }
    
    public void addAttachmentIfAbsent(String key, String val)
    {
    	if(attachments == null)
    	{
    		attachments = new HashMap<String, Object>();
    	}
    	if(!attachments.containsKey(key))
    	{
    		attachments.put(key, val);
    	}
    }
    
    public void removeAttachment(String key)
    {
    	if(attachments != null)
    	{
    		attachments.remove(key);
    	}
    }
    
    public String getAttachment(String key)
    {
    	if(attachments != null)
    	{
    		return (String) attachments.get(key);
    	}
    	return null;
    }
    
    public Map<String, Object> getAttachments()
    {
    	return attachments;
    }

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
    
    @Override
    public String toString() {
        return "RpcRequest [serializeType=" + serializeType +", interfaceName=" + interfaceName + ", methodName=" + methodName + 
        		", arguments=" + Arrays.toString(arguments) + "]";
    }

}
