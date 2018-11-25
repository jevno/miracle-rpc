package com.miracle.module.rpc.core.codec;

import com.miracle.module.rpc.core.api.OutEncodeException;
import com.miracle.module.rpc.core.api.RpcHeader;
import com.miracle.module.rpc.core.api.RpcObject;
import com.miracle.module.rpc.core.api.RpcResult;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcObject> {

	private static final int MAX_FRAME_LENGTH = 1024 * 1024;
	
	@Override
	protected void encode(ChannelHandlerContext ctx, RpcObject msg, ByteBuf out)
			throws Exception {
		// TODO Auto-generated method stub
		byte[] payload = null;
    	int serializeType = msg.getSerializeType();
		
		if(serializeType == RpcHeader.CODEC_PROTOSTUFF){
			payload = SerializationUtils.serialize(msg);
		} else if(serializeType == RpcHeader.CODEC_FASTJSON) {
			payload = SerializationUtils.serializeFastjson(msg);
		} else{
			payload = SerializationUtils.serializeHessian(msg);
		}
    	
		int payloadLen = 0;
		if(payload != null)
		{
			payloadLen = payload.length;
		}
		int inoutType = RpcHeader.REQUEST_TYPE;
		if(msg instanceof RpcResult)
		{
			inoutType = RpcHeader.RESULT_TYPE;
		}
			
		byte[] header = RpcHeader.encode(serializeType, RpcHeader.DEF_VER, payloadLen, inoutType);
        out.writeBytes(header);
        out.writeBytes(payload);
        int size = out.readableBytes();
        if (size > MAX_FRAME_LENGTH) {
        	String errMsg = "RpcEncode packet length is too long, limit is : " + MAX_FRAME_LENGTH + ", this is : " + size;
        	out.clear();
        	OutEncodeException e = new OutEncodeException(msg, errMsg);
        	throw e;
        }
	}

}
