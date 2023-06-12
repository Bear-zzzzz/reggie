package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //查询套餐或者菜品是否在购物车上，如有数量加1就可以，不存在直接添加，数量为1
        shoppingCart.setUserId(BaseContext.getCurrentId());
        Long dishId=shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        if(dishId!=null){
            wrapper.eq(ShoppingCart::getDishId,dishId);

        }
        else {
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one= shoppingCartService.getOne(wrapper);
        if(one==null){
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }
        else {
            Integer num=one.getNumber();
            one.setNumber(num+1);
            shoppingCart=one;
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(shoppingCart);
        }
        return R.success(shoppingCart);
    }


    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        //查询数量为1，直接删除，别的情况下减一
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查询当前菜品或套餐是否 在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 根据登录用户的 userId去ShoppingCart表中查询该用户的购物车数据
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 添加进购物车的是菜品，且 购物车中已经添加过 该菜品
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one=shoppingCartService.getOne(queryWrapper);
        if(one!=null){
            int num=one.getNumber();
            if(num>1){
                one.setNumber(num-1);
                one.setCreateTime(LocalDateTime.now());
                shoppingCartService.updateById(one);
            }
            else {

                shoppingCartService.removeById(one.getId());
            }
        }

        return R.success("修改成功");
    }
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(wrapper);
        return R.success("清空成功");
    }
    @GetMapping("/list")
    public R<List<ShoppingCart>>list(){
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList=shoppingCartService.list(wrapper);
        return R.success(shoppingCartList);
    }
}
