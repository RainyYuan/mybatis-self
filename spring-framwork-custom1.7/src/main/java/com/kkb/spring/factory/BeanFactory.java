package com.kkb.spring.factory;

/*
 顶层接口
 */
public interface BeanFactory {
    Object getBean(String beanName);
}
