package com.miracle.module.rpc.config;

public class InterfaceConfig extends AbstractInvokerConfig {
	protected ApplicationConfig application;
	protected RegistryConfig registry;
	
	// 分组信息
	protected String 			   group;
    // 过滤器
    protected String               filter;
    
    // 监听器
    protected String               listener;
    
    protected String 			   monitor;
    

    
    //连接心跳检测时间间隔
    private Integer 	heartbeat;
    
    //连接心跳检测连接超时
    private Integer 	heartbeatTimeout;
    
    //灰度运行时命中规则
    protected String 	rule;
    //灰度运行时规则参数
    protected String 	ruleArg;
	
	public ApplicationConfig getApplication() {
		return application;
	}
	public void setApplication(ApplicationConfig application) {
		this.application = application;
	}
	public RegistryConfig getRegistry() {
		return registry;
	}
	public void setRegistry(RegistryConfig registry) {
		this.registry = registry;
	}
	
	protected void fixApplicationConfig()
	{
		if(application == null)
		{
			application = new ApplicationConfig();
		}
		appendProperties(application);
	}
	
	protected void fixRegistryConfig()
	{
		if(registry == null)
		{
			registry = new RegistryConfig();
		}
		appendProperties(registry);
	}
	
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getListener() {
		return listener;
	}
	public void setListener(String listener) {
		this.listener = listener;
	}
	public Integer getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(Integer heartbeat) {
		this.heartbeat = heartbeat;
	}
	public Integer getHeartbeatTimeout() {
		return heartbeatTimeout;
	}
	public void setHeartbeatTimeout(Integer heartbeatTimeout) {
		this.heartbeatTimeout = heartbeatTimeout;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getMonitor() {
		return monitor;
	}
	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public String getRuleArg() {
		return ruleArg;
	}
	public void setRuleArg(String ruleArg) {
		this.ruleArg = ruleArg;
	}
}
