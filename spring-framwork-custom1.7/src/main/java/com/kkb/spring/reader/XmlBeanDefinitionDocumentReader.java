package com.kkb.spring.reader;

import com.kkb.spring.config.BeanDefinition;
import com.kkb.spring.config.PropertyValue;
import com.kkb.spring.config.RuntimeBeanReference;
import com.kkb.spring.config.TypedStringValue;
import com.kkb.spring.registry.BeanDefinitionRegistry;
import com.kkb.spring.utils.ReflectUtils;
import org.dom4j.Element;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 针对Document对于的InputStream流对象进行解析
 */
public class XmlBeanDefinitionDocumentReader {
    private BeanDefinitionRegistry registry;

    public XmlBeanDefinitionDocumentReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void registerBeanDefinitions(Element rootElement) {
        // 获取<bean>和自定义标签（比如mvc:interceptors）
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            // 获取标签名称
            String name = element.getName();
            if (name.equals("bean")) {
                // 解析默认标签，其实也就是bean标签
                parseDefaultElement(element);
            } else {
                // 解析自定义标签，比如aop:aspect标签
                parseCustomElement(element);
            }
        }
    }

    private void parseDefaultElement(Element beanElement) {
        try {
            if (beanElement == null) {
                return;
            }
            // 获取id属性
            String id = beanElement.attributeValue("id");

            // 获取name属性
            String name = beanElement.attributeValue("name");

            // 获取class属性
            String clazzName = beanElement.attributeValue("class");
            if (clazzName == null || "".equals(clazzName)) {
                return;
            }

            // 获取init-method属性
            String initMethod = beanElement.attributeValue("init-method");
            // 获取scope属性
            String scope = beanElement.attributeValue("scope");
            scope = scope != null && !scope.equals("") ? scope : "singleton";

            // 获取beanName
            String beanName = id == null ? name : id;
            Class<?> clazzType = Class.forName(clazzName);
            beanName = beanName == null ? clazzType.getSimpleName() : beanName;
            // 创建BeanDefinition对象
            // 此次可以使用构建者模式进行优化
            BeanDefinition beanDefinition = new BeanDefinition(clazzName, beanName);
            beanDefinition.setInitMethod(initMethod);
            beanDefinition.setScope(scope);
            // 获取property子标签集合
            List<Element> propertyElements = beanElement.elements();
            for (Element propertyElement : propertyElements) {
                parsePropertyElement(beanDefinition, propertyElement);
            }

            // 注册BeanDefinition信息
            registry.registerBeanDefinition(beanName, beanDefinition);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void parsePropertyElement(BeanDefinition beanDefination, Element propertyElement) {
        if (propertyElement == null)
            return;

        // 获取name属性
        String name = propertyElement.attributeValue("name");
        // 获取value属性
        String value = propertyElement.attributeValue("value");
        // 获取ref属性
        String ref = propertyElement.attributeValue("ref");

        // 如果value和ref都有值，则返回
        if (value != null && !value.equals("") && ref != null && !ref.equals("")) {
            return;
        }

        /**
         * PropertyValue就封装着一个property标签的信息
         */
        PropertyValue pv = null;

        if (value != null && !value.equals("")) {
            // 因为spring配置文件中的value是String类型，而对象中的属性值是各种各样的，所以需要存储类型
            TypedStringValue typeStringValue = new TypedStringValue(value);

            Class<?> targetType = ReflectUtils.getTypeByFieldName(beanDefination.getClazzName(), name);
            typeStringValue.setTargetType(targetType);

            pv = new PropertyValue(name, typeStringValue);
            beanDefination.addPropertyValue(pv);
        } else if (ref != null && !ref.equals("")) {

            RuntimeBeanReference reference = new RuntimeBeanReference(ref);
            pv = new PropertyValue(name, reference);
            beanDefination.addPropertyValue(pv);
        } else {
            return;
        }
    }


    private void parseCustomElement(Element element) {
        // AOP、TX、MVC标签的解析，都是走该流程
    }

}
