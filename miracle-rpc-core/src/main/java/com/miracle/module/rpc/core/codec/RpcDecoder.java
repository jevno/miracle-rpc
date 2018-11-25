package com.miracle.module.rpc.core.codec;

import com.miracle.module.rpc.core.api.RpcHeader;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;


public class RpcDecoder extends ByteToMessageDecoder{

	private final static Class<?> requestClass = RpcRequest.class;
	private final static Class<?> resultClass = RpcResult.class;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		if (in.readableBytes() < RpcHeader.HEADER_LEN) {
            return;
        }
        
        byte[] header = new byte[RpcHeader.HEADER_LEN];
        in.readBytes(header, 0, RpcHeader.HEADER_LEN);
        
        int payloadLen = RpcHeader.getPayloadLength(header);
        if (payloadLen < 0 || in.readableBytes() < payloadLen) {
            ctx.close();
        }
        int serializeType = RpcHeader.getSerialType(header);
        byte[] payload = new byte[payloadLen];
        in.readBytes(payload);
        
        boolean isRequest = RpcHeader.isRequest(header);
        
        Object obj;
        if(serializeType == RpcHeader.CODEC_PROTOSTUFF){
        	if(isRequest) {
				obj = SerializationUtils.deserialize(payload, requestClass);
			}
        	else {
				obj = SerializationUtils.deserialize(payload, resultClass);
			}
        } else if(serializeType == RpcHeader.CODEC_FASTJSON) {
        	if(isRequest) {
				obj = SerializationUtils.deserializeFastjson(payload, requestClass);
			}
        	else {
				obj = SerializationUtils.deserializeFastjson(payload, resultClass);
			}
        } else{
        	if(isRequest) {
				obj = SerializationUtils.deserializeHessian(payload, requestClass);
			}
        	else {
				obj = SerializationUtils.deserializeHessian(payload, resultClass);
			}
        }
        
        out.add(obj);
	}

}
