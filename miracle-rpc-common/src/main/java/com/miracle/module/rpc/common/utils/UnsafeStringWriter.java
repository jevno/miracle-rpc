package com.miracle.module.rpc.common.utils;

import java.io.IOException;
import java.io.Writer;

public class UnsafeStringWriter extends Writer{
	private StringBuilder mBuffer;

	public UnsafeStringWriter()
	{
		lock = mBuffer = new StringBuilder();
	}

	public UnsafeStringWriter(int size)
	{
		if( size < 0 )
		    throw new IllegalArgumentException("Negative buffer size");

		lock = mBuffer = new StringBuilder();
	}

	public void write(int c)
	{
		mBuffer.append((char)c);
	}

	public void write(char[] cs) throws IOException
	{
		mBuffer.append(cs, 0, cs.length);
	}

	public void write(char[] cs, int off, int len) throws IOException
	{
		if( (off < 0) || (off > cs.length) || (len < 0) ||
				((off + len) > cs.length) || ((off + len) < 0) )
			throw new IndexOutOfBoundsException();

		if( len > 0 )
			mBuffer.append(cs, off, len);
	}

	public void write(String str)
	{
		mBuffer.append(str);
	}

	public void write(String str, int off, int len)
	{
		mBuffer.append(str.substring(off, off + len));
	}

	public Writer append(CharSequence csq)
	{
		if (csq == null)
			write("null");
		else
			write(csq.toString());
		return this;
	}

	public Writer append(CharSequence csq, int start, int end)
	{
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	public Writer append(char c)
	{
		mBuffer.append(c);
		return this;
	}

	public void close(){}

	public void flush(){}

	@Override
	public String toString()
	{
		return mBuffer.toString();
	}
}
