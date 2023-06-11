package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 菜品
 *
 * @author Sire
 */
@Repository
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
