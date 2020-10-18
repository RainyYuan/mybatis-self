package com.kkb.spring.registry.suppport;

import com.kkb.spring.registry.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    // 存储单例Bean实例的Map容器(线程安全的进行单例管理)
    private Map<String,Object> singletonObjects = new HashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        return this.singletonObjects.get(beanName);
    }

    @Override
    public void addSingleton(String beanName, Object bean) {
        // TODO 可以使用双重检查锁进行安全处理
        this.singletonObjects.put(beanName,bean);
    }
}
