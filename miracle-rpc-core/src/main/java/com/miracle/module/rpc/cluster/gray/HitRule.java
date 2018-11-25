package com.miracle.module.rpc.cluster.gray;

import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcRequest;

public interface HitRule {
	<T> boolean isHit(Invoker<T> inv, RpcRequest req);
}
