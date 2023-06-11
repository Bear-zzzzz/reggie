package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Sire
 * @version 1.0
 * @date 2022-06-02 15:43
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan //扫描过滤器
@EnableTransactionManagement //开启事务支持
@EnableCaching // 开启Spring Cache缓存支持
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功！！！");
    }
}
