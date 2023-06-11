package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 分类管理
 * @author Sire
 */
@Repository
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
