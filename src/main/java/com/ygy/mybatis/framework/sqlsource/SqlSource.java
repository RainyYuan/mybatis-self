package com.ygy.mybatis.framework.sqlsource;

import com.ygy.mybatis.test.BoundSql;

public interface SqlSource {
    BoundSql getBoundSql(Object param);
}
