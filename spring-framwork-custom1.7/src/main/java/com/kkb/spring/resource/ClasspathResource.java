package com.kkb.spring.resource;

import java.io.InputStream;

/**
 * 封装的是classpath路径下的配置文件路径
 */
public class ClasspathResource implements Resource {
    private String location;

    public ClasspathResource(String location) {
        this.location = location;
    }

    @Override
    public InputStream getResource() {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }
}
