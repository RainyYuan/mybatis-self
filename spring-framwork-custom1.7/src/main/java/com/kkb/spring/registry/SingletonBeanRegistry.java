package com.kkb.spring.registry;

/**
 * 1.实现类封装了spring容器创建出来的所有的单例Bean的集合
 * 2.接口提供了对于其封装的数据进行操作的接口功能（获取Bean\添加Bean）
 */
public interface SingletonBeanRegistry {
    /**
     * 获取单例Bean
     * @param beanName
     * @return
     */
    Object getSingleton(String beanName);

    /**
     * 添加单例Bean到集合缓存中
     * @param beanName
     * @param bean
     */
    void addSingleton(String beanName,Object bean);
}
