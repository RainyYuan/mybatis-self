package com.kkb.spring.factory.support;

import com.kkb.spring.config.BeanDefinition;
import com.kkb.spring.registry.BeanDefinitionRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 即是spring中的真正管理Bean实例的容器工厂
 * 同时又是管理BeanDefinition的BeanDefinition注册器
 */
public class DefaultListableBeanFactory extends AbstractAutowiredCapableBeanFactory implements BeanDefinitionRegistry {
    // 存储BeanDefinition的容器
    private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();


    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitions.get(beanName);
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition bd) {
        this.beanDefinitions.put(beanName,bd);
    }
}
