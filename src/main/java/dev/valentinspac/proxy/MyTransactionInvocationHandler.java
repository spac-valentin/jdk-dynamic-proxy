package dev.valentinspac.proxy;

import dev.valentinspac.proxy.framework.ProxyInvocationFailedException;
import dev.valentinspac.proxy.framework.MyCustomTransaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class MyTransactionInvocationHandler implements InvocationHandler {

    private final Object target;
    private final Class<?> targetClass;

    public MyTransactionInvocationHandler(Object targetObject) {
        this.target = targetObject;
        this.targetClass = targetObject.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = getOverriddenMethod(method);

        return getTransactionalMethod(targetMethod)
                .map(annotation -> handleTransactionalMethod(method, args, annotation))
                .orElseGet(() -> uncheckedInvoke(method, args));

    }

    private Object handleTransactionalMethod(Method method, Object[] args, MyCustomTransaction annotation)  {
        Object result;
        System.out.println(String.format("Opening transaction [%s] with params %s", annotation.value(), Arrays.toString(args)));

        try {
            result = uncheckedInvoke(method, args);
        } catch (RuntimeException e) {
            System.out.println(String.format("Rollback transaction transaction %s...", annotation.value()));
            throw e;
        }

        System.out.println(String.format("Committing transaction %s...", annotation.value()));

        return result;
    }

    private Optional<MyCustomTransaction> getTransactionalMethod(Method method) {
        return Optional.ofNullable(method.getDeclaredAnnotation(MyCustomTransaction.class));
    }

    private Object uncheckedInvoke(Method method, Object[] args) throws RuntimeException {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ProxyInvocationFailedException("Could not invoke method " + method.getName(), e);
        }
    }

    /**
     * Returns the method from {@link MyTransactionInvocationHandler#target} with the same signature ( the
     * implementation of a method defined in interface)
     */
    private Method getOverriddenMethod(Method method) throws NoSuchMethodException {
        return targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
    }
}
