package com.miracle.module.rpc.core.api;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RpcStatItem {
	private final AtomicInteger concurrent = new AtomicInteger(0);
	private final AtomicInteger total = new AtomicInteger(0);
	private final AtomicInteger succeed = new AtomicInteger(0);
	private final AtomicInteger failed = new AtomicInteger(0);
	private final AtomicLong succeedElapsed = new AtomicLong(0);
	private final AtomicLong failedElapsed = new AtomicLong(0);
	
	 // 最近一分钟内的镜像，数量6，10秒钟采样一次
    private final LinkedList<RpcCallSnapshot> csListMinute = new LinkedList<RpcCallSnapshot>();

    // 最近一小时内的镜像，数量6，10分钟采样一次
    private final LinkedList<RpcCallSnapshot> csListHour = new LinkedList<RpcCallSnapshot>();
    

	public AtomicInteger getConcurrent() {
		return concurrent;
	}

	public AtomicInteger getTotal() {
		return total;
	}

	public AtomicInteger getSucceed() {
		return succeed;
	}

	public AtomicInteger getFailed() {
		return failed;
	}

	public AtomicLong getSucceedElapsed() {
		return succeedElapsed;
	}

	public AtomicLong getFailedElapsed() {
		return failedElapsed;
	}


	public void samplingInSeconds()
	{
        synchronized (this.csListMinute) {
            this.csListMinute.add(new RpcCallSnapshot(System.currentTimeMillis(), this.concurrent.get(), this.total
                .get(), this.succeed.get(), this.failed.get(), this.succeedElapsed.get(), this.failedElapsed.get()));
            if (this.csListMinute.size() > 7) {
                this.csListMinute.removeFirst();
            }
        }
    }


	public void samplingInMinutes() {
		synchronized (this.csListHour) {
	        this.csListHour.add(new RpcCallSnapshot(System.currentTimeMillis(), this.concurrent.get(), this.total
	                .get(), this.succeed.get(), this.failed.get(), this.succeedElapsed.get(), this.failedElapsed.get()));
	        if (this.csListHour.size() > 7) {
	            this.csListHour.removeFirst();
	        }
        }
    }
	
	public RpcCallSnapshot getLast10SecondStat()
	{
		if(!this.csListMinute.isEmpty())
			return this.csListMinute.getLast();
		return new RpcCallSnapshot();
	}
	
	public RpcCallSnapshot getLast10MinStat()
	{
		if(!this.csListHour.isEmpty())
			return this.csListHour.getLast();
		return new RpcCallSnapshot();
	}
	
	private static RpcCallSnapshot computeStatsData(final LinkedList<RpcCallSnapshot> csList) {
	   int concurrent = 0;
       int total = 0;
       int succeed = 0;
       int failed = 0;
       long succeedElapsed = 0;
       long failedElapsed = 0;
       long timeStamp = 0;
       synchronized (csList) {
            if (!csList.isEmpty()) {
            	timeStamp = csList.getLast().getTimestamp();
            	for(RpcCallSnapshot sn : csList)
            	{
            		if(sn.getConcurrent() > concurrent)
            			concurrent = sn.getConcurrent();
            	}
            	RpcCallSnapshot first = csList.getFirst();
            	RpcCallSnapshot last = csList.getLast();
            	total = last.getTotal() - first.getTotal();
        		succeed = last.getSucceed() - first.getSucceed();
        		failed = last.getFailed() - first.getFailed();
        		succeedElapsed = last.getSucceedElapsed() - first.getSucceedElapsed();
        		failedElapsed = last.getFailedElapsed() - first.getFailedElapsed();
            }
            
        }

        return new RpcCallSnapshot(timeStamp, concurrent, total, succeed, failed, succeedElapsed, failedElapsed);
    }


    public RpcCallSnapshot getLastMinStat() {
        return computeStatsData(this.csListMinute);
    }


    public RpcCallSnapshot getLastHourStat() {
        return computeStatsData(this.csListHour);
    }
	
}
