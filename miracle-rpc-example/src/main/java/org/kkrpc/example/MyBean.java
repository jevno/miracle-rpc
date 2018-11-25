package org.kkrpc.example;


import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.codec.SerializationUtils;

public class MyBean {
	private int age;
	private String name;
	private double salary;
	
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getSalary() {
		return salary;
	}
	public void setSalary(double salary) {
		this.salary = salary;
	}
	
	public static void main(String[] args)
	{
		RpcRequest req = new RpcRequest();
		req.setId(1);
		req.setSerializeType((short) 2);
		req.setInterfaceName("org.kkrpc.example.Calculator");
		req.setMethodName("echoBean");
		Object[] params = new Object[1];
		MyBean beanobj = new MyBean();
		beanobj.setAge(10);
		beanobj.setName("jjj");
		beanobj.setSalary(20.0);
		params[0] = beanobj;
		req.setArguments(params);
		byte[] buf = SerializationUtils.serializeFastjson(req);
		System.out.println(buf.length);
	
		System.out.println(new String(buf));
	}
}
