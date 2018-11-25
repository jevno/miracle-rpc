package com.miracle.module.rpc.core.api.coordinator;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodProceedingJoinPoint implements ProceedingJoinPoint, JoinPoint.StaticPart {

    private Object proxy;

    private Object target;

    private Method method;

    private Object[] args;

    private Signature signature;
    
    private Class<?> interfaceClass;

    /**
     * Lazily initialized source location object
     */
    private SourceLocation sourceLocation;

    public MethodProceedingJoinPoint(Object proxy, Object target, Class<?> interfaceClass, Method method, Object[] args) {
        this.proxy = proxy;
        this.target = target;
        this.interfaceClass = interfaceClass;
        this.method = method;
        this.args = args;
    }

    @Override
    public void set$AroundClosure(AroundClosure aroundClosure) {
        throw new UnsupportedOperationException();
    }
    
    private void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) 
        		&& !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    @Override
    public Object proceed() throws Throwable {

        // Use reflection to invoke the method.
        try {
        	makeAccessible(method);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            // Invoked method threw a checked exception.
            // We must rethrow it. The client won't see the interceptor.
            throw ex.getTargetException();
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Tried calling method [" +
                    method + "] on target [" + target + "] failed", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not access method [" + method + "]", ex);
        }
    }

    @Override
    public Object proceed(Object[] objects) throws Throwable {
        throw new UnsupportedOperationException();
    }

    public String toShortString() {
        return "execution(" + getSignature().toShortString() + ")";
    }

    public String toLongString() {
        return "execution(" + getSignature().toLongString() + ")";
    }

    public String toString() {
        return "execution(" + getSignature().toString() + ")";
    }

    @Override
    public Object getThis() {
        return this.proxy;
    }

    public Method getMethod() {
    	return this.method;
    }
    
    public Class<?> getInterfaceClass()
    {
    	return this.interfaceClass;
    }
    
    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public Signature getSignature() {
        if (this.signature == null) {
            this.signature = new MethodSignatureImpl();
        }
        return signature;
    }

    @Override
    public SourceLocation getSourceLocation() {
        if (this.sourceLocation == null) {
            this.sourceLocation = new SourceLocationImpl();
        }
        return this.sourceLocation;
    }

    @Override
    public String getKind() {
        return ProceedingJoinPoint.METHOD_EXECUTION;
    }

    @Override
    public StaticPart getStaticPart() {
        return this;
    }

    /**
     * Lazily initialized MethodSignature.
     */
    private class MethodSignatureImpl implements MethodSignature {

        public String getName() {
            return method.getName();
        }

        public int getModifiers() {
            return method.getModifiers();
        }

        @SuppressWarnings("rawtypes")
		public Class getDeclaringType() {
            return method.getDeclaringClass();
        }

        public String getDeclaringTypeName() {
            return method.getDeclaringClass().getName();
        }

        @SuppressWarnings("rawtypes")
		public Class getReturnType() {
            return method.getReturnType();
        }

        public Method getMethod() {
            return method;
        }

        @SuppressWarnings("rawtypes")
		public Class[] getParameterTypes() {
            return method.getParameterTypes();
        }

        public String[] getParameterNames() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("rawtypes")
		public Class[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        public String toShortString() {
            return toString(false, false, false, false);
        }

        public String toLongString() {
            return toString(true, true, true, true);
        }

        public String toString() {
            return toString(false, true, false, true);
        }

        @SuppressWarnings("rawtypes")
		private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs,
                                boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {
            StringBuilder sb = new StringBuilder();
            if (includeModifier) {
                sb.append(Modifier.toString(getModifiers()));
                sb.append(" ");
            }
            if (includeReturnTypeAndArgs) {
                appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
                sb.append(" ");
            }
            appendType(sb, getDeclaringType(), useLongTypeName);
            sb.append(".");
            sb.append(getMethod().getName());
            sb.append("(");
            Class[] parametersTypes = getParameterTypes();
            appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
            sb.append(")");
            return sb.toString();
        }

        private void appendTypes(StringBuilder sb, Class<?>[] types,
                                 boolean includeArgs, boolean useLongReturnAndArgumentTypeName) {
            if (includeArgs) {
                for (int size = types.length, i = 0; i < size; i++) {
                    appendType(sb, types[i], useLongReturnAndArgumentTypeName);
                    if (i < size - 1) {
                        sb.append(",");
                    }
                }
            } else {
                if (types.length != 0) {
                    sb.append("..");
                }
            }
        }

        private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
            if (type.isArray()) {
                appendType(sb, type.getComponentType(), useLongTypeName);
                sb.append("[]");
            } else {
                sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
            }
        }
    }


    /**
     * Lazily initialized SourceLocation.
     */
    private class SourceLocationImpl implements SourceLocation {

        @SuppressWarnings("rawtypes")
		public Class getWithinType() {
            if (proxy == null) {
                throw new UnsupportedOperationException("No source location joinpoint available: target is null");
            }
            return proxy.getClass();
        }

        public String getFileName() {
            throw new UnsupportedOperationException();
        }

        public int getLine() {
            throw new UnsupportedOperationException();
        }

        public int getColumn() {
            throw new UnsupportedOperationException();
        }
    }

}
