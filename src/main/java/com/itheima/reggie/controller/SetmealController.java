package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;
@PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("保存菜单成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }
    @GetMapping("/page")
    public  R<Page> page(int page, int pageSize, String name){
    Page<Setmeal> pageinfo=new Page<>(page,pageSize);
    Page<SetmealDto> dtoPage=new Page<>(page,pageSize);
    LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
    lambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
    lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
    setmealService.page(pageinfo,lambdaQueryWrapper);
    //对象拷贝
        BeanUtils.copyProperties(pageinfo,dtoPage,"records");
        List<Setmeal> records= pageinfo.getRecords();
        List<SetmealDto> list=records.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
           Long categoryId=item.getCategoryId();
            Category category=categoryService.getById(categoryId);
            if(category!=null){
                String categoryname=category.getName();
                setmealDto.setCategoryName(categoryname);
            }
            BeanUtils.copyProperties(item,setmealDto);
            return setmealDto;
        }).collect(Collectors.toList());
    dtoPage.setRecords(list);
    return  R.success(dtoPage);
    }
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids, HttpServletRequest request){
        Long id = (Long) request.getSession().getAttribute("employee");
        BaseContext.setCurrentId(id);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(ids != null,Setmeal::getId,ids);

        List<Setmeal> list = setmealService.list(queryWrapper);


        for (Setmeal setMeal : list) {
            if (setMeal != null){
                setMeal.setStatus(status);
                setmealService.updateById(setMeal);
            }
        }
        return R.success(status == 1? "启售成功" : "停售成功");
    }
}
