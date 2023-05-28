package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Override
     public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishList=setmealDto.getSetmealDishes();

        setmealDishList.stream().map(item->{
            item.setSetmealId(setmealDto.getId());
            return item;
                }

        ).collect(Collectors.toList());
     setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count=this.count(queryWrapper);
        if(count>0){
            throw  new CustomException("套餐正在售卖中，不能删除");
        }
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapperyWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapperyWrapper.in(SetmealDish::getDishId,ids);
        setmealDishService.removeByIds(ids);
    }
}
