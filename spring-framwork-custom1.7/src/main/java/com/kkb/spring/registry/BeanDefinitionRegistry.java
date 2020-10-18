package com.kkb.spring.registry;

import com.kkb.spring.config.BeanDefinition;

/**
 * 1.实现类是封装了BeanDefinition集合信息
 * 2.接口类是提供对于其封装的BeanDefinition信息进行添加和获取功能
 */
public interface BeanDefinitionRegistry {
    BeanDefinition getBeanDefinition(String beanName);

    void registerBeanDefinition(String beanName,BeanDefinition bd);
}
