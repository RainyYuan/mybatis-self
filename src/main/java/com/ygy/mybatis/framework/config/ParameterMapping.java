package com.ygy.mybatis.framework.config;

import lombok.Data;

/**
 * 封装了#{}解析出来的参数名称和参数类型
 */
@Data
public class ParameterMapping {
    private String name;
    private Class type;
}
