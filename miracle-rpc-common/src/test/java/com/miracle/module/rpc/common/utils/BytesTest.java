package com.miracle.module.rpc.common.utils;

import static org.junit.Assert.*;

import static org.hamcrest.core.Is.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BytesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCopyOf() {
		byte[] dest = Bytes.copyOf(new byte[]{0xa,0xb,0xc,0xd}, 2);
		assertThat(dest, is(new byte[]{0xa, 0xb}));
	}

	@Test
	public void testShort2bytesShort() {
		byte[] dest = Bytes.short2bytes((short)1234); //0x4d2
		assertThat(dest, is(new byte[]{(byte) 0x4, (byte)0xd2}));
	}

	@Test
	public void testShort2bytesShortByteArray() {
		byte[] dest = new byte[2];
		Bytes.short2bytes((short)1234, dest);
		assertThat(dest, is(new byte[]{(byte) 0x4, (byte)0xd2}));
	}

	@Test
	public void testInt2bytesInt() {
		byte[] dest = Bytes.int2bytes(1193046); //0x00123456
		assertThat(dest, is(new byte[]{0x00, 0x12, 0x34, 0x56}));
	}


	@Test
	public void testBytes2shortByteArray() {
		short s = Bytes.bytes2short(new byte[]{(byte) 0x4, (byte)0xd2});
		assertThat(s, is((short)1234));
	}

	@Test
	public void testBytes2intByteArray() {
		int ret = Bytes.bytes2int(new byte[]{0x00, 0x12, 0x34, 0x56});
		assertThat(ret, is(1193046));
	}

}
