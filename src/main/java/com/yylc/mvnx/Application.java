package com.yylc.mvnx;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * @author xiangnan
 * @date 2018/3/1 11:08
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);

        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Resource
    private Starter starter;

    @Override
    public void run(String... args) throws Exception {
        starter.run(CollectionUtil.newArrayList(args));
    }

}
