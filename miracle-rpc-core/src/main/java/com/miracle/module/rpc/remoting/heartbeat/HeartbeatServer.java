package com.miracle.module.rpc.remoting.heartbeat;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;
import com.miracle.module.rpc.remoting.RpcServer;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeartbeatServer implements RpcServer {
	private static final Logger log = Logger.getLogger(HeartbeatServer.class);

	private static final ScheduledThreadPoolExecutor scheduled = 
			new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("server-heartbeat", true));
	
	 // 心跳定时器
    private ScheduledFuture<?> heatbeatTimer;
	
	private int heartbeatInterval;
	private int heartbeatTimeout;
	
	private final RpcServer server;
	
	public HeartbeatServer(RpcServer server)
	{
		this.server = server;
		RpcConfig config = server.getConfig();
		heartbeatInterval = config.getParameter(Constants.HEARTBEAT_KEY, Constants.DEFAULT_HEARTBEAT);
		heartbeatTimeout = config.getParameter(Constants.HEARTBEAT_TIMEOUT_KEY, 3 * heartbeatInterval);
		if(heartbeatTimeout < 2 * heartbeatInterval)
		{
			throw new IllegalStateException("heartbeatTimeout should be greater than heartbeatInterval * 2 ~");
		}
		startHeartbeatTimer();
	}
	
	private void startHeartbeatTimer()
	{
		stopHeartbeatTimer();
        if ( heartbeatInterval > 0 ) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                    new HeartbeatTask( new HeartbeatTask.ChannelProvider() {
                        public Collection<Channel> getChannels() {
                            return Collections.unmodifiableCollection(server.getChannels());
                        }
                    }, heartbeatInterval, heartbeatTimeout, true),
                    heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS );
        }
	}
	
	private void stopHeartbeatTimer()
	{
		if (heatbeatTimer != null && ! heatbeatTimer.isCancelled()) 
		{
	        try {
	            heatbeatTimer.cancel(true);
	            scheduled.purge();
	        } catch ( Throwable e ) {
	        	log.warn(e.getMessage(), e);
	        }
	    }
	    heatbeatTimer =null;
	}

	@Override
	public String getServerHost() {
		return server.getServerHost();
	}

	@Override
	public int getServerPort() {
		return server.getServerPort();
	}

	@Override
	public Collection<Channel> getChannels() {
		return server.getChannels();
	}

	@Override
	public void close() {
		stopHeartbeatTimer();
		server.close();
	}

	@Override
	public RpcConfig getConfig() {
		return server.getConfig();
	}

}
