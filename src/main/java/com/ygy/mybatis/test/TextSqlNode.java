package com.ygy.mybatis.test;

public class TextSqlNode extends SqlNode{
    public TextSqlNode(String trim) {

    }

    public boolean isDynamic() {
        return false;
    }
}
