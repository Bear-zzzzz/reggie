package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    @Transactional
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}"+addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.eq(AddressBook::getIsDefault, 1);
        wrapper.set(AddressBook::getIsDefault, 0);

        addressBookService.update(wrapper);
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id){
        AddressBook addressBook=addressBookService.getById(id);
        if(addressBook==null)
            return R.error("不存在");
        else
            return R.success(addressBook);
    }
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook=addressBookService.getOne(wrapper);
        if(addressBook==null)
            return R.error("不存在");
        else
            return R.success(addressBook);
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(addressBook.getUserId()!=null,AddressBook::getUserId,addressBook.getUserId());
        lambdaQueryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(lambdaQueryWrapper));

    }
    @DeleteMapping
    public R<String> delete(String ids){
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.updateById(addressBook);
        return R.success("更新成功");
    }
}
