package com.miracle.module.rpc.jmeter.utils;


import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ParametersJson {

	public static String getAllJson(Type[] parameterTypes)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		String allJson = "{";
		int i = 1;
		for (Type parameterType : parameterTypes) {
			if (allJson.length() > 1) {
				allJson += ",";
			}
			allJson += "'请求参数" + i + "':";
			allJson += getJsonExample(parameterType);
			i++;
		}
		allJson += "}";
		return allJson;
	}

	private static String getJsonExample(Type parameterType)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		if (parameterType.getClass() == Class.class) {
			return getJsonNoGeneric((Class<?>) parameterType);
		} else if (ParameterizedType.class.isAssignableFrom(parameterType
				.getClass())) {
			return getJsonGeneric((ParameterizedType) parameterType);
		} else if (GenericArrayType.class.isAssignableFrom(parameterType
				.getClass())) {
			return getJsonArrayGeneric(((GenericArrayType) parameterType));
		} else {
            return "未知类型参数无法自动生成Json格式,请使用groovy脚本";
		}

	}

	private static String getJsonArrayGeneric(GenericArrayType genericArrayType) {
		if (GenericArrayType.class.isAssignableFrom(genericArrayType
				.getGenericComponentType().getClass())) {
			String json = getJsonArrayGeneric((GenericArrayType) genericArrayType
					.getGenericComponentType());
			return "["+json+","+json+"]";
		} else if(genericArrayType
				.getGenericComponentType().getClass() == Class.class) {
			String json = getJsonNoGeneric((Class<?>)genericArrayType
				.getGenericComponentType());
			return "["+json+","+json+"]";
		}
        return "未知类型参数无法自动生成Json格式,请使用groovy脚本";
	}

	private static String getJsonGeneric(ParameterizedType parameterType)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Type type = parameterType.getRawType();
		if (type.getClass() != Class.class) {
            return "未知类型参数无法自动生成Json格式,请使用groovy脚本";
		}
		if (Collection.class.isAssignableFrom(((Class<?>) type))) {
			String json = getJsonExample(parameterType.getActualTypeArguments()[0]);
			return "[" + json + "," + json + "]";
		} else if (Map.class.isAssignableFrom(((Class<?>) type))) {
			String key = getJsonExample(parameterType.getActualTypeArguments()[0]);
			String value = getJsonExample(parameterType
					.getActualTypeArguments()[1]);
			return "{" + key + ":" + value + "," + key + ":" + value + "}";
		} else {
			return getJsonNoGeneric((Class<?>) type);
		}
	}

	
	private static int MAX_DEPTH = 3;
	private static String getJsonNoGeneric(Class<?> parameterType) {
		Object obj = null;
		try {
			obj = getParamterInstance(parameterType, MAX_DEPTH);

		} catch (Exception e) {
            return parameterType.getSimpleName() + "类型参数无默认构造函数无法自动生成Json格式,请使用groovy脚本";
		}
		return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteClassName,
				SerializerFeature.WriteNullListAsEmpty,
				SerializerFeature.WriteNullBooleanAsFalse,
				SerializerFeature.WriteNullListAsEmpty,
				SerializerFeature.WriteNullNumberAsZero,
				SerializerFeature.WriteNullStringAsEmpty,
				SerializerFeature.UseSingleQuotes);
	}

	private static Map<Class<?>, Object> parameterInstance = new HashMap<Class<?>, Object>();
	static {
		parameterInstance.put(int.class, 0);
		parameterInstance.put(Integer.class, 0);
		parameterInstance.put(long.class, 0l);
		parameterInstance.put(Long.class, 0l);
		parameterInstance.put(float.class, 0.0f);
		parameterInstance.put(Float.class, 0.0f);
		parameterInstance.put(double.class, 0.00d);
		parameterInstance.put(Double.class, 0.00d);
		parameterInstance.put(byte.class, "a".getBytes()[0]);
		parameterInstance.put(Byte.class, "a".getBytes()[0]);
		parameterInstance.put(boolean.class, false);
		parameterInstance.put(Boolean.class, false);
		parameterInstance.put(short.class, 0);
		parameterInstance.put(Short.class, false);
		parameterInstance.put(char.class, 'a');
		parameterInstance.put(Character.class, 'a');
		parameterInstance.put(Date.class, new Date(System.currentTimeMillis()));
	}


	static private Object getParamterInstance(Class<?> parameterType, int depth)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		if (parameterInstance.get(parameterType) != null) {
			return parameterInstance.get(parameterType);
		}

		Object retObj = parameterType.getConstructor().newInstance();
		if(depth <= 0)
		{
			return retObj;
		}
		
		Method[] methods = parameterType.getMethods();
        for (Method method : methods) 
        {
            try {
                String name = method.getName();
                if (name.length() > 3 && name.startsWith("set") && Modifier.isPublic(method.getModifiers()) &&
                        method.getParameterTypes().length == 1)
                {
                	Class<?>[] paramTypes = method.getParameterTypes();
                	Object paramObj = getParamterInstance(paramTypes[0], depth-1);
                	method.invoke(retObj, paramObj);
                } 
            } catch (Exception e) {
            	//swallow exception here
                //throw new IllegalStateException(e.getMessage(), e);
            }
        }
		
		return retObj;
	}
}

