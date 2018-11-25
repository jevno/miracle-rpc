package com.miracle.module.rpc.remoting.heartbeat;

import io.netty.channel.Channel;

import java.util.Collection;

import org.apache.log4j.Logger;

final class HeartbeatTask implements Runnable{
	
	private final static Logger log = Logger.getLogger(HeartbeatTask.class);
	
	private ChannelProvider channelProvider;
	private int heartbeatInterval;
	private int heartbeatTimeout;
	private boolean isServerSide;
	
	public HeartbeatTask(ChannelProvider provider, int heartbeatInterval, int heartbeatTimeout, boolean isServerSide)
	{
		this.channelProvider = provider;
		this.heartbeatInterval = heartbeatInterval;
		this.heartbeatTimeout = heartbeatTimeout;
		this.isServerSide = isServerSide;
	}

	@Override
	public void run() 
	{
		try{
			long now = System.currentTimeMillis();
			if(channelProvider.getChannels() == null)
				return;
			
			for(Channel ch : channelProvider.getChannels())
			{
				if(!ch.isActive())
					continue;
				
				try{
					Long lastRead = HeartbeatChannelHelper.getReadTimestamp(ch);
					Long lastWrite = HeartbeatChannelHelper.getWriteTimestamp(ch);
					
					if(lastRead != null && now - lastRead > heartbeatTimeout)
					{
						log.warn( "Close channel " + ch
		                        + ", because heartbeat read idle time out: " + heartbeatTimeout + "ms" );
						ch.close();
					}
					
					if(!isServerSide)
					{//只依靠客户端主动心跳，服务端不再主动发心跳给客户端
						if( (lastRead != null && now - lastRead > heartbeatInterval) 
								|| (lastWrite != null && now - lastWrite > heartbeatInterval))
						{
							ch.writeAndFlush(HeartbeatUtil.getDefaultHeartbeatReq());
							if(log.isDebugEnabled())
							{
								 log.debug( "Send heartbeat to remote channel " + ch
			                             + ", cause: The channel has no data-transmission exceeds a heartbeat period: " + heartbeatInterval + "ms" );
							}
						}
					}
				}
				catch(Throwable t)
				{
					log.warn("Exception when heartbeat to remote channel " + ch, t);
				}
			}
		}
		catch(Throwable t)
		{
			log.warn( "Unhandled exception when heartbeat, cause: " + t.getMessage(), t );
		}
	}
	
	interface ChannelProvider {
		Collection<Channel> getChannels();
	}
}
