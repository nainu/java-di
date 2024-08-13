package com.interface21.beans.factory.support;

import com.interface21.beans.BeansCache;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.beans.factory.config.BeanDefinition;
import com.interface21.beans.factory.exception.NoSuchBeanDefinitionException;
import com.interface21.context.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultListableBeanFactory implements BeanFactory, BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private final Map<Class<?>, BeanInstantiation> beanInstantiationsMap = new HashMap<>();

    private final BeansCache singletonObjects = new BeansCache();

    @Override
    public void initialize() {
        loadAllBeans();
    }

    private void loadAllBeans() {
        beanInstantiationsMap.keySet().forEach(this::getBean);
    }

    @Override
    public Set<Class<?>> getBeanClasses() {
        return singletonObjects.getAllBeanClasses();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> clazz) {
        Object bean = singletonObjects.get(clazz);
        if (bean != null) {
            return (T) bean;
        }
        return (T) instantiateClass(clazz);
    }

    private Object instantiateClass(final Class<?> requiredType) {
        Object object;
        if (beanInstantiationsMap.containsKey(requiredType)) {
            object = beanInstantiationsMap.get(requiredType).instantiateClass(this);
        } else {
            final Class<?> concreteClass = findConcreteClass(requiredType, beanInstantiationsMap.keySet());
            object = getBean(concreteClass);
        }

        singletonObjects.store(requiredType, object);
        return object;
    }

    private static Class<?> findConcreteClass(final Class<?> clazz, final Set<Class<?>> candidates) {
        return BeanFactoryUtils
                .findConcreteClass(clazz, candidates)
                .orElseThrow(() -> new NoSuchBeanDefinitionException(clazz));
    }

    @Override
    public void clear() {
    }

    @Override
    public BeansCache getControllers() {
        return singletonObjects.filter(clazz -> clazz.isAnnotationPresent(Controller.class));
    }

    @Override
    public void registerBeanInstantiation(Class<?> clazz, BeanInstantiation beanInstantiation) {
        beanInstantiationsMap.put(clazz, beanInstantiation);
    }

    @Override
    public void registerBeanDefinition(Class<?> clazz, BeanDefinition beanDefinition) {
        // 아직 아무데서도 쓰지 않음
//        beanDefinitionMap.put(clazz.getName(), beanDefinition);
    }
}
