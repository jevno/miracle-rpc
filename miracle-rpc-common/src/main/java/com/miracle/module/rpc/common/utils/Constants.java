package com.miracle.module.rpc.common.utils;

public class Constants {
	public static final String KKRPC_PROPERTIES_KEY = "kkrpc.properties.file";
	public static final String DEFAULT_KKRPC_PROPERTIES = "kkrpc.properties";
	public static final String DEFAULT_KEY_PREFIX = "default";
	public static final String INTERFACE_KEY = "interface";
	public static final String SERVER_STATUS_KEY = "server.status";
	public static final String SIDE_KEY = "side";
	public static final String CONSUMER_SIDE = "consumer";
	public static final String PROVIDER_SIDE = "provider";
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String PID_KEY	= "pid";
	public static final String LOCAL_ADDRESS_KEY = "local.address";
	
	public static final String FILTER_KEY = "filter";	
	public static final String LISTENER_KEY = "listener";
	
	public static final String PORT_KEY = "port";
	public static final int DEFAULT_PORT = 3344;
	
	public static final String HOST_KEY = "host";
	
	public static final String APP_NAME_KEY = "app.name";
	
	public static final String APP_VERSION_KEY = "app.version";
	public static final String APP_AUTHOR_KEY = "author";
	
	public static final String VERSION_KEY = "version";
	public static final String DEFAULT_VERSION = "1.0.0";
	
	public static final String TIMEOUT_KEY = "timeout";
	public static final int DEFAULT_TIMEOUT = 8000;
	
	public static final String CONNECT_TIMEOUT_KEY = "connect.timeout";
	public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
	
	public static final String IO_THREADS_KEY = "io.threads";
	public static final int DEFAULT_IO_THREADS = Runtime.getRuntime()
            .availableProcessors() + 1;
	
	public static final String BOSS_THREADS_KEY = "boss.threads";
	public static final int DEFAULT_BOSS_THREADS = 1;
	
	public static final String RECONNECT_KEY = "reconnect";
	public static final int DEFAULT_RECONNECT_PERIOD = 20000;
	
	public static final String CONNECTIONS_KEY = "connections";
	public static final int DEFAULT_CONNECTIONS = 1;
	
	public static final String WORKER_THREADS_KEY = "worker.threads";
	public static final int DEFAULT_WORKER_THREADS = 8;
	
	public static final String QUEUE_SIZE_KEY = "queue.size";
	public static final int DEFAULT_QUEUE_SIZE = 500;
	
	public static final String ADDRESS_KEY = "address";
	
	public static final String SESSCONN_TIMEOUT_KEY = "sessconn.timeout";
	public static final int DEFAULT_SESSCONN_TIMEOUT = 15000;
	
	public static final String SESSION_TIMEOUT_KEY = "session.timeout";
	public static final int DEFAULT_SESSION_TIMEOUT = 30000;
	
	public static final String ROUNDROBIN_LOAD_BALANCE = "roundrobin";
	
	public static final String LOAD_BALANCE_KEY = "load.balance";
	public static final String DEFAULT_LOAD_BALANCE = ROUNDROBIN_LOAD_BALANCE;
	
	public static final String RETRIES_KEY = "retries";
	public static final int DEFAULT_RETRIES = 3;
	
	public static final String FAILOVER_CLUSTER = "failover";
	public static final String CLUSTER_KEY = "cluster";
	public static final String DEFAULT_CLUSTER = FAILOVER_CLUSTER;
	
	public static final String ROOT_PATH = "/kkrpc/";
	public static final String PATH_SEPARATOR = "/";
	public static final String PROVIDERS_PATH = "providers";
	public static final String CONSUMERS_PATH = "consumers";
	
	public static final String ASYNC_KEY = "async";
	
	public static final String HEARTBEAT_KEY = "heartbeat";
	public static final int DEFAULT_HEARTBEAT = 60000;
	
	public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";
	
	public static final String GROUP_KEY = "group";
	
	public static final String IPROUTER_KEY = "iprouter";
	
	public static final String INJVM_EXPORTER_LISTENER = "injvm";
	
	public static final String MONITOR_KEY = "monitor";
	public static final String DEFAULT_MONITOR = "*";
	
	public static final String MONITOR_INTERVAL_KEY = "monitor.interval";
	public static final long DEFAULT_MONITOR_INTERVAL = 60000;
	
