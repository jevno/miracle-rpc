package com.miracle.module.rpc.core.api;

import com.miracle.module.rpc.common.RpcConfig;

public interface Protocol {

    /**
     * 暴露远程服务：<br>
     */
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;

    /**
     * 引用远程服务：<br>
     */
    <T> Invoker<T> refer(Class<T> type, RpcConfig config) throws RpcException;

    /**
     * 释放协议：<br>
     */
    void destroy();

}
