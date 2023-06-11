package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     *
     * @param dishDto
     */
    void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    DishDto getByIdWithFlavor(Long id);

    /**
     * 更新菜品信息，同时更新对应的口味信息
     *
     * @param dishDto
     */
    void updateWithFlavor(DishDto dishDto);

    /**
     * 删除菜品信息, 同时删除其对应的菜品口味
     *
     * @param ids
     */
    boolean deleteWhitFlavor(List<Long> ids);

    /**
     * 起售或者停售
     *
     * @param status
     * @param ids
     */
    void updateStatus(Integer status, List<Long> ids);
}
