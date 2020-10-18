package com.kkb.spring.factory;

import java.util.List;

/**
 * 对于Bean容器中的Bean可以进行集合操作或者说叫批量操作
 */
public interface ListableBeanFactory extends BeanFactory{
    List<Object> getBeansByType(Class type);
}
