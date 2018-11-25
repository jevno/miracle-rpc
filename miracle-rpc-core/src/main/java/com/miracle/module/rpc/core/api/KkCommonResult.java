package com.miracle.module.rpc.core.api;

import java.io.Serializable;

/**
 * Title: KkCommonResult
 * <p>
 * Description: KK模块返回的通用数据格式
 * </p>
 * 
 * @author 魏安稳<a href="mailto:anwen.wei@melot.cn"/>
 * @version V1.0
 * @since 2017年6月20日 下午4:01:48
 */
public class KkCommonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public KkCommonResult(String code, String msg, T data){
         this.code = code;
         this.msg = msg;
         this.data = data;
    }

    public KkCommonResult(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    /**
     * 错误码
     */
    private String code;
    
    /**
     * 错误描述
     */
    private String msg;
    
    /**
     * 结果数据
     */
    private T data;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    

    public void setData(T data) {
        this.data = data;
    }
    
    public T getData() {
    	return this.data;
    }

    @Override
    public String toString() {
        return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }

}