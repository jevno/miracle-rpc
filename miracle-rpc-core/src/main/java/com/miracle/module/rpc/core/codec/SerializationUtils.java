package com.miracle.module.rpc.core.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianFactory;
import com.miracle.module.rpc.core.api.RpcException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.log4j.Logger;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUtils {
	private static Logger log = Logger.getLogger(SerializationUtils.class);
	
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static Objenesis objenesis = new ObjenesisStd(true);
    
    private static final ThreadLocal<LinkedBuffer> localBuffer = 
    		new ThreadLocal<LinkedBuffer>() {
		    	 @Override protected LinkedBuffer initialValue() {    
		             return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);   
		         }    
    		};

    private SerializationUtils() {
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = localBuffer.get();
        buffer.clear();
        try {
            Schema<T> schema = getSchema(cls);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
        	log.error("Protostuff serialize message failed.", e);
            throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
        } 
    }

    public static <T> byte[] serializeFastjson(T obj) {
    	byte[] buf = JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
    	return buf;
    }
    
    public static <T> byte[] serializeHessian(T obj){
    	ByteArrayOutputStream os = new ByteArrayOutputStream();  
        Hessian2Output ho2 = new Hessian2Output(os);  
        
        try {    
            ho2.writeObject(obj);    
            try{
            	ho2.close();
            	ho2 = null;
            }
            catch(IOException e)
            {
            	log.error("serializeHessian, close ho2 failed:" + e);
            }
            byte[] ret = os.toByteArray();
            try{
            	os.close();
            	os = null;
            }
            catch(IOException e)
            {
            	log.error("serializeHessian, close ho2 failed:" + e);
            }
            return ret;
        } catch (Exception e) {
        	log.error("Hessian serialize message failed.", e);
        	throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
		} 
        finally
		{
        	try {
        		if(ho2 != null)
        		{
        			ho2.close();
        		}
        		if(os != null)
        		{
        			os.close();
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
        	log.error("Protostuff deserialize message failed.", e);
            throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
        }
    }
    
    public static <T> T deserializeFastjson(byte[] data, Class<T> cls)
    {
    	try{
    		@SuppressWarnings("unchecked")
			T message = (T) JSON.parse(data);
    		return message;
    	} catch (Exception e) {
    		log.error("Fastjson deserialize message failed.", e);
            throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
    	}
    }
    
    @SuppressWarnings("unchecked")
	public  static <T> T deserializeHessian(byte[] data, Class<T> cls) {
    	HessianFactory hFactory = new HessianFactory();
   		ByteArrayInputStream is = new ByteArrayInputStream(data); 
   		Hessian2Input h2in =hFactory.createHessian2Input(is);  
   		
        try {
            T message = (T) objenesis.newInstance(cls);
    		message= (T)h2in.readObject();
            return message;
        } catch (Exception e) {
        	log.error("Hessian deserialize message failed.", e);
            throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
        } finally {
        	try {
				h2in.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    
    }
}
