package com.miracle.module.rpc.remoting.channelhandler;

import com.miracle.module.rpc.core.api.RpcException;
import io.netty.channel.Channel;

public interface ExchangeHandler extends ChannelInOutHandler{
	Object reply(Channel channel, Object request) throws RpcException;
}
