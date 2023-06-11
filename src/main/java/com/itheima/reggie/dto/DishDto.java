package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sire
 */
@Data
public class DishDto extends Dish {

    /**
     * 菜品口味
     */
    private List<DishFlavor> flavors = new ArrayList<>();

    /**
     * 菜品名称
     */
    private String categoryName;

    /**
     * 份数
     */
    private Integer copies;
}
