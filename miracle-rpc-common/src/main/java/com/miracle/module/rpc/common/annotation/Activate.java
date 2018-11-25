package com.miracle.module.rpc.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Activate {
    /**
     * Group过滤条件。 {provider,consumer}
     * 如没有Group设置，则不过滤。
     */
    String[] group() default {};

    /**
     * Key过滤条件。包含{@link ExtensionLoader#getActivateExtension}的URL的参数Key中有，则返回扩展。
     * <p />
     * 示例：<br/>
     * 注解的值 <code>@Activate("cache,validatioin")</code>，
     * 则{@link RpcConfig的参数有<code>cache</code>Key，或是<code>validatioin</code>则返回扩展。
     * <br/>
     * 如没有设置，则不过滤。
     */
    String[] value() default {};
    
    String auto() default "false"; 

    /**
     * 作为provider时的排序信息，可以不提供。
     */
    int providerorder() default 0;
    
    /**
     * 作为consumer时的排序信息，可以不提供。
     */
    int consumerorder() default 0;
}