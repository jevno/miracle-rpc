package com.miracle.module.rpc.core.api;

import com.miracle.module.rpc.common.utils.Bytes;

public class RpcHeader {
	//| --  MAGIC --   ||-SERIAL-|| -ver- |
	//|XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX|
	//|    ------payload length-------    |
	//|XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX|
	//|    ----- reserved ------          |
	//|XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX|
	
	public static int HEADER_LEN = 12;
	public static int LEN_OF_MAGIC = 2;
	public static int LEN_OF_SERIALTYPE = 1;
	public static int LEN_OF_VERSION = 1;
	public static int LEN_OF_PAYLOADSIZE = 4;
	public static int LEN_OF_INOUTTYPE = 1;
	public static int LEN_OF_RESERVED = 4;
	
	public static short MAGIC = (short)0xbbbb;
	public static int DEF_VER = 4;
	
	public static short CODEC_PROTOSTUFF = 0;
	public static short CODEC_HESSIAN = 1;
	public static short CODEC_FASTJSON = 2;
	
	public static short REQUEST_TYPE = 0;
	public static short RESULT_TYPE = 1;
	
	public static byte[] encode(int serialType, int ver, int payloadLen, int inoutType)
	{
		byte[] buf = new byte[HEADER_LEN];
		Bytes.short2bytes(MAGIC, buf);
		buf[2] = (byte)serialType;
		buf[3] = (byte)ver;
		Bytes.int2bytes(payloadLen, buf, 4);
		buf[8] = (byte)inoutType;
		
		return buf;
	}
	
	public static short getMagic(byte[] buf)
	{
		return Bytes.bytes2short(buf);
	}
	
	public static int getSerialType(byte[] buf)
	{
		return (int)buf[2];
	}
	
	public static int getVersion(byte[] buf)
	{
		return (int)buf[3];
	}
	
	public static int getPayloadLength(byte[] buf)
	{
		return Bytes.bytes2int(buf, 4);
	}
	
	public static boolean isRequest(byte[] buf)
	{
		return (int)buf[8] == REQUEST_TYPE;
	}
	
	public static boolean isResult(byte[] buf)
	{
		return (int)buf[8] == RESULT_TYPE;
	}
	
	private RpcHeader(){}
}
