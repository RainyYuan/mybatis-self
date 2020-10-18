package com.kkb.spring.reader;

import com.kkb.spring.registry.BeanDefinitionRegistry;
import com.kkb.spring.utils.DocumentUtils;
import org.dom4j.Document;

import java.io.InputStream;

/**
 * 针对XML对于的InputStream流对象进行解析
 */
public class XmlBeanDefinitionReader {
    private BeanDefinitionRegistry registry;

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void loadBeanDefinitions(InputStream inputStream) {
        // 创建文档对象
        Document document = DocumentUtils.getDocument(inputStream);
        XmlBeanDefinitionDocumentReader documentReader = new XmlBeanDefinitionDocumentReader(registry);
        documentReader.registerBeanDefinitions(document.getRootElement());
    }
}
