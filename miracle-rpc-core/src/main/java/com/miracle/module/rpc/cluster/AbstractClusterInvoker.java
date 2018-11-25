package com.miracle.module.rpc.cluster;

import com.miracle.module.rpc.cluster.gray.HitRule;
import com.miracle.module.rpc.cluster.gray.RuleFactory;
import com.miracle.module.rpc.common.RpcConfig;
import com.miracle.module.rpc.common.utils.Constants;
import com.miracle.module.rpc.common.utils.StringUtils;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;
import com.miracle.module.rpc.core.api.RpcResult;
import com.miracle.module.rpc.directory.Directory;
import com.miracle.module.rpc.loadbalance.LoadBalance;
import com.miracle.module.rpc.loadbalance.LoadBalanceFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

	private static Logger log = Logger.getLogger(AbstractClusterInvoker.class);
	private final Directory<T> directory;
	private volatile boolean destroyed = false; 
	
	public AbstractClusterInvoker(Directory<T> directory)
	{
		if(directory == null)
		{
			throw new IllegalArgumentException("directory == null");
		}
		this.directory = directory;
	}
	
	@Override
	public RpcConfig getConfig() {
		// TODO Auto-generated method stub
		return this.directory.getConfig();
	}

	@Override
	public boolean isAvailable() {
		return directory.isAvailable();
	}

	@Override
	public void destroy() {
		directory.destroy();
		destroyed = true;
	}

	@Override
	public Class<T> getInterface() {
		// TODO Auto-generated method stub
		return this.directory.getInterface();
	}

	protected void checkWheatherDestoried() {
		if(destroyed){
            throw new RpcException("Rpc cluster invoker for " + getInterface()
                    + " is now destroyed! Can not invoke any more.");
        }
	}
	

    protected Invoker<T> select(LoadBalance loadbalance, RpcRequest request, List<Invoker<T>> invokers,
                                Invoker<T> grayInvoker, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0) {
			return null;
		}
      
        if(grayInvoker != null && grayInvoker.isAvailable())
        {
        	invokers.remove(grayInvoker);
        	selected.remove(grayInvoker);
        	try{
	        	if(doGrayRuleHitTest(grayInvoker, request))
	        	{
	        		return grayInvoker;
	        	}
        	}
        	catch(Throwable t)
        	{
        		log.warn("Unexpected exception occured while doGrayRuleHitTest.", t);
        	}
        }
        
        this.checkInvokers(invokers, request);
        Invoker<T> invoker = doselect(loadbalance, request, invokers, selected);
        
        return invoker;
    }
    
    private boolean doGrayRuleHitTest(Invoker<T> grayInvoker, RpcRequest request)
    {
    	HitRule rule = RuleFactory.getInstance().getRule(grayInvoker.getConfig());
    	return rule.isHit(grayInvoker, request);
    }
    
    private Invoker<T> doselect(LoadBalance loadbalance, RpcRequest request, List<Invoker<T>> invokers, 
    		List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0) {
			return null;
		}
        if (invokers.size() == 1) {
			return invokers.get(0);
		}
     
        List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>();
        for(Invoker<T> invoker : invokers){
            if(invoker.isAvailable()){
                if(selected == null || !selected.contains(invoker)){
                    reselectInvokers.add(invoker);
                }
            }
        }
        
        if(reselectInvokers.size()>0){
            return  loadbalance.select(reselectInvokers, getConfig(), request);
        }
        
        log.info("No available invoker in not selected set, so reselect from selected set here");
        if(selected != null){
            for(Invoker<T> invoker : selected){
				//优先选available
                if(invoker.isAvailable())
                {
                	reselectInvokers.add(invoker);
                }
            }
        }
        if(reselectInvokers.size() > 0){
            return  loadbalance.select(reselectInvokers, getConfig(), request);
        }

        //Maybe none of invoker is available currently, then just select one to try randomly
		if(invokers.size() > 0)
		{
			int randomIdx = new Random().nextInt(invokers.size());
			return invokers.get(randomIdx);
		}
        return null;
    }
	
	@Override
	public RpcResult invoke(RpcRequest request) throws RpcException {
		checkWheatherDestoried();
		List<Invoker<T>> invokers = list(request);
		Invoker<T> grayInvoker = getGrayInvoker(invokers);
		
		LoadBalance loadbalance = LoadBalanceFactory.getInstance().getLoadBalance(getConfig());
		
		return doInvoke(request, invokers, grayInvoker, loadbalance);
	}
	
	protected Invoker<T> getGrayInvoker(List<Invoker<T>> invokers)
	{
		Invoker<T> grayInvoker = null;
		if(invokers != null && invokers.size() > 0)
		{
			for(Invoker<T> inv : invokers)
			{
				String group = inv.getConfig().getParameter(Constants.GROUP_KEY);
				if(!StringUtils.isEmpty(group) && group.equalsIgnoreCase(Constants.GRAY_GROUP_NAME))
				{
					grayInvoker = inv;
					break;
				}
			}
		}
		return grayInvoker;
	}
	
	protected  List<Invoker<T>> list(RpcRequest request) throws RpcException {
    	List<Invoker<T>> invokers = directory.list(request);
    	return invokers;
    }
	
	protected void checkInvokers(List<Invoker<T>> invokers, RpcRequest request) {
        if (invokers == null || invokers.size() == 0) {
            throw new RpcException(RpcException.NOSERVICE_EXCEPTION, "Failed to invoke the method "
                    + request.getMethodName() + " in the service " + getInterface().getName() 
                    + ". No non-gray provider available for the service " + directory.getConfig().getServiceKey()
                    + " from registry " + directory.getConfig().getRegistryServices()
                    + ". Please check if the providers have been started and registered.");
        }
    }

	protected abstract RpcResult doInvoke(RpcRequest request, List<Invoker<T>> invokers, Invoker<T> grayInvoker,
             LoadBalance loadbalance) throws RpcException;
}


