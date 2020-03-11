package dev.valentinspac.proxy.framework;

import dev.valentinspac.proxy.MyTransactionInvocationHandler;
import dev.valentinspac.proxy.ProxyApp;
import org.reflections.Reflections;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProxyFactory {
    private  List<MyCustomProxy> beanRegistry;

    public ProxyFactory(Package packageToLookup) {
        Reflections reflections = new Reflections(packageToLookup.getName());
        Set<Class<?>> transactionalServiceClasses = reflections.getTypesAnnotatedWith(TransactionalService.class);

        List<?> beans = instantiateBeans(transactionalServiceClasses);
        beanRegistry = createProxies(beans);
    }

    /**
     * Creates custom proxies for the provided beans
     */
    private List<MyCustomProxy> createProxies(List<?> beans) {
        return beans.stream()
                .map(this::createProxy)
                .collect(Collectors.toList());
    }

    /**
     * For the provided bean which, it creates a proxy instance for all the interfaces it defines
     * @param bean target to be proxied
     * @return an instance of {@link MyCustomProxy}
     */
    private MyCustomProxy createProxy(Object bean) {
        InvocationHandler handler = new MyTransactionInvocationHandler(bean);
        Object proxyObj = Proxy.newProxyInstance(ProxyApp.class.getClassLoader(), bean.getClass().getInterfaces(), handler);

        return new MyCustomProxy(Arrays.asList(bean.getClass().getInterfaces()), proxyObj);
    }

    /**
     * Instantiates objects using the default no-arg constructor
     */
    private List<?> instantiateBeans(Set<Class<?>> annotated) {
        return annotated.stream()
                .map(this::instantiateClass)
                .collect(Collectors.toList());
    }

    /**
     * Instantiates an object for the given class {@param aClass} using the default, no-arg constructor
     * @param aClass class to be instantiated
     * @return an instance of {@param aClass}
     */
    private Object instantiateClass(Class<?> aClass) {
        try {
            return aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate class " + aClass);
        }
    }

    /**
     * Searches and returns first proxy that can be casted to the specified type
     * @param clazz class to be casted to
     */
    public <T> T getBean(Class<T> clazz) {
        Object proxy = beanRegistry.stream()
                .filter(p -> p.hasInterface(clazz))
                .findFirst()
                .map(MyCustomProxy::getJdkProxy)
                .orElseThrow(() -> new RuntimeException("No Bean found for class " + clazz));

        return clazz.cast(proxy);
    }

}
