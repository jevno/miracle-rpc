package com.miracle.module.rpc.core.api;

public interface Exporter<T> {
    
    /**
     * get invoker.
     * 
     * @return invoker
     */
    Invoker<T> getInvoker();
    
    /**
     * unexport.
     * 
     * <code>
     *     getInvoker().destroy();
     * </code>
     */
    void unexport();

}
