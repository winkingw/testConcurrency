package com.utgaming.testconcurrency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.utgaming.testconcurrency.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {
    @Update("update product set stock = stock - #{count} , updated_at = now() " + "where id = #{id} and stock >= #{count}")
    int deductStock(@Param("id") Long id,@Param("count") Integer count);
}