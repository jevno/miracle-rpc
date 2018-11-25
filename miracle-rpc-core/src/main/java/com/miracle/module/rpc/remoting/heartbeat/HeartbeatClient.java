package com.miracle.module.rpc.remoting.heartbeat;

import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.NamedThreadFactory;
import com.miracle.module.rpc.remoting.ExchangeChannel;
import com.miracle.module.rpc.remoting.ResponseFuture;
import com.miracle.module.rpc.remoting.RpcClient;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeartbeatClient implements RpcClient {
	private static final Logger log = Logger.getLogger(HeartbeatClient.class);

	private static final ScheduledThreadPoolExecutor scheduled = 
			new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("client-heartbeat", true));
	
	 // 心跳定时器
    private ScheduledFuture<?> heatbeatTimer;
	
	private int heartbeatInterval;
	private int heartbeatTimeout;
	
	private final RpcClient client;
	public HeartbeatClient(RpcClient client) {
		this.client = client;
		
		RpcConfig config = client.getConfig();
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
                        	if(client.getChannels() == null)
                        		return null;
                            return Collections.unmodifiableCollection(client.getChannels());
                        }
                    }, heartbeatInterval, heartbeatTimeout, false),
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
	public void close() {
		stopHeartbeatTimer();
		client.close();
	}

	@Override
	public boolean isClosed() {
		return client.isClosed();
	}

	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	@Override
	public SocketAddress getConnectAddress() {
		return client.getConnectAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return client.getRemoteAddress();
	}

	@Override
	public SocketAddress getLocalAddress() {
		return client.getLocalAddress();
	}

	@Override
	public ExchangeChannel getChannel() {
		return client.getChannel();
	}

	@Override
	public ResponseFuture request(Object req, int timeout) {
		return client.request(req, timeout);
	}

	@Override
	public RpcConfig getConfig() {
		return this.client.getConfig();
	}

	@Override
	public Collection<Channel> getChannels() {
		return client.getChannels();
	}
	
}
