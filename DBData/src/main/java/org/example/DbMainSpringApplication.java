package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan(basePackages = {"org.example.db.modules.**.mapper"})
public class DbMainSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(DbMainSpringApplication.class, args);
    }
}
