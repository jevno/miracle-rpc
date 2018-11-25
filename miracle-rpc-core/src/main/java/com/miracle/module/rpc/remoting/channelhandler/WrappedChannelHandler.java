package com.miracle.module.rpc.remoting.channelhandler;

import com.dianping.cat.Cat;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.threadpool.FixedWorkerThreadPool;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.impl.DefaultProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class WrappedChannelHandler extends ChannelHandlerDelegate{

	private final static Logger log = Logger.getLogger(WrappedChannelHandler.class);
	protected final ConcurrentHashMap<String, ExecutorService> allWorkerExecutorMap = 
			new ConcurrentHashMap<String/*threadName*/, ExecutorService>();
	protected final ConcurrentHashMap<String/*interface.methodname*/, String/*threadName*/> executorNameMap = 
			new ConcurrentHashMap<String, String>();
	
	protected final DefaultProtocol protocol;
	protected final ChannelInOutHandler channellHandler;
	
	private final ScheduledExecutorService requestQueueMonitor = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactory(){

				@Override
				public Thread newThread(Runnable r) {
					String name = "KKrpcReqQueueMonitor";
			        Thread ret = new Thread(r);
			        ret.setName(name);
			        ret.setDaemon(true);
			        return ret;
				}
				
			});

	@SuppressWarnings("serial")
	private static final Map<Integer, String> queueReportKeyMap = new HashMap<Integer , String>(){{
		put(0, "[none]");
		put(1, "[1-10)");
		put(2, "[10-100)");
		put(3, "[100-1000)");
		put(4, "[1000-10000)");
		put(5, "[10000-more)");
	}};

	public static String getQueueMonitorReportKey(int queueSize)
	{

		 int offset = 0;
		 while(queueSize > 0 && offset < 5)
		 {
			 offset++;
			 queueSize /= 10;
		 }
		 return queueReportKeyMap.get(offset);
	}

	@SuppressWarnings("serial")
	private static final Map<Integer, String> workerThreadNumReportKeyMap = new HashMap<Integer , String>(){{
		put(0, "[none]");
		put(1, "[1-5]");
		put(2, "(5-10]");
		put(3, "(10-15]");
		put(4, "(15-20]");
		put(5, "(20-more)");
	}};
	public static String getWorkerThreadMonitorReportKey(int threadSize)
	{
		 int offset = 0;
		 while(threadSize > 0 && offset < 5)
		 {
			 offset++;
			 threadSize -= 5;
		 }
		 return workerThreadNumReportKeyMap.get(offset);
	}
	
	public WrappedChannelHandler(ChannelInOutHandler handler, DefaultProtocol protocol)
	{
		this.channellHandler = handler;
		this.protocol = protocol;
		
		final RpcConfig config = protocol.getExporterConfigByInfName(null);//get shared common rpcconfig from any exporter
		final int threads = config.getParameter(Constants.WORKER_THREADS_KEY, Constants.DEFAULT_WORKER_THREADS);
        final int queues = config.getParameter(Constants.QUEUE_SIZE_KEY, Constants.DEFAULT_QUEUE_SIZE);
        ExecutorService defaultExecutor = (ExecutorService) FixedWorkerThreadPool.getInstance().getExecutor(config,
				Constants.DEFAULT_THREADS_NAME, threads, queues);
        this.allWorkerExecutorMap.put(Constants.DEFAULT_THREADS_NAME, defaultExecutor);
		
		requestQueueMonitor.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				try{
					if(WrappedChannelHandler.this.allWorkerExecutorMap.size() > 0)
					{
						for(String tName : WrappedChannelHandler.this.allWorkerExecutorMap.keySet())
						{
							ExecutorService executor = WrappedChannelHandler.this.allWorkerExecutorMap.get(tName);
							if(executor instanceof ThreadPoolExecutor)
							{
								ThreadPoolExecutor executorPool = (ThreadPoolExecutor)executor;
								int activeThreadSize = executorPool.getActiveCount();
								int queueSize = executorPool.getQueue().size();
								
								String serviceKey = config.getServiceKey();
								Cat.logEvent("KKrpcServerReqQueue@"+tName, serviceKey + "@" + getQueueMonitorReportKey(queueSize));
								Cat.logEvent("KKrpcServerActiveWorker@"+tName, serviceKey + "@" + getWorkerThreadMonitorReportKey(activeThreadSize));
								if(queueSize > 3 && activeThreadSize >= executorPool.getCorePoolSize())
								{
									log.warn("Too much request! Kkrpc server: " + serviceKey + 
											", active worker size: " + activeThreadSize +
											", request accumulation: " + queueSize);
								}
							}
						}
					}
				} 
				catch(Throwable t)
				{
					log.error("Unexpected error in requestQueueMonitor.", t);
				}
			}
			
		}, 1, 1, TimeUnit.MINUTES);
	}
	
	@Override
	public void close() {
		if(this.allWorkerExecutorMap.size() > 0)
		{
			for(ExecutorService executor : this.allWorkerExecutorMap.values())
			{
		        try {
		            if (executor instanceof ExecutorService) {
		                ((ExecutorService)executor).shutdown();
		            }
		        } catch (Throwable t) {
		            log.warn("fail to destroy thread pool of server: " + t.getMessage(), t);
		        }
			}
			this.allWorkerExecutorMap.clear();
		}
        
        try {
        	requestQueueMonitor.shutdown();
        } catch (Throwable t) {
            log.warn("fail to destroy requestQueueMonitor scheduled executor of server: " + t.getMessage(), t);
        }
        
        if(this.channellHandler != null) {
        	this.channellHandler.close();
        }
    }
	 
	public ExecutorService getExecutor(Object msg)
	{
		if(msg instanceof RpcRequest)
		{
			RpcRequest req = (RpcRequest)msg;
			String interfaceName = req.getInterfaceName();
			String methodName = req.getMethodName();
			
			RpcConfig config = this.protocol.getExporterConfigByInfName(interfaceName);
			
			String threadKey = interfaceName + "_" + methodName;
			String threadName = this.executorNameMap.get(threadKey);
			if(threadName == null)
			{
				threadName = config.getMethodParameter(methodName, Constants.THREADS_NAME_KEY,
						Constants.DEFAULT_THREADS_NAME);
				this.executorNameMap.putIfAbsent(threadKey, threadName);
			}
			ExecutorService executor = this.allWorkerExecutorMap.get(threadName);
			if(executor == null)
			{
				synchronized(this){
					if(this.allWorkerExecutorMap.get(threadName) == null)
					{
						int threads = config.getMethodParameter(methodName, 
								Constants.THREADS_CORE_SIZE_KEY, Constants.DEFAULT_THREADS_CORE_SIZE);
						int queues = config.getMethodParameter(methodName, 
								Constants.THREADS_QUEUE_SIZE_KEY, Constants.DEFAULT_THREADS_QUEUE_SIZE);
						executor = (ExecutorService) FixedWorkerThreadPool.getInstance().getExecutor(config,
								threadName, threads, queues);
						this.allWorkerExecutorMap.putIfAbsent(threadName, executor);
						executor = this.allWorkerExecutorMap.get(threadName);
					}
				}
			}
			return executor;
		}
		return this.allWorkerExecutorMap.get(Constants.DEFAULT_THREADS_NAME);
	}
	 
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		channellHandler.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channellHandler.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		channellHandler.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		channellHandler.exceptionCaught(ctx, cause);
	}

	@Override
	public ChannelInOutHandler getChannelHandler() {
		if(channellHandler instanceof ChannelHandlerDelegate)
		{
			return ((ChannelHandlerDelegate)channellHandler).getChannelHandler();
		}
		return channellHandler;
	}
	
	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		channellHandler.close(ctx, promise);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		channellHandler.write(ctx, msg, promise);
	}

}
