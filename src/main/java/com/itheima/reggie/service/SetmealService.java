package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     *
     * @param ids
     */
    boolean removeWithDish(List<Long> ids);

    /**
     * 起售或者停售
     *
     * @param status
     * @param ids
     */
    void updateStatus(Integer status, List<Long> ids);

    /**
     * 根据id查询套餐信息和对应的菜品信息
     *
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    /**
     * 修改套餐信息
     *
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);
}
