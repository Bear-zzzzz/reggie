package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 *
 * @author Sire
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        // 清理 后台修改分类 下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询(管理员用的)
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝 这里需要忽略 records（是所有的对象集合），因为泛型，全部拷贝的话会更改泛型
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取记录对象集合
        List<Dish> records = pageInfo.getRecords();
        // 这里的操作主要是为了 给前端传递 categoryName【这也是DishDto中字段的由来】
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //产品类型赋值
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        // 需要清除redis中的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("根据id删除菜品信息...");
        if (null == ids || ids.size() == 0) {
            return R.error("菜品id不能为空");
        }
        boolean b = dishService.deleteWhitFlavor(ids);
        // 如果菜品正在售卖中
        if (!b) {
            return R.error("菜品正在售卖中，不能删除");
        }
        return R.success("删除成功");
    }

    /**
     * 起售或者停售
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        // 只有当处于起售的状态，才会进行清除缓存
        if (status == 0) {
            // 由于可能是多个id进行操作，所以需要借助一个集合来存储
            ArrayList<String> keys = new ArrayList<>();
            for (Long id : ids) {
                Dish dish = dishService.getById(id);
                String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
                keys.add(key);
            }
            // 需要清除redis中的缓存
            redisTemplate.delete(keys);
        }
        log.info("根据id进行起售或者停售菜品……");
        if (null == ids || ids.size() == 0) {
            return R.error("菜品id不能为空");
        }
        dishService.updateStatus(status, ids);
        return R.success("装态更改成功");
    }

    /**
     * 根据条件查询对应的菜品数据（用户使用的）
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        // 使用缓存进行代码优化
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 首先要从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果缓存中存在则直接返回
        if (null != dishDtoList) {
            return R.success(dishDtoList);
        }
        log.info("dish:{}", dish);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()), Dish::getName, dish.getName());
        queryWrapper.eq(null != dish.getCategoryId(), Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 列出所有的Dish
        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoList = dishes.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, item.getId());

            dishDto.setFlavors(dishFlavorService.list(wrapper));
            return dishDto;
        }).collect(Collectors.toList());
        // 如果不存在，就查数据库，然后存到redis中 60分钟后过期
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }


}
