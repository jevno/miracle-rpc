package com.miracle.module.rpc.core.api;

public class RpcCallSnapshot {
	private long timestamp;
	private int concurrent;
	private int total;
	private int succeed;
	private int failed;
	private long succeedElapsed;
	private long failedElapsed;
	
	public RpcCallSnapshot()
	{
		this.timestamp = System.currentTimeMillis();
	}
	
	public RpcCallSnapshot(long timestamp, int concurrent, int total, int succeed, int failed,
			long succeedElapsed, long failedElapsed)
	{
		this.timestamp = timestamp;
		this.concurrent = concurrent;
		this.total = total;
		this.succeed = succeed;
		this.failed = failed;
		this.succeedElapsed = succeedElapsed;
		this.failedElapsed = failedElapsed;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getConcurrent() {
		return concurrent;
	}

	public int getTotal() {
		return total;
	}

	public int getSucceed() {
		return succeed;
	}

	public int getFailed() {
		return failed;
	}

	public long getSucceedElapsed() {
		return succeedElapsed;
	}

	public long getFailedElapsed() {
		return failedElapsed;
	}
	
}
