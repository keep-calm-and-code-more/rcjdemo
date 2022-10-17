package com.example.rcjdemo.common;

import tk.mybatis.mapper.common.*;

/***
 * @description 调用tkmapper进行接口封装
 * @param <T>
 */
public interface MyMapper<T> extends
        BaseMapper<T>,
        ExampleMapper<T>,
        RowBoundsMapper<T>,
        Marker, ConditionMapper<T>, Mapper<T> {

}
