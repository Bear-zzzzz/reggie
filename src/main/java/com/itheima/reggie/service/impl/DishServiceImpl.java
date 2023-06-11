package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sire
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //菜品id
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品信息, 同时删除其对应的菜品口味
     *
     * @param ids
     */
    @Override
    @Transactional
    public boolean deleteWhitFlavor(List<Long> ids) {
        //select count(*) from dish where id in (1,2,3) and status = 1
        //查询菜品状态，确定是否可用删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);

        // 查看符合条件的记录数
        int count = this.count(queryWrapper);
        if (count > 0) {
            return false;
        }

        //如果可以删除，先删除菜品表中的数据---dish
        this.removeByIds(ids);

        //delete from dish_flavor where dish_id in (1,2,3)
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        //删除关系表中的数据----dish_flavor
        dishFlavorService.remove(lambdaQueryWrapper);
        return true;

        /** 一开始的思路，很危险，典型的慢sql语句，遍历删除【不要使用】 **/
        /*for (String id : ids) {
            this.removeById(id);
            dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().like(DishFlavor::getDishId, id));
        }*/
    }

    /**
     * 起售或者停售
     *
     * @param status
     * @param ids
     */
    @Override
    @Transactional
    public void updateStatus(Integer status, List<Long> ids) {
        // 先查询出目标菜品（简单优化后的）
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.in(Dish::getId, ids);
        // 设置状态
        updateWrapper.set(Dish::getStatus, status);
        this.update(updateWrapper);
    }


}