	public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
	public static final long DEFAULT_REGISTRY_RETRY_PERIOD = 5000;
	
	public static final String INIT_KEY = "init";
	
	public static final String INJVM_KEY = "injvm";
	
	public static final String ECHO_INTERFACE = "Echo";
	public static final String ECHO_METHOD = "echo";
	
	public static final String TPS_LIMIT_RATE_KEY = "tps";
	
	public static final String DEGRADE_FILTER_KEY = "degrade";
	
	public static final String TPS_LIMIT_INTERVAL_KEY = "tps.interval";
	public static final long DEFAULT_TPS_LIMIT_INTERVAL = 60000;
	
	public static final String CB_FAIL_THRESHOLD_KEY = "fail.threshold";
	public static final int DEFAULT_CB_FAIL_THRESHOLD = 50;
	public static final String CB_VOLUME_THRESHOLD_KEY = "volume.threshold";
	public static final int DEFAULT_CB_VOLUME_THRESHOLD = 100;
	public static final String CB_HALF_OPEN_TIMEOUT_KEY = "half.open.timeout";
	public static final int DEFAULT_CB_HALF_OPEN_TIMEOUT = 10000;
	public static final String CB_CLOSE_THRESHOLD_KEY = "close.threshold";
	public static final int DEFAULT_CB_CLOSE_THRESHOLD = 5;
	public static final String CB_FORCE_OPEN_KEY = "force.open";
	public static final String CB_FORCE_CLOSE_KEY = "force.close";
	
    public static final int DEFAULT_CLOSE_PERCENT_THRESHOLD = 90;
    public static final int DEFAULT_MIN_SAMPLE_NUM = 30;
    
    public static final String CACHE_KEY = "cache";
    public static final String CACHE_SIZE_KEY = "cache.size";
    public static final int DEFAULT_CACHE_SIZE = 1000;
    public static final String CACHE_TTL_KEY = "cache.ttl";
    public static final int DEFAULT_CACHE_TTL = 10*60;
    
    public static final String THREADS_NAME_KEY = "threads.name";
    public static final String DEFAULT_THREADS_NAME = "DefaultWokerThreadPool";
    public static final String THREADS_CORE_SIZE_KEY = "threads.core.size";
    public static final int    DEFAULT_THREADS_CORE_SIZE = 1;
    public static final String THREADS_QUEUE_SIZE_KEY = "threads.queue.size";
    public static final int    DEFAULT_THREADS_QUEUE_SIZE = 0;
    
    public static final String SEMAPHORE_CONCURRENT_KEY = "semaphore.concurrent";
    public static final int    DEFAULT_SEMAPHORE_CONCURRENT = Integer.MAX_VALUE;
    
    //灰度组名称
    public static final String GRAY_GROUP_NAME = "gray";
    public static final String GRAY_RULE_KEY = "rule";
    public static final String DEFAULT_GRAY_RULE = "rand";
    public static final String GRAY_RULE_ARG_KEY = "rule.arg";
    public static final String DEFAULT_GRAY_RULE_ARG = "10";
    
    public static final String CHECK_CONNECTION = "check.conn";
    
    public static final String[] MERGE_PROVIDER_CONFIG_KEYS = {HOST_KEY, PORT_KEY, SERVER_STATUS_KEY, 
    	GROUP_KEY, GRAY_RULE_KEY, GRAY_RULE_ARG_KEY};
    
    public static final String[] TO_UNIQUE_STRING_IGNORED_KEYS = {SERVER_STATUS_KEY};
    
    public static final String[] FILTER_CONFIG_KEYS = {FILTER_KEY, TPS_LIMIT_RATE_KEY, TPS_LIMIT_INTERVAL_KEY, DEGRADE_FILTER_KEY,
    	CB_FAIL_THRESHOLD_KEY, CB_VOLUME_THRESHOLD_KEY, CB_HALF_OPEN_TIMEOUT_KEY, CB_CLOSE_THRESHOLD_KEY, 
    	CB_FORCE_OPEN_KEY, CB_FORCE_CLOSE_KEY, CACHE_KEY, CACHE_SIZE_KEY, CACHE_TTL_KEY, THREADS_NAME_KEY, 
    	THREADS_CORE_SIZE_KEY, THREADS_QUEUE_SIZE_KEY, SEMAPHORE_CONCURRENT_KEY};
}
