package com.kkb.spring.factory.support;

import com.kkb.spring.config.BeanDefinition;
import com.kkb.spring.config.PropertyValue;
import com.kkb.spring.resolver.BeanDefinitionValueResolver;
import com.kkb.spring.utils.ReflectUtils;

import java.util.List;

/**
 * 完成Bean的创建和依赖装配
 */
public abstract class AbstractAutowiredCapableBeanFactory extends AbstractBeanFactory{
    @Override
    public Object createBean(BeanDefinition bd) {
        // 1.Bean的实例化
        Object bean = createBeanByConstructor(bd);

        // TODO 处理循环依赖

        // 2.Bean的属性填充（依赖注入）
        populateBean(bean,bd);
        // 3.Bean的初始化
        initilizeBean(bean,bd);
        return bean;
    }
    private void initilizeBean(Object bean, BeanDefinition beanDefinition) {
        // TODO 需要针对Aware接口标记的类进行特殊处理

        // TODO 可以进行IntilizingBean接口的处理
        invokeInitMethod(bean,beanDefinition);
    }

    private void invokeInitMethod(Object bean, BeanDefinition beanDefinition) {
        try {
            String initMethod = beanDefinition.getInitMethod();
            if (initMethod == null) {
                return;
            }
            Class<?> clazzType = beanDefinition.getClazzType();
            ReflectUtils.invokeMethod(bean,initMethod);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void populateBean(Object bean, BeanDefinition beanDefinition) {
        List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue pv : propertyValues) {
            String name = pv.getName();
            // 这不是我们需要给Bean设置的value值
            Object value = pv.getValue();

            BeanDefinitionValueResolver resolver = new BeanDefinitionValueResolver(this);
            Object valueToUse = resolver.resoleValue(value);

            ReflectUtils.setProperty(bean,name,valueToUse);
        }
    }

    private Object createBeanByConstructor(BeanDefinition beanDefinition) {
        // TODO 静态工厂方法、工厂实例方法
        // 构造器方式去创建Bean实例
        Class<?> clazzType = beanDefinition.getClazzType();
        return ReflectUtils.createBean(clazzType);
    }
}
