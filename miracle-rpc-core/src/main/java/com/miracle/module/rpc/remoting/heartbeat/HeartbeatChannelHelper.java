package com.miracle.module.rpc.remoting.heartbeat;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class HeartbeatChannelHelper {
	public final static String LAST_READ_TIMESTAMP = "READ_TS";
	public final static String LAST_WRITE_TIMESTAMP = "WRITE_TS";
	
	private ConcurrentHashMap<String, Object> channelAttrMap = new ConcurrentHashMap<String, Object>();
	
	private final static ConcurrentHashMap<Channel, HeartbeatChannelHelper> allChannelsMap = 
			new ConcurrentHashMap<Channel, HeartbeatChannelHelper>();
	
	private Channel innerChannel;
	protected HeartbeatChannelHelper(final Channel channel)
	{
		this.innerChannel = channel;
	}
	
	public Channel getChannel()
	{
		return this.innerChannel;
	}
	
	public static List<HeartbeatChannelHelper> getAllChannelHelpers()
	{
		return Collections.synchronizedList(new ArrayList<HeartbeatChannelHelper>(allChannelsMap.values()));
	}
	
	public static HeartbeatChannelHelper getOrAddChannelHelper(Channel newchannel)
	{
		HeartbeatChannelHelper channelHelper = allChannelsMap.get(newchannel);
		if(channelHelper == null)
		{
			synchronized(allChannelsMap)
			{
				channelHelper = allChannelsMap.get(newchannel);
				if(channelHelper == null)
				{
					channelHelper = new HeartbeatChannelHelper(newchannel);
					allChannelsMap.put(newchannel, channelHelper);
				}
			}
		}
		return channelHelper;
	}
	
	public static void removeChannelHelper(Channel oldchannel)
	{
		synchronized(allChannelsMap)
		{
			allChannelsMap.remove(oldchannel);
		}
	}
	
	public static void setReadTimestamp(Channel channel)
	{
		HeartbeatChannelHelper helper = getOrAddChannelHelper(channel);
		helper.setAttribute(LAST_READ_TIMESTAMP, System.currentTimeMillis());
	}
	public static void setWriteTimestamp(Channel channel)
	{
		HeartbeatChannelHelper helper = getOrAddChannelHelper(channel);
		helper.setAttribute(LAST_WRITE_TIMESTAMP, System.currentTimeMillis());
	}
	public static Long getReadTimestamp(Channel channel)
	{
		HeartbeatChannelHelper helper = getOrAddChannelHelper(channel);
		return (Long) helper.getAttribute(LAST_READ_TIMESTAMP);
	}
	public static Long getWriteTimestamp(Channel channel)
	{
		HeartbeatChannelHelper helper = getOrAddChannelHelper(channel);
		return (Long) helper.getAttribute(LAST_WRITE_TIMESTAMP);
	}
	public static void clearReadTimestamp(Channel channel)
	{
		removeChannelHelper(channel);
	}
	public static void clearWriteTimestamp(Channel channel)
	{
		removeChannelHelper(channel);
	}
	
	public Object getAttribute(String key)
	{
		return channelAttrMap.get(key);
	}
	public void setAttribute(String key, Object value)
	{
		channelAttrMap.put(key, value);
	}
	public void removeAttribute(String key)
	{
		channelAttrMap.remove(key);
	}
	public boolean hasAttribute(String key)
	{
		return channelAttrMap.containsKey(key);
	}
	
	public String toString()
	{
		return this.innerChannel.toString() + ", attrubutes: " + this.channelAttrMap.toString();
	}
}
