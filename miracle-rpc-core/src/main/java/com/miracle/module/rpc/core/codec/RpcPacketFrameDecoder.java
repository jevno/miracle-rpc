package com.miracle.module.rpc.core.codec;

import com.miracle.module.rpc.core.api.RpcHeader;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcPacketFrameDecoder extends LengthFieldBasedFrameDecoder {
	// LengthFieldBasedFrameDecoder(1048576, 4, 4, 4, 0))
	private static final int MAX_FRAME_LENGTH = 1024 * 1024;
	private static final int maxFrameLength = MAX_FRAME_LENGTH;
	private static final int lengthFieldOffset = RpcHeader.LEN_OF_MAGIC + RpcHeader.LEN_OF_SERIALTYPE + RpcHeader.LEN_OF_VERSION;
	private static final int lengthFieldLength = RpcHeader.LEN_OF_PAYLOADSIZE;
	private static final int lengthAdjustment = RpcHeader.LEN_OF_RESERVED;
	private static final int initialBytesToStrip = 0;
	
	public RpcPacketFrameDecoder()
	{  
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}
}
